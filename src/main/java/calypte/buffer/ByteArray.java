/*
 * Calypte http://calypte.uoutec.com.br/
 * Copyright (C) 2018 UoUTec. (calypte@uoutec.com.br)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package calypte.buffer;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 
 * @author Ribeiro
 *
 */
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
