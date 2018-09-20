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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 
 * @author Ribeiro
 *
 */
public class HeapMemoryUtil {

	/**
	 * Blocos de dados.
	 */
	public static int segmentSize = 64;
	
	/**
	 * Blocos alocados e livres.
	 */
	private static BlockingQueue<byte[]> segments = new LinkedBlockingQueue<byte[]>();
	
	public static byte[][] alloc(long size){
		int quantity = (int)(size / HeapMemoryUtil.segmentSize);
		
		if((size % HeapMemoryUtil.segmentSize) > 0)
			quantity++;
		
		byte[][] allocSegs = new byte[quantity][];
		
		for(int i=0;i<quantity;i++){
			byte[] seg = allocSegment();
			allocSegs[i] = seg;
		}
		return allocSegs;
	}
	
	public static void free(byte[][] allocSegs){
		for(int i=0;i<allocSegs.length;i++){
			segments.add(allocSegs[i]);
		}
 	}
	
	private static byte[] allocSegment(){
		byte[] seg = segments.poll();
		if(seg == null)
			return new byte[segmentSize];
		else
			return seg;
	}
	
}
