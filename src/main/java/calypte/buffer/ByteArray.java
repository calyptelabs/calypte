package calypte.buffer;

import java.io.IOException;
import java.io.RandomAccessFile;

public interface ByteArray {

	long readLong(long offset);

	int readInt(long offset);

	short readShort(long offset);

	byte readByte(long offset);
	
	int read(long srcOff, byte[] dest, int destOff, int len);

	long read(long srcOff, RandomAccessFile dest, long destOff, long len) throws IOException;
	
	void writeLong(long offset, long value);

	void writeInt(long offset, long value);

	void writeShort(long offset, long value);

	void writeByte(long offset, long value);
	
	void write(byte[] src, int srcOff, long destOff, int len);

	void write(RandomAccessFile src, long srcOff, long destOff, long len) throws IOException;
	
	long size();
	
}
