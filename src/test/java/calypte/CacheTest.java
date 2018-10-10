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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import calypte.CacheHandler.ResultFind;
import junit.framework.TestCase;

/**
 * 
 * @author Ribeiro
 *
 */
public class CacheTest extends TestCase{

	private static final String KEY    = "teste";

	private static final String VALUE  = "value";

	private static final String VALUE2 = "val";
	
	private Cache cache;
	
	public void setUp(){
		CalypteConfig config = new TestCalypteConfig();
		this.cache = new ConcurrentCache(config);
	}
	
	public void tearDown(){
		this.cache.destroy();
		this.cache = null;
		System.gc();
	}
	
	/* replace */
	
	public void testReplace() throws StorageException{
		TestCase.assertFalse(cache.replace(KEY, VALUE, 0, 0));
	}

	public void testReplaceSuccess() throws StorageException, RecoverException{
		cache.put(KEY, VALUE, 0, 0);
		TestCase.assertEquals(VALUE, (String)cache.get(KEY));
		TestCase.assertTrue(cache.replace(KEY, VALUE2, 0, 0));
		TestCase.assertEquals(VALUE2, (String)cache.get(KEY));
	}

	public void testReplaceStream() throws StorageException, IOException{
		TestCase.assertFalse(cache.replaceStream(KEY, CacheTestHelper.toStream(VALUE), 0, 0));
	}

	public void testReplaceStreamSuccess() throws StorageException, RecoverException, IOException, ClassNotFoundException{
		cache.putStream(KEY, CacheTestHelper.toStream(VALUE), 0, 0);
		TestCase.assertEquals(VALUE, (String)CacheTestHelper.toObject(cache.getStream(KEY)));
		TestCase.assertTrue(cache.replaceStream(KEY, CacheTestHelper.toStream(VALUE2), 0, 0));
		TestCase.assertEquals(VALUE2, (String)CacheTestHelper.toObject(cache.getStream(KEY)));
	}
	
	public void testReplaceExact() throws StorageException{
		TestCase.assertFalse(cache.replace(KEY, VALUE, VALUE2, 0, 0));
	}

	public void testReplaceExactSuccess() throws StorageException, RecoverException{
		cache.put(KEY, VALUE, 0, 0);
		TestCase.assertEquals(VALUE, (String)cache.get(KEY));
		TestCase.assertTrue(cache.replace(KEY, VALUE, VALUE2, 0, 0));
		TestCase.assertEquals(VALUE2, (String)cache.get(KEY));
	}

	/* putIfAbsent */
	
	public void testputIfAbsent() throws StorageException, RecoverException{
		TestCase.assertNull(cache.putIfAbsent(KEY, VALUE, 0, 0));
		TestCase.assertEquals(VALUE, (String)cache.get(KEY));
	}

	public void testputIfAbsentExistValue() throws StorageException, RecoverException{
		cache.put(KEY, VALUE, 0, 0);
		TestCase.assertEquals(VALUE, cache.putIfAbsent(KEY, VALUE2, 0, 0));
		TestCase.assertEquals(VALUE, (String)cache.get(KEY));
	}

	public void testputIfAbsentStream() throws StorageException, RecoverException, IOException{
		TestCase.assertNull(cache.putIfAbsentStream(KEY, CacheTestHelper.toStream(VALUE), 0, 0));
		TestCase.assertEquals(VALUE, (String)cache.get(KEY));
	}

	public void testputIfAbsentStreamExistValue() throws StorageException, RecoverException, IOException, ClassNotFoundException{
		cache.putStream(KEY, CacheTestHelper.toStream(VALUE), 0, 0);
		TestCase.assertEquals(VALUE, CacheTestHelper.toObject(cache.putIfAbsentStream(KEY, CacheTestHelper.toStream(VALUE2), 0, 0)));
		TestCase.assertEquals(VALUE, CacheTestHelper.toObject(cache.getStream(KEY)));
	}
	
	/* put */
	
	public void testPut() throws StorageException, RecoverException{
		TestCase.assertNull((String)cache.get(KEY));
		cache.put(KEY, VALUE, 0, 0);
		TestCase.assertEquals(VALUE, (String)cache.get(KEY));
	}

	/* get */
	
	public void testGet() throws StorageException, RecoverException{
		TestCase.assertNull((String)cache.get(KEY));
		cache.put(KEY, VALUE, 0, 0);
		TestCase.assertEquals(VALUE, (String)cache.get(KEY));
	}

	public void testGetOverride() throws StorageException, RecoverException{
		TestCase.assertNull((String)cache.get(KEY));
		cache.put(KEY, VALUE, 0, 0);
		TestCase.assertEquals(VALUE, (String)cache.get(KEY));
		cache.put(KEY, VALUE2, 0, 0);
		TestCase.assertEquals(VALUE2, (String)cache.get(KEY));
	}

	/* remove */
	
	public void testRemoveExact() throws StorageException, RecoverException{
		TestCase.assertNull((String)cache.get(KEY));
		TestCase.assertFalse(cache.remove(KEY, VALUE));
		
		cache.put(KEY, VALUE, 0, 0);
		
		TestCase.assertEquals(VALUE, (String)cache.get(KEY));
		
		TestCase.assertFalse(cache.remove(KEY, VALUE2));
		TestCase.assertTrue(cache.remove(KEY, VALUE));
	}

	public void testRemove() throws StorageException, RecoverException{
		TestCase.assertNull((String)cache.get(KEY));
		TestCase.assertFalse(cache.remove(KEY));
		
		cache.put(KEY, VALUE, 0, 0);
		
		TestCase.assertEquals(VALUE, (String)cache.get(KEY));
		
		TestCase.assertTrue(cache.remove(KEY));
	}

	/* timeToLive */
	
	public void testTimeToLive() throws InterruptedException{
		cache.put(KEY, VALUE, 1000, 0);
		assertEquals(cache.get(KEY), VALUE);
		Thread.sleep(800);
		assertEquals(cache.get(KEY), VALUE);
		Thread.sleep(400);
		assertNull(cache.get(KEY));
	}

	public void testTimeToLiveLessThanTimeToIdle() throws InterruptedException{
		cache.put(KEY, VALUE, 1000, 5000);
		assertEquals(cache.get(KEY), VALUE);
		Thread.sleep(1200);
		assertNull(cache.get(KEY));
	}

	public void testNegativeTimeToLive() throws InterruptedException{
		try{
			cache.put(KEY, VALUE, -1, 5000);
			fail();
		}
		catch(StorageException e){
			if(!e.getError().equals(CacheErrors.ERROR_1029)){
				fail();
			}
				
		}
	}

	/* TimeToIdle */
	
	public void testTimeToIdle() throws InterruptedException{
		cache.put(KEY, VALUE, 0, 1000);
		assertEquals(cache.get(KEY), VALUE);
		Thread.sleep(800);
		assertEquals(cache.get(KEY), VALUE);
		Thread.sleep(800);
		assertEquals(cache.get(KEY), VALUE);
		Thread.sleep(1200);
		assertNull(cache.get(KEY));
		
	}

	public void testTimeToIdleLessThanTimeToLive() throws InterruptedException{
		cache.put(KEY, VALUE, 20000, 1000);
		assertEquals(cache.get(KEY), VALUE);
		Thread.sleep(800);
		assertEquals(cache.get(KEY), VALUE);
		Thread.sleep(800);
		assertEquals(cache.get(KEY), VALUE);
		Thread.sleep(1200);
		assertNull(cache.get(KEY));
	}

	public void testNegativeTimeToIdle() throws InterruptedException{
		try{
			cache.put(KEY, VALUE, 0, -1);
			fail();
		}
		catch(StorageException e){
			if(!e.getError().equals(CacheErrors.ERROR_1028)){
				fail();
			}
				
		}
	}
	
    public void testswap() throws FileNotFoundException, IOException, ClassNotFoundException, InterruptedException{
    	
        int itens = 1000;
        
        for(int i=0;i<itens;i++){
            try{
                String key = String.valueOf(i) + "-INDEX-AJBK-";
                String value = key + "-INDEX-AJBK-";
                cache.put(key, value, 0, 0);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        for(int i=0;i<itens;i++){
            try{
                String key = String.valueOf(i) + "-INDEX-AJBK-";
                String value = key + "-INDEX-AJBK-";
                String cachedValue = (String) cache.get(key);
                assertEquals(value, cachedValue);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        
    }
    
    public void testFind() {

        int itens = 10;
        
        for(int i=0;i<itens;i++){
            try{
                String key = String.valueOf(i) + "-index-";
                String value = key + "-index-";
                cache.put(key, value, 0, 0);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        final Set<String> result = new HashSet<String>();
        
        ((ConcurrentCache)cache).cacheHandler.find(null, new ResultFind() {
			
			public void found(String key, CacheHandler cache) {
				result.add(key);
			}
			
		});
        
        for(int i=0;i<itens;i++){
            try{
                String key = String.valueOf(i) + "-index-";
                assertTrue(result.contains(key));
                
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        
    }
    
}
