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

/**
 * Gerencia a memória usada no cache.
 * 
 * @author Ribeiro
 *
 */
public class HeapMemory implements Memory{

	public RegionMemory alloc(long size) {
		byte[][] segs = HeapMemoryUtil.alloc(size);
		return new HeapRegionMemory(segs, (int)HeapMemoryUtil.segmentSize, (int)size);
	}

	public void realloc(long size, RegionMemory region){
		HeapRegionMemory r = (HeapRegionMemory)region;
		byte[][] segs      = r.segments;
		byte[][] newSegs   = HeapMemoryUtil.alloc(size);
		
		if(newSegs.length > segs.length){
			System.arraycopy(segs, 0, newSegs, 0, segs.length);
			HeapMemoryUtil.free(segs);
			r.segments    = newSegs;
			r.segmentSize = (int)HeapMemoryUtil.segmentSize;
		}
		else{
			System.arraycopy(segs, 0, newSegs, 0, newSegs.length);
			HeapMemoryUtil.free(segs);
		}
		
	}
	
	public void alloc(long size, RegionMemory region) {
		HeapRegionMemory r = (HeapRegionMemory)region;
		if(r.segments != null)
			throw new IllegalStateException();
		allocSegments(size, r);
	}
	
	public void release(RegionMemory region){
		HeapRegionMemory r = (HeapRegionMemory)region;
		if(r.segments == null)
			return;
		
		HeapMemoryUtil.free(r.segments);
		r.segments = null;
 	}
	
	private HeapRegionMemory allocSegments(long size, HeapRegionMemory region){
		byte[][] allocSegs = HeapMemoryUtil.alloc(size);
		region.length      = size;
		region.segments    = allocSegs;
		region.segmentSize = HeapMemoryUtil.segmentSize;
		return region;
	}
	

}
