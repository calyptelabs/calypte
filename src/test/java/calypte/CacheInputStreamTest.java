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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import calypte.BasicCache;
import calypte.Block;
import calypte.CacheInputStream;
import calypte.memory.Memory;
import calypte.memory.RegionMemory;
import calypte.memory.UnsafeMemory;
import junit.framework.TestCase;

/**
 * 
 * @author Ribeiro
 *
 */
@SuppressWarnings("resource")
public class CacheInputStreamTest extends TestCase{

	private Memory memory = new UnsafeMemory();
	
	private Random r      = new Random();
	
	public void testOneSegment() throws IOException{
		byte[] dta    = new byte[30];
		byte[] result = new byte[30];
		r.nextBytes(dta);

		RegionMemory r = memory.alloc(64);
		r.write(0, dta, 0, dta.length);
		Block b = new Block(0, 0, r, dta.length);
		
		CacheInputStream in = new CacheInputStream(null, null, new Block[]{b}, 1024);
		in.read(result, 0, result.length);
		assertTrue(Arrays.equals(dta, result));
	}
	
	public void testMultSegments() throws IOException{
		byte[] dta    = new byte[1024];
		byte[] result = new byte[1024];
		r.nextBytes(dta);

		List<Block> blocks = new ArrayList<Block>();
		int blockLen       = dta.length/4;
		int blocksSize     = dta.length/blockLen + (dta.length % blockLen != 0? 1 : 0);
		int maxLen         = dta.length;
		int off            = 0;
		for(int i=0;i<blocksSize;i++){
			int len = maxLen > blockLen? blockLen : maxLen;
			RegionMemory r = memory.alloc(blockLen);
			r.write(0, dta, off, len);
			Block b = new Block(0, i, r, len);
			blocks.add(b);
			
			off    += len;
			maxLen -= len;
		}
		
		CacheInputStream in = new CacheInputStream(null, null, blocks.toArray(new Block[]{}), 1024);
		in.read(result, 0, result.length);
		assertTrue(Arrays.equals(dta, result));
	}

	public void testMultWithIrregularBlockSize() throws IOException{
		byte[] dta    = new byte[1030];
		byte[] result = new byte[1030];
		r.nextBytes(dta);

		List<Block> blocks = new ArrayList<Block>();
		int blockLen       = dta.length/4;
		int blocksSize     = dta.length/blockLen + (dta.length % blockLen != 0? 1 : 0);
		int maxLen         = dta.length;
		int off            = 0;
		for(int i=0;i<blocksSize;i++){
			int len = maxLen > blockLen? blockLen : maxLen;
			RegionMemory r = memory.alloc(blockLen);
			r.write(0, dta, off, len);
			Block b = new Block(0, i, r, len);
			blocks.add(b);
			
			off    += len;
			maxLen -= len;
		}
		
		CacheInputStream in = new CacheInputStream(null, null, blocks.toArray(new Block[]{}), 1024);
		in.read(result, 0, result.length);
		assertTrue(Arrays.equals(dta, result));
	}

	public void testMultWithIrregularBlockSizeAndMicroBuffer() throws IOException{
		byte[] dta    = new byte[1030];
		r.nextBytes(dta);

		List<Block> blocks = new ArrayList<Block>();
		int blockLen       = dta.length/4;
		int blocksSize     = dta.length/blockLen + (dta.length % blockLen != 0? 1 : 0);
		int maxLen         = dta.length;
		int off            = 0;
		for(int i=0;i<blocksSize;i++){
			int len = maxLen > blockLen? blockLen : maxLen;
			RegionMemory r = memory.alloc(blockLen);
			r.write(0, dta, off, len);
			Block b = new Block(0, i, r, len);
			blocks.add(b);
			
			off    += len;
			maxLen -= len;
		}
		
		CacheInputStream in = new CacheInputStream(null, null, blocks.toArray(new Block[]{}), 1024);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] b = new byte[4];
		int l;
		while((l = in.read(b)) > 0){
			out.write(b, 0, l);
		}
		assertTrue(Arrays.equals(dta, out.toByteArray()));
	}

	public void testMultWithLastBlockOneByte() throws IOException{
		byte[] dta    = new byte[1025];
		r.nextBytes(dta);

		RegionMemory r = memory.alloc(1024);
		r.write(0, dta, 0, 1024);
		Block b1 = new Block(0, 0, r, 1024);

		r = memory.alloc(1);
		r.write(0, dta, 1024, 1);
		Block b2 = new Block(0, 1, r, 1);
		
		CacheInputStream in = new CacheInputStream(null, null, new Block[] {b1,b2}, 1025);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] b = new byte[4];
		int l;
		while((l = in.read(b)) > 0){
			out.write(b, 0, l);
		}
		
		assertTrue(Arrays.equals(dta, out.toByteArray()));
		
	}

	public void testMultWithLastBlocktwoBytes() throws IOException{
		byte[] dta    = new byte[1026];
		r.nextBytes(dta);

		RegionMemory r = memory.alloc(1024);
		r.write(0, dta, 0, 1024);
		Block b1 = new Block(0, 0, r, 1024);

		r = memory.alloc(2);
		r.write(0, dta, 1024, 2);
		Block b2 = new Block(0, 1, r, 2);
		
		CacheInputStream in = new CacheInputStream(null, null, new Block[] {b1,b2}, 1026);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] b = new byte[4];
		int l;
		while((l = in.read(b)) > 0){
			out.write(b, 0, l);
		}
		
		assertTrue(Arrays.equals(dta, out.toByteArray()));
		
	}

	public void testMultWithoutLastByte() throws IOException{
		byte[] dta    = new byte[2047];
		r.nextBytes(dta);

		RegionMemory r = memory.alloc(1024);
		r.write(0, dta, 0, 1024);
		Block b1 = new Block(0, 0, r, 1024);

		r = memory.alloc(1023);
		r.write(0, dta, 1024, 1023);
		Block b2 = new Block(0, 1, r, 1023);
		
		CacheInputStream in = new CacheInputStream(null, null, new Block[] {b1,b2}, 2047);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] b = new byte[4];
		int l;
		while((l = in.read(b)) > 0){
			out.write(b, 0, l);
		}
		
		assertTrue(Arrays.equals(dta, out.toByteArray()));
		
	}
	
}
