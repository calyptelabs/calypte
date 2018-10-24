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
	
	public HeapBuffer(long size, int blockSize) {
		int maxOff = (Integer.MAX_VALUE/blockSize)*blockSize;
		int segs  = (int) (size/maxOff);
		segs     += size % maxOff != 0? 1 : 0;

		this.data = new int[segs][];
		
		int seg = 0;
		
		while(size > 0) {
			int len = (int) (size > maxOff? maxOff : size);
			data[seg++] = new int[len];
			size -= len;
		}
		
	}
	
	public int alloc() {
		return -1;
	}
	
	public int read(int segment, int offset, byte[] buf, int off, int len) {
		return -1;
	}
	
	public void write(int segment, int offset, byte[] buf, int off, int len) {
	}
	
	public void release(int segment) {
	}
	
}
