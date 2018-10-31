package calypte.buffer;

import java.io.RandomAccessFile;

public interface ByteArray {

	long readLong(long offset);

	int readInt(long offset);

	short readShort(long offset);

	byte readByte(long offset);
	
	int read(long offset, byte[] buf, int off, int len);

	int read(RandomAccessFile src, long srcOffset, long destOffset, int len);
	
	void writeLong(long offset, long value);

	void writeInt(long offset, long value);

	void writeShort(long offset, long value);

	void writeByte(long offset, long value);
	
	void write(long offset, byte[] buf, int off, int len);

	void write(long srcOffset, RandomAccessFile dest, long destOffset, int len);
	
	long size();
	
}
