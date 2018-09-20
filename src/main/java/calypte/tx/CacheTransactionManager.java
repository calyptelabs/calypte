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

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import calypte.CacheHandler;

/**
 * Gestor das transações de um cache.
 * 
 * @author Ribeiro
 *
 */
public interface CacheTransactionManager {

	void lock(Serializable transaction, String key) throws TransactionException;

	void tryLock(Serializable transaction, String key, long time, TimeUnit unit) throws TransactionException;

	void unlock(Serializable transaction, String key) throws TransactionException;
	
	void commit(Serializable transaction) throws TransactionException;
	
	void rollback(Serializable transaction) throws TransactionException;
	
	void setPath(String value);
	
	String getPath();

	void setTimeout(long value);
	
	long getTimeout();
	
	CacheTransactionHandler begin(CacheHandler cache);
	
	CacheTransactionHandler getCurrrent();

	CacheTransactionHandler getCurrrent(boolean required);
	
	void close(CacheTransactionHandler tx) throws TransactionException;
}
