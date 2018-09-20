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
public class UnsafeMemory implements Memory{

	public UnsafeMemory(){
	}
	
	public RegionMemory alloc(long size){
		long address = UnsafeMemoryUtil.alloc(size);
		return new UnsafeRegionMemory(address, size);
	}

	public void realloc(long size, RegionMemory region) {
		UnsafeRegionMemory r = (UnsafeRegionMemory)region;
		long address         = UnsafeMemoryUtil.realloc(r.address, size);
		r.address            = address;
	}
	
	public void alloc(long size, RegionMemory region){
		UnsafeRegionMemory r = (UnsafeRegionMemory)region;
		if(r.address != null)
			throw new IllegalStateException();
		
		long address = UnsafeMemoryUtil.alloc(size);
		r.address = address;
	}
	
	public void release(RegionMemory region){
		synchronized(region){
			UnsafeRegionMemory r = (UnsafeRegionMemory)region;
			if(r.address == null)
				return;
			UnsafeMemoryUtil.free(r.address);
			r.address = null;
		}
 	}

}
