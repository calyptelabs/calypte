package calypte.buffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;

import calypte.buffer.VirtualSegmentMapping.Item;
import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class VirtualByteArray implements ByteArray{

	private static final Unsafe UNSAFE;
	
	private static final long BYTE_ARRAY_OFFSET;

	private static final long LONG_ARRAY_OFFSET;
	
    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
    		BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
    		LONG_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(long[].class);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
	private long size;
	
	protected ByteArray memory;
	
	protected VirtualSegmentMapping segmentMapping;
	
	protected int blockSize;
	
	protected long dataSize;
	
	protected long dataOffset;
	
	protected long mappingSize;
	
	protected long mappingOffset;
	
	protected int blockSHL;
	
	public VirtualByteArray(long bytesMemory, long size, int blockSize, File file) throws IOException {
		this.blockSHL       = createBlockSHL(blockSize) - 1;
		this.blockSize      = 1 << this.blockSHL;
		this.memory         = this.createByteArray(bytesMemory);
		this.size           = size;
		this.dataSize       = 
				new BigDecimal(bytesMemory)
					.divide(
						new BigDecimal(blockSize + 34L)
							.divide(new BigDecimal(blockSize), MathContext.DECIMAL128),
						MathContext.DECIMAL128
					).longValue();
		
		//bytesMemory/((blockSize + 34)/blockSize);
		this.mappingSize    = bytesMemory - this.dataSize;
		this.mappingOffset  = 0;
		this.dataOffset     = this.mappingSize;
		this.segmentMapping = new VirtualSegmentMapping(this.memory, this.mappingOffset, (int)(dataSize/blockSize), mappingSize);
		
		file.createNewFile();
		this.file           = new RandomAccessFile(file, "rw");
	}

	protected ByteArray createByteArray(long bytesMemory) {
		return new HeapByteArray(bytesMemory);
	}
	
	public long readLong(long offset) {
		return readNumber(offset, 8);
	}

	public int readInt(long offset) {
		return (int)readNumber(offset, 4);
	}

	public short readShort(long offset) {
		return (short)readNumber(offset, 2);
	}

	public byte readByte(long offset) {
		return (byte)readNumber(offset, 1);
	}

	public long readNumber(long vOffset, int bytes) {
		long[] val       = new long[] {0};
		byte[] dta       = new byte[bytes];
		long blockOffset = vOffset % blockSize;
		long segment     = vOffset - blockOffset;
		
		if(blockOffset - bytes < 0) {
			int r1 = (int)(blockSize - blockOffset);
			int r2 = bytes - r1;

			synchronized(segmentMapping) {
				long offset = getSegment(segment);
				memory.read(dataOffset + offset + blockOffset, dta, 0, r1);
			}				
			
			segment += blockSize;
			
			synchronized (segmentMapping) {
				long offset = getSegment(segment);
				memory.read(dataOffset + offset, dta, r1, r2);
			}
		}
		else {
			synchronized(segmentMapping) {
				long offset = getSegment(segment);
				memory.read(dataOffset + offset, dta, 0, bytes);
			}
		}
		
		UNSAFE.copyMemory(dta, BYTE_ARRAY_OFFSET, val, LONG_ARRAY_OFFSET, bytes);
		return val[0];
	}
	
	public long read(long srcOff, RandomAccessFile dest, long destOff, long len) throws IOException{

		if(destOff + len > dest.length()) {
			throw new IndexOutOfBoundsException((destOff + len) + " > " + dest.length());
		}
		
		long maxRead = size - srcOff;
		len = len > maxRead? maxRead : len;
		long total = len;
				
		long copy;
		long maxSegCopy;
		
		while(len > 0) {
			maxSegCopy = blockSize - (destOff % blockSize);
			copy = maxSegCopy > len? len : maxSegCopy;
			
			synchronized(segmentMapping) {
				long offset = getSegment(destOff - (destOff % blockSize));
				memory.read(dataOffset + srcOff, dest, destOff, (int)copy);
				
				Item item = segmentMapping.getItem(); 
				item.setNeedUpdate(offset >> blockSHL, true);
			}
			
			len     -= copy;
			srcOff  += copy;
			destOff += copy;
		}
		
		return total;	
	}
	
	public int read(long srcOff, byte[] dest, int destOff, int len) {

		if(destOff + len > dest.length) {
			throw new IndexOutOfBoundsException((destOff + len) + " > " + dest.length);
		}
		
		long maxRead = size - srcOff;
		len = (int)(len > maxRead? maxRead : len);
		int total = len;
		
		long copy;
		long maxSegCopy;
		
		while(len > 0) {
			maxSegCopy = blockSize - (destOff % blockSize);
			copy = maxSegCopy > len? len : maxSegCopy;
			
			synchronized(segmentMapping) {
				long offset = getSegment(destOff - (destOff % blockSize));
				memory.read(dataOffset + srcOff, dest, destOff, (int)copy);
				
				Item item = segmentMapping.getItem(); 
				item.setNeedUpdate(offset >> blockSHL, true);
			}
			
			len     -= copy;
			srcOff  += copy;
			destOff += copy;
		}
		
		return total;
	}

	public void writeLong(long offset, long value) {
		writeNumber(offset, value, 8);
	}

	public void writeInt(long offset, long value) {
		writeNumber(offset, value, 4);
	}

	public void writeShort(long offset, long value) {
		writeNumber(offset, value, 2);
	}

	public void writeByte(long offset, long value) {
		writeNumber(offset, value, 1);
	}
	
	private void writeNumber(long vOffset, long value, int bytes) {
		long[] val       = new long[] {value};
		byte[] dta       = new byte[bytes];
		
		UNSAFE.copyMemory(val, LONG_ARRAY_OFFSET, dta, BYTE_ARRAY_OFFSET, bytes);
		write(dta, 0, vOffset, bytes);
	}

	public void write(RandomAccessFile src, long srcOff, long destOff, long len) throws IOException{

		if(destOff + len > size) {
			throw new IndexOutOfBoundsException((destOff + len) + " > " + size);
		}
		
		long copy;
		long maxSegCopy;
		
		while(len > 0) {
			maxSegCopy = blockSize - (destOff % blockSize);
			copy = maxSegCopy > len? len : maxSegCopy;
			
			synchronized(segmentMapping) {
				long offset = getSegment(destOff - (destOff % blockSize));
				memory.write(src, srcOff, dataOffset + destOff, (int)copy);
				
				Item item = segmentMapping.getItem(); 
				item.setNeedUpdate(offset >> blockSHL, true);
			}
			
			len     -= copy;
			srcOff  += copy;
			destOff += copy;
		}
				
	}
	
	public void write(byte[] src, int srcOff, long destOff, int len) {

		if(destOff + len > size) {
			throw new IndexOutOfBoundsException((destOff + len) + " > " + size);
		}
		
		long copy;
		long maxSegCopy;
		
		while(len > 0) {
			maxSegCopy = blockSize - (destOff % blockSize);
			copy = maxSegCopy > len? len : maxSegCopy;
			
			synchronized(segmentMapping) {
				long offset = getSegment(destOff - (destOff % blockSize));
				memory.write(src, srcOff, dataOffset + destOff, (int)copy);
				
				Item item = segmentMapping.getItem(); 
				item.setNeedUpdate(offset >> blockSHL, true);
			}
			
			len     -= copy;
			srcOff  += copy;
			destOff += copy;
		}
		
	}

	public long size() {
		return size;
	}
	
	protected long getSegment(long vOffset) {
		
		long offset = segmentMapping.get(vOffset);
		
		if(offset == -1) {
			offset     = segmentMapping.allocSegment(vOffset);
			Item item  = segmentMapping.getItem(); 
			offset     = offset << blockSHL;
			reloadSegment(vOffset, offset, item);
		}
		else {
			offset = offset << blockSHL;
		}
		
		return offset;
	}
	
	protected RandomAccessFile file;
	
	protected void reloadSegment(long vOffset, long offset, Item item) {

		try {
			long index         = offset >> blockSHL;
			long oldVOffset    = item.getVOffset(index);
			boolean needUpdate = item.isNeedUpdate(index);
			
			if(needUpdate && oldVOffset != -1) {
				if(oldVOffset > file.length()) {
					file.setLength(oldVOffset + blockSize);
				}
				memory.read(offset + dataOffset, file, oldVOffset, blockSize);
			}
			
			if(vOffset < file.length()) {
				//if(vOffset > file.length()) {
				//	file.setLength(vOffset + blockSize);
				//}
				
				memory.write(file, vOffset, offset + dataOffset, blockSize);
			}
			
			item.setVOffset(index, vOffset);
			item.setNeedUpdate(index, false);
		}
		catch(Throwable e) {
			throw new IllegalStateException(e);
		}
		
	}

	private int createBlockSHL(long value) {
		return getRMSB(value);
	}
	
	private int getRMSB(long n){ 
        int position = 1; 
        int m = 1; 
  
        while ((n & m) == 0) { 
            m = m << 1; 
            position++; 
        } 
        return position; 
    }

}
