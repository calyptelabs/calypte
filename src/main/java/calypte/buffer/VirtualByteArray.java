package calypte.buffer;

public class VirtualByteArray implements ByteArray{

	private long mappingOffset;
	
	private long mappingSize;

	private long dataSize;

	private long dataOffset;
	
	private ByteArray data;
	
	private int blockSize;
	
	private long size;
	
	public VirtualByteArray(long bytesMemory, long size, int blockSize) {
		this.blockSize = blockSize;
		this.data = new HeapByteArray(bytesMemory);
		this.size = size;
		
		this.dataSize = bytesMemory/((blockSize + 8)/blockSize);
		this.mappingSize = bytesMemory - this.dataSize;
		
		this.mappingOffset = 0;
		this.dataOffset = this.mappingSize;
	}

	public long readLong(long offset) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int readInt(long offset) {
		// TODO Auto-generated method stub
		return 0;
	}

	public short readShort(long offset) {
		// TODO Auto-generated method stub
		return 0;
	}

	public byte readByte(long offset) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int read(long offset, byte[] buf, int off, int len) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeLong(long offset, long value) {
		// TODO Auto-generated method stub
		
	}

	public void writeInt(long offset, long value) {
		// TODO Auto-generated method stub
		
	}

	public void writeShort(long offset, long value) {
		// TODO Auto-generated method stub
		
	}

	public void writeByte(long offset, long value) {
		// TODO Auto-generated method stub
		
	}

	public void write(long offset, byte[] buf, int off, int len) {
		// TODO Auto-generated method stub
		
	}

	public long size() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}
