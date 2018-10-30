package calypte.buffer;

public class HeapByteArray implements ByteArray{

	private byte[][] data;

	private long size;
	
	public HeapByteArray(long size) {
		this.size   = size;
		int segs = (int) (size/Integer.MAX_VALUE);
		segs     += size % Integer.MAX_VALUE != 0? 1 : 0;

		this.data = new byte[segs][];

		int seg = 0;
		
		while(size > 0) {
			int len = (int) (size > Integer.MAX_VALUE? Integer.MAX_VALUE : size);
			data[seg++] = new byte[len];
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
		long value = 0;
		int len    = bytes;
		
		if(offset + len >= size) {
			throw new IndexOutOfBoundsException(offset + " >= " + size);
		}
		
		long maxRead = size - offset;
		len = (int)(len > maxRead? maxRead : len);
		
		int s = (int) (offset >> 32);
		int o = (int) (offset & 0xFFFF);
		
		int r1 = data[s].length - o;
		int r2 = len - r1;
		
		
		if(len > r1) {

			s++;
			
			for(int i=r2-1;i>=0;i--) {
				value = value << 8 | (data[s][i] & 0xFF);
			}

			s--;
			
			for(int i=data[s].length - 1;i>=o;i--) {
				value = value << 8 | (value |= data[s][i] & 0xFF);
			}
			
			return value;
		}
		else {
			for(int i=len - 1;i>=0;i--) {
				value = value << 8 | (data[s][o + i] & 0xFF);
			}
			
			return value;
		}
		
	}
	
	public int read(long offset, byte[] buf, int off, int len) {
		
		if(offset >= size) {
			return -1;
		}
		
		long maxRead = size - offset;
		len = (int)(len > maxRead? maxRead : len);
		
		int s = (int) (offset >> 32);
		int o = (int) (offset & 0xFFFF);
		
		int r1 = data[s].length - o;
		int r2 = len - r1;
		
		
		if(len > r1) {
			System.arraycopy(data[s    ], o, buf, off, r1);
			System.arraycopy(data[s + 1], 0, buf, off + r1, r2);
			return r1 + r2;
		}
		else {
			System.arraycopy(data[s], o, buf, off, len);
			return r1;
		}
		
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
		int len    = bytes;
		int shl    = 0;
		
		if(offset >= size) {
			throw new IndexOutOfBoundsException(offset + " >= " + size);
		}
		
		long maxRead = size - offset;
		len = (int)(len > maxRead? maxRead : len);
		
		int s = (int) (offset >> 32);
		int o = (int) (offset & 0xFFFF);
		
		int r1 = data[s].length - o;
		int r2 = len - r1;
		
		
		if(len > r1) {
			
			for(int i=0;i<r1;i++) {
				data[s][o + i] = (byte)(value >> (1 << shl) & 0xFF);
				value = value >> 8;
			}
			
			o += r1;
			s++;
			
			for(int i=0;i<r2;i++) {
				data[s][o + i] = (byte)(value >> (1 << shl) & 0xFF);
				value = value >> 8;
			}
			
		}
		else {
			
			for(int i=0;i<len;i++) {
				data[s][o + i] = (byte)(value & 0xFF);
				value = value >> 8;
			}
			
		}
		
	}	
	
	public void write(long offset, byte[] buf, int off, int len) {
		
		if(offset >= size) {
			throw new IndexOutOfBoundsException(offset + " >= " + size);
		}
		
		long maxRead = size - offset;
		len = (int)(len > maxRead? maxRead : len);
		
		int s = (int) (offset >> 32);
		int o = (int) (offset & 0xFFFF);
		
		int r1 = data[s].length - o;
		int r2 = len - r1;
		
		
		if(len > r1) {
			System.arraycopy(buf, off     , data[s    ], o, r1);
			System.arraycopy(buf, off + r1, data[s + 1], 0, r2);
		}
		else {
			System.arraycopy(buf, off, data[s], o, len);
		}

		
	}
	
}
