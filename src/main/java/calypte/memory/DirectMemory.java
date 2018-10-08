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

package calypte.memory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * @author Ribeiro
 *
 */
public class DirectMemory implements Memory{

	public RegionMemory alloc(long size) {
		return new DirectRegionMemory(new byte[(int)size]);
	}

	public void realloc(long size, RegionMemory region) {
		((DirectRegionMemory)region).buffer = new byte[(int)size];
	}

	public void alloc(long size, RegionMemory region) {
		((DirectRegionMemory)region).buffer = new byte[(int)size];
	}

	public void release(RegionMemory region) {
		((DirectRegionMemory)region).buffer = null;
	}

	private class DirectRegionMemory implements RegionMemory{

		private static final long serialVersionUID = -6421266271433887657L;
		
		byte[] buffer;
		
		public DirectRegionMemory(byte[] buffer) {
			this.buffer = buffer;
		}
		
		public long size() {
			return buffer.length;
		}

		public int read(long thisOff, byte[] buf, int off, int len) {
			int max = (int)(buffer.length - thisOff);
			max     = max > len? len : max;
			
			System.arraycopy(buffer, (int)thisOff, buf, off, max);
			thisOff += max;
			return max;
		}

		public long read(long thisOff, RegionMemory buf, long off, long len) {
			int max = (int)(buffer.length - thisOff);
			max     = (int)(max > len? len : max);
			
			System.arraycopy(buffer, (int)thisOff, ((DirectRegionMemory)buf).buffer, (int)off, max);
			thisOff += max;
			return max;
		}

		public void read(OutputStream out, int off, int len) throws IOException {
			byte[] tmp = new byte[2048];
			int read;
			int thisOff = off;
			int maxRead = len > tmp.length? tmp.length : len;
			while((read = read(thisOff, tmp, 0, maxRead)) > 0){
				out.write(tmp, 0, read);
				thisOff += read;
				len     -= read;
				maxRead  = len > tmp.length? tmp.length : len;
			}
		}

		public void write(long thisOff, byte[] buf, int off, int len) {
			int max = (int)(buffer.length - thisOff);
			max     = max > len? len : max;
			
			System.arraycopy(buf, (int)thisOff, buffer, off, max);
			thisOff += max;
		}

		public void write(long thisOff, RegionMemory buf, long off, long len) {
			int max = (int)(buffer.length - thisOff);
			max     = (int)(max > len? len : max);
			
			System.arraycopy(((DirectRegionMemory)buf).buffer, (int)thisOff, buffer, (int)off, max);
			thisOff += max;
		}

		public byte get(long off) {
			return buffer[(int)off];
		}
		
	}
}
