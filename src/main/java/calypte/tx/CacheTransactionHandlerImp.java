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

import java.io.InputStream;
import java.io.Serializable;

import calypte.CacheErrors;
import calypte.CacheException;
import calypte.CacheHandler;
import calypte.CalypteConfig;
import calypte.DataMap;
import calypte.RecoverException;
import calypte.StorageException;
import calypte.collections.MapReferenceCollection.Find;

/**
 * 
 * @author Ribeiro
 *
 */
public class CacheTransactionHandlerImp implements CacheTransactionHandler{

	private static final long serialVersionUID = -7066992634138343620L;

	private TransactionInfo transactionInfo;
	
	private boolean commitInProgress;
	
	private boolean started;

	private boolean rolledBack;
	
	private boolean commited;
	
	private CacheHandler cache;
	
	private CacheTransactionManager transactionManager;
	
	private Serializable id;
	
	private long timeout;
	
	public CacheTransactionHandlerImp(
			Serializable id, 
			CacheTransactionManager transactionManager, 
			CacheHandler cache, long timeout){
		
		this.commitInProgress   = false;
		this.started            = false;
		this.commited           = false;
		this.rolledBack         = false;
		this.cache				= cache;
		this.transactionManager	= transactionManager;
		this.id					= id;
		this.timeout            = timeout;
	}
	
	public Serializable getId() {
		return id;
	}
	
	public synchronized void begin() {
		
		transactionInfo = new TransactionInfo(id, transactionManager, cache, timeout);
		
		if(started){
			throw new TransactionException(CacheErrors.ERROR_1016);
		}

		if(commitInProgress){
			throw new TransactionException(CacheErrors.ERROR_1010);
		}
		
		started = true;
	}
	
	public boolean isRolledBack() {
		return rolledBack;
	}

	public boolean isCommited() {
		return commited;
	}

	public void rollback() throws TransactionException {
		this.rollback(transactionInfo);
	}
	
	private void rollback(TransactionInfo transactionInfo) throws TransactionException {

		if(rolledBack){
			throw new TransactionException(CacheErrors.ERROR_1011);
		}

		if(commited){
			throw new TransactionException(CacheErrors.ERROR_1012);
		}
		
		if(!started){
			throw new TransactionException(CacheErrors.ERROR_1013);
		}
		
		try{
			if(commitInProgress){
				transactionInfo.rollback(cache);
				closeTransaction(transactionInfo);
			}
			else{
				closeTransaction(transactionInfo);
				return;
			}
			this.rolledBack = true;
			this.commited   = false;
		}
		catch(TransactionException e){
			throw e;
		}
		catch(CacheException e){
			throw new TransactionException(e, e.getError(), e.getParams());
		}
		catch (Throwable e) {
			throw new TransactionException(e, CacheErrors.ERROR_1018);
		}
		
	}
	
	protected void closeTransaction(TransactionInfo transactionInfo) throws TransactionException{
		transactionManager.close(this);
	}
	
	public void close(){
		try{
			transactionInfo.close(cache);
			cache              = null;
			transactionInfo    = null;
			transactionManager = null;
			started            = false;
			commitInProgress   = false;
		}
		catch(Throwable e){
			throw new TransactionException(e, CacheErrors.ERROR_1023);
		}
	}
	
	public void commit() throws TransactionException {
		if(this.rolledBack){
			throw new TransactionException(CacheErrors.ERROR_1011);
		}

		if(this.commited){
			throw new TransactionException(CacheErrors.ERROR_1012);
		}
		
		if(!started){
			throw new TransactionException(CacheErrors.ERROR_1013);
		}
		
		try{
			this.commitInProgress = true;
			this.transactionInfo.commit(cache);
			this.closeTransaction(this.transactionInfo);
			this.commitInProgress = false;
			this.commited         = true;
			this.rolledBack       = false;
		}
		catch(TransactionException e){
			throw e;
		}
		catch(CacheException e){
			throw new TransactionException(e, e.getError(), e.getParams());
		}
		catch(Throwable e){
			throw new TransactionException(e, CacheErrors.ERROR_1019);
		}
		
	}

	public void find(Find<DataMap> result) {
		transactionInfo.find(result);
	}
	
	public InputStream getStream(String key, boolean forUpdate)
			throws RecoverException {
		return transactionInfo.getStream(key, forUpdate);
	}

	public boolean putStream(String key, InputStream inputData,
			long timeToLive, long timeToIdle) throws StorageException {
		return transactionInfo.putStream(key, inputData, timeToLive, timeToIdle);
	}

	public boolean replaceStream(String key, InputStream inputData,
			long timeToLive, long timeToIdle) throws StorageException {
		return transactionInfo.replaceStream(key, inputData, timeToLive, timeToIdle);
	}

	public InputStream putIfAbsentStream(String key, InputStream inputData,
			long timeToLive, long timeToIdle) throws StorageException {
		return transactionInfo.putIfAbsentStream(key, inputData, timeToLive, timeToIdle);
	}

	public InputStream getStream(String key) throws RecoverException {
		return transactionInfo.getStream(key);
	}

	public boolean removeIfInvalid(String key) throws StorageException {
		return transactionInfo.removeIfInvalid(key);
	}
	
	public boolean removeStream(String key) throws StorageException {
		return transactionInfo.removeStream(key);
	}

	public boolean containsKey(String key) {
		return transactionInfo.containsKey(key);
	}

	public DataMap getPointer(String key) throws RecoverException {
		return transactionInfo.getPointer(key);
	}

	public void setPointer(String key, DataMap newDta) throws RecoverException {
		transactionInfo.setPointer(key, newDta);
	}

	public boolean replacePointer(String key, DataMap originalDta,
			DataMap newDta) throws RecoverException {
		return transactionInfo.replacePointer(key, originalDta, newDta);
	}

	public void remove(String key, DataMap data) {
		transactionInfo.remove(key, data);
	}

	public void releaseSegments(DataMap map) {
		transactionInfo.releaseSegments(map);
	}

	public InputStream getStream(String key, DataMap map)
			throws RecoverException {
		return transactionInfo.getStream(key, map);
	}

	public void putData(DataMap map, InputStream inputData)
			throws StorageException, InterruptedException {
		transactionInfo.putData(map, inputData);
	}

	public long getNextModCount() {
		return transactionInfo.getNextModCount();
	}

	public CalypteConfig getConfig() {
		return transactionInfo.getConfig();
	}

	public long getCountRead() {
		return transactionInfo.getCountRead();
	}

	public long getCountWrite() {
		return transactionInfo.getCountWrite();
	}

	public long getCountRemoved() {
		return transactionInfo.getCountRemoved();
	}

	public long getCountReadData() {
		return transactionInfo.getCountReadData();
	}

	public long getCountWriteData() {
		return transactionInfo.getCountWriteData();
	}

	public long getCountRemovedData() {
		return transactionInfo.getCountRemovedData();
	}

	public boolean isDeleteOnExit() {
		return transactionInfo.isDeleteOnExit();
	}

	public void setDeleteOnExit(boolean deleteOnExit) {
		transactionInfo.setDeleteOnExit(deleteOnExit);
	}

	public int getMaxKeySize() {
		return transactionInfo.getMaxKeySize();
	}
	
	public long size() {
		return transactionInfo.size();
	}

	public boolean isEmpty() {
		return transactionInfo.isEmpty();
	}

	public void clear() {
		transactionInfo.clear();
	}

	public void destroy() {
		transactionInfo.destroy();
	}

	public long getCreationTime() {
		return transactionInfo.getCreationTime();
	}

	public boolean isDestroyed() {
		return transactionInfo.isDestroyed();
	}

}
