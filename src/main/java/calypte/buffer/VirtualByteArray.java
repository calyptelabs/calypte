package calypte.buffer;

import java.lang.reflect.Field;

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
	
	private ByteArray memory;
	
	private VirtualSegmentMapping segmentMapping;
	
	private int blockSize;
	
	private long dataSize;
	
	private long mappingSize;
	
	private int blockSHL;
	
	public VirtualByteArray(long bytesMemory, long size, int blockSize) {
		this.blockSHL       = createBlockSHL(blockSize);
		this.blockSize      = 1 << this.blockSHL;
		this.memory         = this.createByteArray(bytesMemory);
		this.size           = size;
		this.dataSize       = bytesMemory/((blockSize + 34)/blockSize);
		this.mappingSize    = bytesMemory - this.dataSize;
		this.segmentMapping = new VirtualSegmentMapping(this.memory, 0, (int)(dataSize/blockSize), mappingSize);
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
				memory.read(offset + blockOffset, dta, 0, r1);
			}				
			
			segment += blockSize;
			
			synchronized (segmentMapping) {
				long offset = getSegment(segment);
				memory.read(offset, dta, r1, r2);
			}
		}
		else {
			synchronized(segmentMapping) {
				long offset = getSegment(segment);
				memory.read(offset, dta, 0, bytes);
			}
		}
		
		UNSAFE.copyMemory(dta, BYTE_ARRAY_OFFSET, val, LONG_ARRAY_OFFSET, bytes);
		return val[0];
	}
	
	public int read(long vOffset, byte[] buf, int off, int len) {
		long maxRead     = size - vOffset;
		maxRead          = (int)(len > maxRead? maxRead : len);
		int r            = 0;
		
		long blockRead;
		long maxBlockRead;
		long blockOffset;
		long segment;
		
		while(maxRead > 0) {
			blockOffset  = vOffset % blockSize;
			segment      = vOffset - blockOffset;
			maxBlockRead = blockSize - blockOffset;
			blockRead    = maxRead > maxBlockRead? maxBlockRead : maxRead;
			
			synchronized(segmentMapping) {
				long offset = getSegment(segment);
				memory.read(offset, buf, off, (int)blockRead);
			}
			
			r       += maxBlockRead;
			off     += maxBlockRead;
			vOffset += maxBlockRead;
			maxRead -= maxBlockRead;
			
		}

		return r;
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
		
		long blockOffset = vOffset % blockSize;
		long segment     = vOffset - blockOffset;
		
		if(blockOffset - bytes < 0) {
			int r1 = (int)(blockSize - blockOffset);
			int r2 = bytes - r1;

			synchronized(segmentMapping) {
				long offset = getSegment(segment);
				memory.write(offset + blockOffset, dta, 0, r1);
			}				
			
			segment += blockSize;
			
			synchronized (segmentMapping) {
				long offset = getSegment(segment);
				memory.write(offset, dta, r1, r2);
			}
		}
		else {
			synchronized(segmentMapping) {
				long offset = getSegment(segment);
				memory.write(offset + blockOffset, dta, 0, bytes);
			}
		}
		
	}

	public void write(long vOffset, byte[] buf, int off, int len) {
		long maxWrite = size - vOffset;
		maxWrite      = (int)(len > maxWrite? maxWrite : len);
		
		long blockWrite;
		long maxBlockWrite;
		long blockOffset;
		long segment;
		
		while(maxWrite > 0) {
			blockOffset  = vOffset % blockSize;
			segment      = vOffset - blockOffset;
			maxBlockWrite = blockSize - blockOffset;
			blockWrite    = maxWrite > maxBlockWrite? maxBlockWrite : maxWrite;
			
			synchronized(segmentMapping) {
				long offset = getSegment(segment);
				memory.write(offset + blockOffset, buf, off, (int)blockWrite);
			}
			
			off     += maxBlockWrite;
			vOffset += maxBlockWrite;
			maxWrite -= maxBlockWrite;
			
		}
		
	}

	public long size() {
		return size;
	}
	
	protected long getSegment(long vOffset) {
		
		long offset = segmentMapping.get(vOffset);
		
		if(offset == -1) {
			offset = segmentMapping.allocSegment(vOffset);
			offset = offset << blockSHL;
			reloadSegment(vOffset, offset);
		}
		else {
			offset = offset << blockSHL;
		}
		
		return offset;
	}
	
	protected void reloadSegment(long vOffset, long offset) {
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
