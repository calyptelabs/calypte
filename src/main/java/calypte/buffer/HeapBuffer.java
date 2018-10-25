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

public class HeapBuffer implements Buffer{

	private int[][] data;
	
	private int blockSize;
	
	private SwapBuffer swap;
	
	private int maxRegionOffset;
	
	public HeapBuffer(long size, int blockSize) {
		this.maxRegionOffset  = (Integer.MAX_VALUE/blockSize)*blockSize;
		int regions = (int) (size/maxRegionOffset);
		regions     += size % maxRegionOffset != 0? 1 : 0;

		this.data = new int[regions][];
		this.blockSize = blockSize;

		int seg = 0;
		
		while(size > 0) {
			int len = (int) (size > maxRegionOffset? maxRegionOffset : size);
			data[seg++] = new int[len];
			size -= len;
		}
		
	}
	
	public long alloc() {
		return -1;
	}
	
	public int read(int offset, byte[] buf, int off, int len) {
		int maxRead = blockSize - offset;
		int read = len > maxRead? maxRead : len;
		readVOffset(offset, buf, off, read); 
		return read;
	}
	
	public void readVOffset(long vOffset, byte[] dest, int off, int len) {
		long offset = reloadVOffset(vOffset);
		int segreg  = (int)(offset / maxRegionOffset);
		int offreg  = (int)(offset % maxRegionOffset);
		System.arraycopy(data[segreg], offreg, dest, off, len);
	}
	
	public void write(int segment, int offset, byte[] buf, int off, int len) {
		int maxRead = blockSize - offset;
		int read = len > maxRead? maxRead : len;
		writeVOffset(offset, buf, off, read); 
	}

	public void writeVOffset(long vOffset, byte[] dest, int off, int len) {
		long offset = reloadVOffset(vOffset);
		int segreg  = (int)(offset / maxRegionOffset);
		int offreg  = (int)(offset % maxRegionOffset);
		System.arraycopy(dest, off, data[segreg], offreg, len);
	}
	
	public void release(int segment) {
	}

	private long reloadVOffset(long offset) {
		return -1;
	}
	
}
