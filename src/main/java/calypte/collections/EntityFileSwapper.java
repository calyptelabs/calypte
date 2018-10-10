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

package calypte.collections;

import java.lang.reflect.Array;

import org.brandao.entityfilemanager.EntityFileAccess;

import calypte.CacheErrors;
import calypte.CacheException;

/**
 * 
 * @author Ribeiro
 *
 * @param <T>
 */
public class EntityFileSwapper<T> implements Swapper<T>{

	private static final long serialVersionUID = -6380145781552093583L;

	private EntityFileAccess<T, ?, ?> entityFile;
	
	private volatile long maxID;
	
	public EntityFileSwapper(EntityFileAccess<T, ?, ?> entityFile){
		this.entityFile = entityFile;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void sendItem(long index, Entry<T> item) throws CacheException{
		try {
			if(maxID <= index) {
				int emptyInsert = (int)(maxID < 0? index : index - maxID) + 1;
				T[] array = (T[]) Array.newInstance(entityFile.getType(), emptyInsert);
				entityFile.seek(entityFile.length());
				entityFile.batchWrite(array);
				maxID = entityFile.length() - 1;
			}
			
			entityFile.seek(index);
			entityFile.write(item.getItem());
			entityFile.flush();
		}
		catch(Throwable e) {
			throw new CacheException(CacheErrors.ERROR_1014, e);
		}
	}
	
	public synchronized Entry<T> getItem(long index) throws CacheException{
		try {
			entityFile.seek(index);
			Entry<T> e = new Entry<T>(index, entityFile.read());
			e.setNeedUpdate(false);
			return e;
		}
		catch(Throwable e) {
			throw new CacheException(CacheErrors.ERROR_1015, e);
		}
	}

	public synchronized void clear() throws CacheException{
		try {
			entityFile.setLength(0);
		}
		catch(Throwable e) {
			throw new CacheException(CacheErrors.ERROR_1014, e);
		}
	}

	public synchronized void destroy() throws CacheException{
		try {
			entityFile.delete();
		}
		catch(Throwable e) {
			throw new CacheException(CacheErrors.ERROR_1014, e);
		}
	}


}
