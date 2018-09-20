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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import calypte.CalypteConfig;
import calypte.BasicCache;
import calypte.Cache;
import calypte.ConcurrentCache;
import calypte.tx.CacheTransactionManager;
import calypte.tx.CacheTransactionManagerImp;
import junit.framework.TestCase;

/**
 *
 * @author Ribeiro
 */
public class StressTXCacheTest extends TestCase{
    
	private Cache cache;
	
	public void setUp(){
		CalypteConfig config        = new TestCalypteConfig();
		CacheTransactionManager txm = new CacheTransactionManagerImp("./tx", TimeUnit.SECONDS.toMillis(30));
		BasicCache ntxCache         = new ConcurrentCache(config);
		cache                       = ntxCache.getTXCache(txm);
	}
	
	public void tearDown(){
		this.cache.destroy();
		this.cache = null;
		System.gc();
	}
	
	public void testPut() throws InterruptedException{
		int totalClients  = 300;
		int maxOperations = 10;
		CountDownLatch countDown = new CountDownLatch(totalClients);
		AtomicInteger keyCount   = new AtomicInteger();
		Thread[] clients         = new Thread[totalClients];
		byte[] value             = new byte[1024];
		
		for(int i=0;i<totalClients;i++){
			clients[i] = new Thread(new PutClient(cache, keyCount, maxOperations, countDown, value));
		}
		
		long time = System.nanoTime();
		for(Thread c: clients){
			c.start();
		}
		countDown.await();
		time = System.nanoTime() - time;
		
		double op     = totalClients*maxOperations;
		double timeOp = time / op;
		double opsSec = 1000000000 / timeOp;
		
		System.out.println("operations: " + op + ", time: " + time + " nano, ops: " + op + ", ops/Sec: " + + opsSec );
	}

}
