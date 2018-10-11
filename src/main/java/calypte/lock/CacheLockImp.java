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

package calypte.lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import calypte.CacheLock;

/**
 * 
 * @author Ribeiro
 *
 * @param <T>
 */
public class CacheLockImp<T> implements CacheLock<T>{

	protected ConcurrentMap<T, ReadWriteLock> locks;
	
	protected ConcurrentMap<T, Integer> counter;
	
	private Lock[] sharedLocks;

	public CacheLockImp() {
		this.locks       = new ConcurrentHashMap<T, ReadWriteLock>();
		this.counter     = new ConcurrentHashMap<T, Integer>();
		this.sharedLocks = new Lock[10];
		
		for(int i=0;i<this.sharedLocks.length;i++) {
			this.sharedLocks[i] = new ReentrantLock();
		}
			
	}
	
	public ReadWriteLock getLock(T value) {
		int actionLockIndex = value.hashCode() % sharedLocks.length;
		Lock actionLock     = sharedLocks[actionLockIndex > 0? actionLockIndex : -actionLockIndex];
		ReadWriteLock lock  = null;
		
		actionLock.lock();
		try {
			lock = locks.get(value);
			if(lock == null) {
				lock = new ReentrantReadWriteLock();
				locks.put(value, lock);
				Integer count = counter.get(value);
				counter.put(value, count == null? 1 : ++count);
			}
			else {
				Integer count = counter.get(value);
				counter.put(value, ++count);
			}
		}
		finally {
			actionLock.unlock();
		}
		
		return new ReadWriteLockProxy(lock, actionLock, value);
	}

	@SuppressWarnings("unchecked")
	void destroy(ReadWriteLockProxy handler){
		
		Lock actionLock = handler.sharedLock;
		Object value    = handler.value;
		actionLock.lock();
		try {
			Integer count = counter.get(value);
			count--;
			
			if(count == 0) {
				counter.remove(value);
				locks.remove(value);
			}
			else {
				counter.put((T)value, count);
			}
		}
		finally {
			actionLock.unlock();
		}

	}

	public class ReadWriteLockProxy implements ReadWriteLock{

		private ReadWriteLock lock;
		
		private Lock sharedLock;
		
		private Object value;
		
		public ReadWriteLockProxy(ReadWriteLock lock, Lock sharedLock, Object value) {
			this.lock = lock;
			this.sharedLock = sharedLock;
			this.value = value;
		}
		
		public Lock readLock() {
			return lock.readLock();
		}

		public Lock writeLock() {
			return lock.writeLock();
		}
		
		public void finalize() throws Throwable{
			try{
				destroy(this);
			}
			finally{
				super.finalize();
			}
		}
	}
}
