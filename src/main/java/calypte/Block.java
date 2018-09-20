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

package calypte;

import java.io.Serializable;

import calypte.memory.RegionMemory;

/**
 * 
 * @author Ribeiro
 *
 */
@SuppressWarnings("serial")
public class Block 
    implements Serializable{

    public long id;

    public int segment;
    
    public int length;
    
    public long nextBlock;
    
    public RegionMemory buffer;
    
    public Block(long id, int segment, RegionMemory data, int length){
        this.id        = id;
        this.segment   = segment;
        this.buffer    = data;
        this.length    = length;
        this.nextBlock = -1;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + segment;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Block other = (Block) obj;
		if (id != other.id)
			return false;
		if (segment != other.segment)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Block [id=" + id + ", segment=" + segment + ", length="
				+ length + ", nextBlock=" + nextBlock + "]";
	}
    
}
