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

import java.util.Arrays;
import java.util.Random;

import calypte.memory.Memory;
import calypte.memory.RegionMemory;
import calypte.memory.UnsafeMemory;
import junit.framework.TestCase;

/**
 * 
 * @author Ribeiro
 *
 */
public class UnsafeMemoryTest extends TestCase{

	private Memory memory = new UnsafeMemory();
	
	public void testAlloc(){
		RegionMemory r = memory.alloc(1024);
		byte[] b = new byte[1024];
		Random random = new Random();
		random.nextBytes(b);
		r.write(0, b, 0, b.length);
		
		byte[] b2 = new byte[1024];
		r.read(0, b2, 0, b2.length);
		assertTrue(Arrays.equals(b, b2));
	}
	
	public void testCopyAlloc(){
		RegionMemory r = memory.alloc(1024);
		byte[] b = new byte[1024];
		Random random = new Random();
		random.nextBytes(b);
		r.write(0, b, 0, b.length);
		
		byte[] b2 = new byte[1024];
		r.read(0, b2, 0, b2.length);
		assertTrue(Arrays.equals(b, b2));
	}

	public void testCopy2(){
		RegionMemory r = memory.alloc(1024);
		byte[] b = new byte[1024];
		Random random = new Random();
		random.nextBytes(b);
		r.write(10, b, 10, b.length-10);
		
		byte[] b2 = new byte[1024];
		r.read(10, b2, 10, b2.length-10);
		
		for(int i= 0;i<10;i++){
			b[i] = 0;
		}
		
		assertTrue(Arrays.equals(b, b2));
	}
	
}
