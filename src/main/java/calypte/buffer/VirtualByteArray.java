package calypte.buffer;

public class VirtualByteArray implements ByteArray{

	private long size;
	
	private ByteArray memory;
	
	private VirtualSegmentMapping segmentMapping;
	
	private int blockSize;
	
	private long dataSize;
	
	private long mappingSize;
	
	public VirtualByteArray(long bytesMemory, long size, int blockSize) {
		this.blockSize      = blockSize;
		this.memory         = this.createByteArray(bytesMemory);
		this.size           = size;
		this.dataSize       = bytesMemory/((blockSize + 33)/blockSize);
		this.mappingSize    = bytesMemory - this.dataSize;
		this.segmentMapping = new VirtualSegmentMapping(this.memory, 0, (int)mappingSize);
	}

	protected ByteArray createByteArray(long bytesMemory) {
		return new HeapByteArray(bytesMemory);
	}
	
	public long readLong(long vOffset) {
		
		long offset = this.segmentMapping.get(vOffset);
		
		if(offset == -1) {
			offset = this.reloadSegment(vOffset);
		}
		
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
		return size;
	}
	
	protected long reloadSegment(long vOffset) {
		return -1;
	}

}
