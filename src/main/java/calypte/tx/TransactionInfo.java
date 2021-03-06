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

package calypte.tx;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import calypte.CalypteConfig;
import calypte.CacheErrors;
import calypte.CacheException;
import calypte.CacheHandler;
import calypte.DataMap;
import calypte.RecoverException;
import calypte.StorageException;
import calypte.collections.MapReferenceCollection.Find;

/**
 * 
 * @author Ribeiro
 *
 */
public class TransactionInfo implements TransactionCacheHandler {

	private static final long serialVersionUID = 3758041685386590737L;
	
	private Serializable id;
	
	private CacheTransactionManager manager;
	
	private CacheHandler cache;
	
	/**
	 * Contém as chaves gerenciadas pela transação.
	 */
	private Set<String> managed;
	
	private Map<String, DataMap> cacheItemMetadata;
	
	private Map<String, DataMap> originalMetadata;
	
	private long timeout;
	
	public TransactionInfo(Serializable id, CacheTransactionManager manager, CacheHandler cache, long timeout){
		this.id                = id;
		this.cache             = cache;
		this.manager           = manager;
		this.managed           = new HashSet<String>();
		this.cacheItemMetadata = new HashMap<String, DataMap>();
		this.originalMetadata  = new HashMap<String, DataMap>();
		this.timeout           = timeout;
	}
	
	public void find(Find<DataMap> result) {
		cache.find(result);
	}
	
	/* métodos de armazenamento */
	
	public boolean replaceStream(String key, InputStream inputData, long timeToLive, long timeToIdle) throws StorageException{

		try{
			DataMap dta = getEntity(manager, cache, key, true);
			
			if(dta != null && !dta.isDead(getCreationTime())){
				putEntity(manager, cache, key, dta, inputData, timeToLive, timeToIdle);
				return true;
			}
			else
				return false;
		}
		catch(CacheException e){
			throw new StorageException(e, e.getError(), e.getParams());
		}
		catch(Throwable e){
			throw new StorageException(e, CacheErrors.ERROR_1020);
		}
		
	}
	
	public InputStream putIfAbsentStream(String key, InputStream inputData, long timeToLive, long timeToIdle) throws StorageException{
		
		try{
			DataMap dta = getEntity(manager, cache, key, true);
			
			if(dta == null || dta.isDead(getCreationTime())){
				putEntity(manager, cache, key, dta, inputData, timeToLive, timeToIdle);
				return null;
			}
			
			return cache.getStream(key, dta);
		}
		catch(CacheException e){
			throw new StorageException(e, e.getError(), e.getParams());
		}
		catch(Throwable e){
			throw new StorageException(e, CacheErrors.ERROR_1020);
		}		
	}
	
    public boolean putStream(String key, InputStream inputData, long timeToLive, long timeToIdle) 
    		throws StorageException {

    	try{
    		DataMap oldMap = getEntity(manager, cache, key, true);
    		putEntity(manager, cache, key, oldMap, inputData, timeToLive, timeToIdle);
    		return oldMap != null && !oldMap.isDead(getCreationTime());
    	}
		catch(CacheException e){
			throw new StorageException(e, e.getError(), e.getParams());
		}
		catch(Throwable e){
			throw new StorageException(e, CacheErrors.ERROR_1020);
		}
    }
	
	/* métodos de coleta*/
	
	public InputStream getStream(String key) throws RecoverException {
		return getStream(key, false);
	}
    
    public InputStream getStream(String key, boolean forUpdate) throws RecoverException {
    	
    	try{
			DataMap dta = getEntity(manager, cache, key, forUpdate);
			return dta == null || dta.isDead(getCreationTime())? 
					null : 
					cache.getStream(key, dta);
    	}
    	catch(RecoverException e){
    		throw e;
    	}
		catch(CacheException e){
			throw new StorageException(e, e.getError(), e.getParams());
		}
		catch(Throwable e){
			throw new StorageException(e, CacheErrors.ERROR_1021);
		}
    }

	public InputStream getStream(String key, DataMap map)
			throws RecoverException {
    	throw new UnsupportedOperationException();
	}
    
    /* métodos de remoção */
    
	public boolean removeIfInvalid(String key) throws StorageException {
		return cache.removeIfInvalid(key);
	}
	
    public boolean removeStream(String key) throws StorageException{
    	
    	try{
    		DataMap o = getEntity(manager, cache, key, true);
    		putEntity(manager, cache, key, o, null, 0, 0);
    		return o != null;
    	}
		catch(CacheException e){
			throw new StorageException(e, e.getError(), e.getParams());
		}
		catch(Throwable e){
			throw new StorageException(e, CacheErrors.ERROR_1021);
		}
    }
	
	public boolean containsKey(String key) {
		if(cacheItemMetadata.containsKey(key)){
			return cacheItemMetadata.get(key) != null;
		}
		else{
			return cache.containsKey(key);
		}
	}

	public DataMap getPointer(String key) throws RecoverException {
		throw new UnsupportedOperationException();
	}

	public void setPointer(String key, DataMap newDta) throws RecoverException {
		throw new UnsupportedOperationException();
	}

	public boolean replacePointer(String key, DataMap originalDta,
			DataMap newDta) throws RecoverException {
		throw new UnsupportedOperationException();
	}

	public void remove(String key, DataMap data) {
		throw new UnsupportedOperationException();
	}

	public void releaseSegments(DataMap map) {
		throw new UnsupportedOperationException();
	}

	public void putData(DataMap map, InputStream inputData)
			throws StorageException, InterruptedException {
		throw new UnsupportedOperationException();
	}

	public long getNextModCount() {
		return cache.getNextModCount();
	}

	public CalypteConfig getConfig() {
		return cache.getConfig();
	}

	public long getCountRead() {
		return cache.getCountRead();
	}

	public long getCountWrite() {
		return cache.getCountWrite();
	}

	public long getCountRemoved() {
		return cache.getCountRemoved();
	}

	public long getCountReadData() {
		return cache.getCountReadData();
	}

	public long getCountWriteData() {
		return cache.getCountWriteData();
	}

	public long getCountRemovedData() {
		return cache.getCountRemovedData();
	}

	public boolean isDeleteOnExit() {
		return cache.isDeleteOnExit();
	}

	public void setDeleteOnExit(boolean deleteOnExit) {
		cache.setDeleteOnExit(deleteOnExit);
	}

	public int getMaxKeySize() {
		return cache.getMaxKeySize();
	}
	
	public long size() {
		return cache.size();
	}

    public long getCreationTime() {
    	return cache.getCreationTime();
    }
	
	public boolean isEmpty() {
		return cache.isEmpty();
	}

	public void clear() {
		cache.clear();
	}

	public void destroy() {
		cache.destroy();
	}
    
    /*métodos de manipulação*/
    
	public void rollback(CacheHandler cache) throws StorageException, RecoverException {
		
		for(Entry<String,DataMap> e: originalMetadata.entrySet()){
			
			DataMap dta = cacheItemMetadata.get(e.getKey());
			
			if(e.getValue() == null){
				if(dta != null){
					cache.remove(e.getKey(), dta);
				}
			}
			else{
				if(dta != null){
					cache.replacePointer(e.getKey(), dta, e.getValue());
					cache.releaseSegments(dta);
				}
				else{
					cache.setPointer(e.getKey(), e.getValue());
				}
			}
		}
		
	}
	
	public void commit(CacheHandler cache) throws RecoverException, StorageException {
		for(Entry<String,DataMap> e: cacheItemMetadata.entrySet()){
			
			DataMap dta = originalMetadata.get(e.getKey());
			
			if(e.getValue() == null){
				if(dta != null){
					cache.remove(e.getKey(), dta);
				}
			}
			else{
				if(dta != null){
					cache.replacePointer(e.getKey(), dta, e.getValue());
					cache.releaseSegments(dta);
				}
				else{
					cache.setPointer(e.getKey(), e.getValue());
				}
			}
		}
		
	}
	
	public void close(CacheHandler cache) throws TransactionException{
		managed.clear();
		cacheItemMetadata.clear();
		originalMetadata.clear();
	}
	
    /* métodos internos */
    
    private DataMap getEntity(CacheTransactionManager manager, CacheHandler cache,
    		String key, boolean lock) 
    		throws RecoverException, IOException, TransactionException{
    	
    	if(lock && !managed.contains(key)){
			manageItem(manager, cache, key, timeout);
    	}
    	
    	return cacheItemMetadata.containsKey(key)? 
    				cacheItemMetadata.get(key) : 
					cache.getPointer(key);
    	
    }

    private void putEntity(
    		CacheTransactionManager manager, CacheHandler cache, String key, DataMap originalDta, 
    		InputStream inputData, long timeToLive, long timeToIdle
    		) throws StorageException, InterruptedException{

    	if(timeToLive < 0)
            throw new StorageException(CacheErrors.ERROR_1029);

    	if(timeToIdle < 0)
            throw new StorageException(CacheErrors.ERROR_1028);
    	
        if(key.length() > cache.getMaxKeySize())
            throw new StorageException(CacheErrors.ERROR_1008);
    	
		manageItem(manager, cache, key, timeout);
    	
		DataMap newDta = null;
		DataMap oldDta = cacheItemMetadata.get(key);
		
		if(oldDta != null){
			cache.releaseSegments(oldDta);
		}
		
		if(inputData != null){
			newDta = new DataMap();
			newDta.setCreationTime(System.currentTimeMillis());
			newDta.setId(cache.getNextModCount());
			newDta.setMostRecentTime(System.currentTimeMillis());
			newDta.setTimeToIdle(timeToIdle);
			newDta.setTimeToLive(timeToLive);
			cache.putData(newDta, inputData);
		}
		
		cacheItemMetadata.put(key, newDta);
		
    	if(!originalMetadata.containsKey(key)){
			originalMetadata.put(key, originalDta);
    	}
    	
    }
    
    private void manageItem(CacheTransactionManager manager, CacheHandler cache, String key, long time){
    	
    	if(managed.contains(key)){
    		return;
    	}
    	
    	if(time <= 0){
    		manager.lock(id, key);
    	}
    	else{
			manager.tryLock(id, key, time, TimeUnit.MILLISECONDS);
    	}
    	
    	managed.add(key);
    }

	public boolean isDestroyed() {
		return cache.isDestroyed();
	}

}
