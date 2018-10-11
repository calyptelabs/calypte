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
import java.util.concurrent.locks.Lock;

import calypte.lock.CacheLockImp;

/**
 * Provê as operações de um cache com bloqueio dos métodos que alteram o cache.
 * 
 * <pre>
 * ex:
 *    
 *    Cache cache = ...;
 *    cache.put("uma_chave", meuObjeto, 1200, 0);
 *    
 * ex2:
 *    
 *    MeuObjeto o = cache.get("uma_chave");
 *
 * </pre>
 * @author Ribeiro
 *
 */
public class ConcurrentCache extends BasicCache {
	
	private static final long serialVersionUID = -8558471389768293591L;

	protected transient CacheLock<String> locks;
	
    /**
     * Cria um novo cache a partir de uma configuração específica.
     * @param config configuração.
     */
    public ConcurrentCache(CalypteConfig config){
    	super(config);
    	this.locks = new CacheLockImp<String>();
    }
    
    /**
     * Cria um novo cache.
     * 
     * @param cacheHandler Manpulador do cache.
     */
    public ConcurrentCache(CacheHandler cacheHandler){
    	super(cacheHandler);
    	this.locks = new CacheLockImp<String>();
    }
    
	/* métodos de armazenamento */
	
	public boolean replace(String key, Object value, 
			long timeToLive, long timeToIdle) throws StorageException {
		
		Lock lock = this.locks.getLock(key).writeLock();
		lock.lock();
		try{
			return super.replace(key, value, timeToLive, timeToIdle);
		}
		finally{
			lock.unlock();
		}
	}
	
    public boolean replaceStream(String key, InputStream inputData, long timeToLive, long timeToIdle) throws StorageException{
		Lock lock = this.locks.getLock(key).writeLock();
		lock.lock();
		try{
			return super.replaceStream(key, inputData, timeToLive, timeToIdle);
		}
		finally{
			lock.unlock();
		}
    }
	
	public boolean replace(String key, Object oldValue, 
			Object newValue, long timeToLive, long timeToIdle) throws StorageException {
		
		Lock lock = this.locks.getLock(key).writeLock();
		lock.lock();
		try{
			Object o = super.get(key);
			if(o != null && o.equals(oldValue)){
				super.put(key, newValue, timeToLive, timeToIdle);
				return true;
			}
			else
				return false;
		}
		catch(StorageException e){
			throw e;
		}
		catch(RecoverException e){
			throw new StorageException(e, e.getError(), e.getParams());
		}
		finally{
			lock.unlock();
		}
	}
	
	public Object putIfAbsent(String key,
			Object value, long timeToLive, long timeToIdle) throws StorageException {
		
		Lock lock = this.locks.getLock(key).writeLock();
		lock.lock();
		try{
			return super.putIfAbsent(key, value, timeToLive, timeToIdle);
		}
		finally{
			lock.unlock();
		}
	}
	
    public InputStream putIfAbsentStream(String key, InputStream inputData, long timeToLive, long timeToIdle) throws StorageException{
    	
		Lock lock = this.locks.getLock(key).writeLock();
		lock.lock();
		try{
			return super.putIfAbsentStream(key, inputData, timeToLive, timeToIdle);
		}
		catch(StorageException e){
			throw e;
		}
		catch(RecoverException e){
			throw new StorageException(e, e.getError(), e.getParams());
		}
		finally{
			lock.unlock();
		}
    }
	
	public boolean put(String key, Object value, long timeToLive, long timeToIdle) throws StorageException {
		
		Lock lock = this.locks.getLock(key).writeLock();
		lock.lock();
		try{
			return super.put(key, value, timeToLive, timeToIdle);
		}
		finally{
			lock.unlock();
		}
	}
	
    public boolean putStream(String key, InputStream inputData, 
    		long timeToLive, long timeToIdle) throws StorageException{
		Lock lock = this.locks.getLock(key).writeLock();
		lock.lock();
		try{
			return super.putStream(key, inputData, timeToLive, timeToIdle);
		}
		finally{
			lock.unlock();
		}
    }
	
    /* métodos de coleta */
	
	public Object get(String key) throws RecoverException {
		Lock lock = this.locks.getLock(key).readLock();
		lock.lock();
		try {
			return super.get(key);
		}
		finally {
			lock.unlock();
		}
	}

    public InputStream getStream(String key) throws RecoverException {
		Lock lock = this.locks.getLock(key).readLock();
		lock.lock();
		try {
			return super.getStream(key);
		}
		finally {
			lock.unlock();
		}
    }
	
    /* métodos de remoção */

	public boolean remove(String key, Object value) throws StorageException {
		
		Lock lock = this.locks.getLock(key).writeLock();
		lock.lock();
		try{
			Object o = super.get(key);
			if(o != null && o.equals(value)){
				return super.remove(key);
			}
			else
				return false;
		}
    	catch(StorageException e){
    		throw e;
    	}
    	catch(RecoverException e){
    		throw new StorageException(e, e.getError(), e.getParams());
    	}
		catch(Throwable e){
			throw new StorageException(e, CacheErrors.ERROR_1021);
		}
		finally{
			lock.unlock();
		}
	}
	
    public boolean remove(String key) throws StorageException{
		Lock lock = this.locks.getLock(key).writeLock();
		lock.lock();
		try{
			return super.remove(key);
		}
		finally{
			lock.unlock();
		}
    }
	
}
