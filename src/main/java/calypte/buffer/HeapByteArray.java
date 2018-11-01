package calypte.buffer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class HeapByteArray implements ByteArray{

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
	
	private byte[][] data;

	private long size;
	
	public HeapByteArray(long size) {
		this(size, Integer.MAX_VALUE);
	}
	
	public HeapByteArray(long size, int block) {
		
		if(block < 8) {
			throw new IllegalStateException("block < 8");
		}
		
		this.size   = size;
		int segs = (int) (size/block);
		segs     += size % block != 0? 1 : 0;

		this.data = new byte[segs][];

		int seg = 0;
		
		while(size > 0) {
			int len = (int) (size > block? block : size);
			data[seg++] = new byte[len < 8? 8 : len]; // último bloco tem que ter no mínimo 8 bytes 
			size -= len;
		}
		
	}
	
	public long size() {
		return size;
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
	
	private long readNumber(long offset, int bytes) {
		int len    = bytes;
		
		if(offset + bytes > size) {
			throw new IndexOutOfBoundsException(offset + bytes + " > " + size);
		}
		
		long maxRead = size - offset;
		len = (int)(len > maxRead? maxRead : len);
		
		int s  = (int)(offset >> 32);
		long o = offset & 0xFFFFFFFFL;
		
		long r1 = data[s].length - o;
		long r2 = len - r1;
		
		
		if(len > r1) {
			switch (bytes) {
			case 1:
				throw new IllegalStateException("bug");

			case 2:
				return 
					(UNSAFE.getByte(data[s    ], BYTE_ARRAY_OFFSET + o) << 8 | 
					UNSAFE.getByte(data[s + 1], BYTE_ARRAY_OFFSET    )) & 0xffff;

			case 4:
				return 
					(UNSAFE.getInt(data[s    ], BYTE_ARRAY_OFFSET + data[s].length - 4) << ((4 - r1) << 3) |
					UNSAFE.getInt(data[s + 1], BYTE_ARRAY_OFFSET                     ) >> ((4 - r2) << 3)) & 0xffffffff;

			case 8:
				return 
					(UNSAFE.getInt(data[s    ], BYTE_ARRAY_OFFSET + data[s].length - 8) << ((8 - r1) << 3) |
					UNSAFE.getInt(data[s + 1], BYTE_ARRAY_OFFSET                     ) >> ((8 - r2) << 3)) & 0xffffffffffffffffL;

			default:
				throw new IllegalStateException("bytes: " + bytes);
			}			
		}
		else {
			switch (bytes) {
			case 1:
				return UNSAFE.getByte(data[s], BYTE_ARRAY_OFFSET + o);

			case 2:
				return UNSAFE.getShort(data[s], BYTE_ARRAY_OFFSET + o);

			case 4:
				return UNSAFE.getInt(data[s], BYTE_ARRAY_OFFSET + o);

			case 8:
				return UNSAFE.getLong(data[s], BYTE_ARRAY_OFFSET + o);

			default:
				throw new IllegalStateException("bytes: " + bytes);
			}
			
		}
		
	}
	
	public int read(long srcOff, byte[] dest, int destOff, int len) {
		
		if(destOff + len > dest.length) {
			throw new IndexOutOfBoundsException((destOff + len) + " > " + dest.length);
		}

		long maxRead = size - srcOff;
		len = (int)(len > maxRead? maxRead : len);
		long total = len;
				
		int seg  = (int)(srcOff >> 32);
		long off = srcOff & 0xFFFFFFFFL;
		
		long maxSegCopy = data[seg].length - off;
		long copy       = maxSegCopy > len? len : maxSegCopy;
		UNSAFE.copyMemory(data[seg], BYTE_ARRAY_OFFSET + off, dest, BYTE_ARRAY_OFFSET + destOff, copy);

		len -= copy;
		off  = 0;
		
		while(len > 0) {
			
			destOff += copy;
			seg++;
			
			maxSegCopy = data[seg].length - off;
			copy       = maxSegCopy > len? len : maxSegCopy;
			
			UNSAFE.copyMemory(data[seg], BYTE_ARRAY_OFFSET + off, dest, BYTE_ARRAY_OFFSET + destOff, copy);
			
			len -= copy;
			
		}
		
		return (int)total;
	}

	public long read(long srcOff, RandomAccessFile dest, long destOff, long len) throws IOException {
		
		if(destOff + len > dest.length()) {
			throw new IndexOutOfBoundsException((destOff + len) + " > " + dest.length());
		}

		long maxRead = size - srcOff;
		len = (int)(len > maxRead? maxRead : len);
		long total = len;
				
		int seg  = (int)(srcOff >> 32);
		long off = srcOff & 0xFFFFFFFFL;
		
		long maxSegCopy = data[seg].length - off;
		long copy       = maxSegCopy > len? len : maxSegCopy;
		dest.seek(destOff);
		dest.write(data[seg], (int)off, (int)copy);

		len -= copy;
		off  = 0;
		
		while(len > 0) {
			
			destOff += copy;
			seg++;
			
			maxSegCopy = data[seg].length - off;
			copy       = maxSegCopy > len? len : maxSegCopy;
			
			dest.seek(destOff);
			dest.write(data[seg], (int)off, (int)copy);
			
			len -= copy;
			
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
	
	private void writeNumber(long offset, long value, int bytes) {
		
		if(offset + bytes > size) {
			throw new IndexOutOfBoundsException(offset + bytes + " > " + size);
		}
		
		long maxRead = size - offset;
		int len = (int)(bytes > maxRead? maxRead : bytes);

		int s  = (int)(offset >> 32);
		long o = offset & 0xFFFFFFFFL;
		
		long r1 = data[s].length - o;
		long r2 = len - r1;
		
		
		if(len > r1) {
			long[] v = new long[] {value};
			UNSAFE.copyMemory(v, LONG_ARRAY_OFFSET     , data[s    ], BYTE_ARRAY_OFFSET + o, r1);
			UNSAFE.copyMemory(v, LONG_ARRAY_OFFSET + r1, data[s + 1], BYTE_ARRAY_OFFSET    , r2);
		}
		else {
			switch (bytes) {
			case 1:
				UNSAFE.putByte(data[s], BYTE_ARRAY_OFFSET + o, (byte)value);
				break;
				
			case 2:
				UNSAFE.putShort(data[s], BYTE_ARRAY_OFFSET + o, (short)value);
				break;

			case 4:
				UNSAFE.putInt(data[s], BYTE_ARRAY_OFFSET + o, (int)value);
				break;

			case 8:
				UNSAFE.putLong(data[s], BYTE_ARRAY_OFFSET + o, value);
				break;

			default:
				throw new IllegalStateException("bytes: " + bytes);
			}
		}
		
	}
	
	public void write(byte[] src, int srcOff, long destOff, int len) {
		
		if(destOff + len > size) {
			throw new IndexOutOfBoundsException((destOff + len) + " > " + size);
		}

		long maxRead = src.length - srcOff;
		len = (int)(len > maxRead? maxRead : len);
				
		int seg  = (int)(destOff >> 32);
		long off = destOff & 0xFFFFFFFFL;
		
		long maxSegCopy = data[seg].length - off;
		long copy       = maxSegCopy > len? len : maxSegCopy;

		UNSAFE.copyMemory(src, BYTE_ARRAY_OFFSET + srcOff, data[seg], BYTE_ARRAY_OFFSET + off, copy);
		
		len -= copy;
		off = 0;
		
		while(len > 0) {
			
			srcOff += copy;
			seg++;
			
			maxSegCopy = data[seg].length - off;
			copy = maxSegCopy > len? len : maxSegCopy;
			
			UNSAFE.copyMemory(src, BYTE_ARRAY_OFFSET + srcOff, data[seg], BYTE_ARRAY_OFFSET + off, copy);
			
			len -= copy;
			
		}

	}
	
	public void write(RandomAccessFile src, long srcOff, long destOff, long len) throws IOException{
		
		if(destOff + len > size) {
			throw new IndexOutOfBoundsException((destOff + len) + " > " + size);
		}

		long maxRead = src.length() - srcOff;
		len = (int)(len > maxRead? maxRead : len);
				
		int seg  = (int)(destOff >> 32);
		long off = destOff & 0xFFFFFFFFL;
		
		long maxSegCopy = data[seg].length - off;
		long copy       = maxSegCopy > len? len : maxSegCopy;

		src.seek(srcOff);
		src.read(data[seg], (int)off, (int)copy);
		
		len -= copy;
		off = 0;
		
		while(len > 0) {
			
			srcOff += copy;
			seg++;
			
			maxSegCopy = data[seg].length - off;
			copy = maxSegCopy > len? len : maxSegCopy;
			
			src.seek(srcOff);
			src.read(data[seg], (int)off, (int)copy);
			
			len -= copy;
			
		}

	}
	
	
}
