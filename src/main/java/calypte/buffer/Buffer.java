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
public class Buffer {
	
	private long firstFree;
	
	private ByteArray byteArray;
	
	private long lastOffset;
	
	private long blockSize;
	
	public Buffer(ByteArray byteArray, long blockSize) {
		this.blockSize  = blockSize;
		this.byteArray  = byteArray;
		this.firstFree  = -1;
		this.lastOffset = 0;
	}
	
	public synchronized long alloc() {
		
		if(firstFree != -1) {
			long offset = firstFree;
			firstFree = byteArray.readLong(this.firstFree);
			return offset;
		}
		
		long offset = lastOffset;
		lastOffset += blockSize;
		return offset;
		
	}
	
	public synchronized void release(long segment) {
		segment = segment - (segment % blockSize);
		
		if(firstFree == -1) {
			firstFree = segment;
		}
		else {
			byteArray.writeLong(segment, firstFree);
			firstFree = segment;
		}
		
	}
	
	public long readLong(long offset) {
		return byteArray.readLong(offset);
	}

	public int readInt(long offset) {
		return byteArray.readInt(offset);
	}

	public short readShort(long offset) {
		return byteArray.readShort(offset);
	}

	public byte readByte(long offset) {
		return byteArray.readByte(offset);
	}
	
	public int read(long srcOff, byte[] dest, int destOff, int len) {
		return byteArray.read(srcOff, dest, destOff, len);
	}

	public long read(long srcOff, RandomAccessFile dest, long destOff, long len) throws IOException{
		return byteArray.read(srcOff, dest, destOff, len);
	}
	
	public void writeLong(long offset, long value) {
		byteArray.writeLong(offset, value);
	}
	
	public void writeInt(long offset, long value) {
		byteArray.writeInt(offset, value);
	}

	public void writeShort(long offset, long value) {
		byteArray.writeShort(offset, value);
	}

	public void writeByte(long offset, long value) {
		byteArray.writeByte(offset, value);
	}
	
	public void write(byte[] src, int srcOff, long destOff, int len) {
		byteArray.write(src, srcOff, destOff, len);
	}

	public void write(RandomAccessFile src, long srcOff, long destOff, long len) throws IOException{
		byteArray.write(src, srcOff, destOff, len);
	}
	
	public long size() {
		return byteArray.size();
	}
	
}
