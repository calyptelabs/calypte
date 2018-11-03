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
	
	protected int blockBitDesc;
	
	protected int blockMask;
	
	public VirtualByteArray(long bytesMemory, long size, int blockSize, File file) throws IOException {
		this.blockBitDesc       = createBlockSHL(blockSize) - 1;
		this.blockSize      = 1 << this.blockBitDesc;
		this.blockMask      = this.blockSize - 1;
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
		this.segmentMapping = 
				new VirtualSegmentMapping(
					this.memory, 
					this.mappingOffset, 
					(int)(dataSize/blockSize + (dataSize % blockSize != 0? 1 : 0)), 
					mappingSize
				);
		
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
		
		long vOff = destOff & blockMask;
		long vSeg = destOff >> blockBitDesc;
		
		long maxCopy = size - srcOff;
		maxCopy = len > maxCopy? maxCopy : len;
		
		len = (int)maxCopy;
		
		long copy;
		long maxSegCopy;
		
		while(len > 0) {
			maxSegCopy = blockSize - vOff;
			copy       = maxSegCopy > len? len : maxSegCopy;
			
			synchronized(segmentMapping) {
				long offsetSeg = getSegment(vSeg);
				memory.read(dataOffset + offsetSeg + vOff, dest, destOff, (int)copy);
				
				Item item = segmentMapping.getItem(); 
				item.setNeedUpdate(offsetSeg >> blockBitDesc, true);
			}
			
			len     -= copy;
			srcOff  += copy;
			vOff     = 0;
			vSeg++;
		}
		
		return maxCopy;
	}
	
	public int read(long srcOff, byte[] dest, int destOff, int len) {

		if(destOff + len > dest.length) {
			throw new IndexOutOfBoundsException((destOff + len) + " > " + dest.length);
		}
		
		long vOff = destOff & blockMask;
		long vSeg = destOff >> blockBitDesc;
		
		long maxCopy = size - srcOff;
		maxCopy = len > maxCopy? maxCopy : len;
		
		len = (int)maxCopy;
		
		long copy;
		long maxSegCopy;
		
		while(len > 0) {
			maxSegCopy = blockSize - vOff;
			copy       = maxSegCopy > len? len : maxSegCopy;
			
			synchronized(segmentMapping) {
				long offsetSeg = getSegment(vSeg);
				memory.read(dataOffset + offsetSeg + vOff, dest, destOff, (int)copy);
				
				Item item = segmentMapping.getItem(); 
				item.setNeedUpdate(offsetSeg >> blockBitDesc, true);
			}
			
			len     -= copy;
			srcOff  += copy;
			vOff     = 0;
			vSeg++;
		}
		
		return (int)maxCopy;
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
		
		long vOff = destOff & blockMask;
		long vSeg = destOff >> blockBitDesc;
				;
		long copy;
		long maxSegCopy;
		
		while(len > 0) {
			maxSegCopy = blockSize - vOff;
			copy       = maxSegCopy > len? len : maxSegCopy;
			
			synchronized(segmentMapping) {
				long offsetSeg = getSegment(vSeg);
				memory.write(src, srcOff, dataOffset + offsetSeg + vOff, (int)copy);
				
				Item item = segmentMapping.getItem(); 
				item.setNeedUpdate(offsetSeg >> blockBitDesc, true);
			}
			
			len     -= copy;
			srcOff  += copy;
			vOff     = 0;
			vSeg++;
		}				
		
	}
	
	public void write(byte[] src, int srcOff, long destOff, int len) {

		if(destOff + len > size) {
			throw new IndexOutOfBoundsException((destOff + len) + " > " + size);
		}
		
		long vOff = destOff & blockMask;
		long vSeg = destOff >> blockBitDesc;

		long copy;
		long maxSegCopy;
		
		while(len > 0) {
			maxSegCopy = blockSize - vOff;
			copy       = maxSegCopy > len? len : maxSegCopy;
			
			synchronized(segmentMapping) {
				long offsetSeg = getSegment(vSeg);
				memory.write(src, srcOff, dataOffset + offsetSeg + vOff, (int)copy);
				
				Item item = segmentMapping.getItem(); 
				item.setNeedUpdate(offsetSeg >> blockBitDesc, true);
			}
			
			len     -= copy;
			srcOff  += copy;
			vOff     = 0;
			vSeg++;
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
			offset     = offset << blockBitDesc;
			reloadSegment(vOffset, offset, item);
		}
		else {
			offset = offset << blockBitDesc;
		}
		
		return offset;
	}
	
	protected RandomAccessFile file;
	
	protected void reloadSegment(long vOffset, long offset, Item item) {

		try {
			long index         = offset >> blockBitDesc;
			long oldVOffset    = item.getVOffset(index);
			boolean needUpdate = item.isNeedUpdate(index);
			
			if(needUpdate && oldVOffset != -1) {
				if(oldVOffset + blockSize > file.length()) {
					file.setLength(oldVOffset + blockSize);
				}
				memory.read(dataOffset + offset, file, oldVOffset, blockSize);
			}
			
			if(vOffset < file.length()) {
				memory.write(file, vOffset, dataOffset + offset, blockSize);
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
