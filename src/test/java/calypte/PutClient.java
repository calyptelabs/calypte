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

import java.io.ByteArrayInputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import calypte.Cache;

/**
 * 
 * @author Ribeiro
 *
 */
public class PutClient implements Runnable{
	
	private Cache cache;
	
	private AtomicInteger keyCount;
	
	private Throwable error;
	
	private int maxOperations;
	
	private CountDownLatch countDown;
	
	private byte[] value;
	
	public PutClient(Cache cache, 
			AtomicInteger keyCount, int maxOperations, CountDownLatch countDown, byte[] value){
		this.keyCount      = keyCount;
		this.maxOperations = maxOperations;
		this.cache         = cache;
		this.value         = value;
		this.countDown     = countDown;
	}
	
	public void run(){
		for(int i=0;i<maxOperations;i++){
			try{
				long key      = keyCount.getAndIncrement();
				String strKey = Long.toString(key, Character.MAX_RADIX);
				cache.putStream(strKey, new ByteArrayInputStream(value), 0, 0);
			}
			catch(Throwable e){
				e.printStackTrace();
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
