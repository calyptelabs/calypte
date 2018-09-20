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

import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import calypte.Cache;

/**
 * 
 * @author Ribeiro
 *
 */
public class GetClient implements Runnable{
	
	private static final Random random = new Random();
	
	private Cache cache;
	
	private AtomicInteger keyCount;
	
	private byte[] buffer;
	
	private Throwable error;
	
	private int maxOperations;
	
	private CountDownLatch countDown;
	
	public GetClient(Cache cache, 
			AtomicInteger keyCount, int maxOperations, CountDownLatch countDown){
		this.keyCount      = keyCount;
		this.maxOperations = maxOperations;
		this.buffer        = new byte[1024];
		this.cache         = cache;
	}
	
	@SuppressWarnings("unused")
	public void run(){
		for(int i=0;i<maxOperations;i++){
			try{
				long key       = random.nextInt(keyCount.get());
				String strKey  = Long.toString(key, Character.MAX_RADIX);
				InputStream in = cache.getStream(strKey);
				if(in != null){
					int l;
					try{
						while((l = in.read(buffer, 0, buffer.length)) != -1);
					}
					finally{
						in.close();
					}
				}
			}
			catch(Throwable e){
				error = e;
				break;
			}
		}
		
		countDown.countDown();
		
	}

	public Throwable getError() {
		return error;
	}
	
}
