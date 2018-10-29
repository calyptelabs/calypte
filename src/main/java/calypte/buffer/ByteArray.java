package calypte.buffer;

public interface ByteArray {

	long readLong(long offset);

	int readInt(long offset);

	short readShort(long offset);

	byte readByte(long offset);
	
	int read(long offset, byte[] buf, int off, int len);

	void writeLong(long offset, long value);

	void writeInt(long offset, long value);

	void writeShort(long offset, long value);

	void writeByte(long offset, long value);
	
	void write(long offset, byte[] buf, int off, int len);
	
	long size();
	
}
