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

package calypte.collections.swapper;

import java.lang.reflect.Array;

import org.brandao.entityfilemanager.EntityFile;
import org.brandao.entityfilemanager.EntityFileManagerConfigurer;
import org.brandao.entityfilemanager.tx.EntityFileTransaction;

import calypte.CacheErrors;
import calypte.CacheException;
import calypte.collections.Entry;
import calypte.collections.Swapper;

/**
 * 
 * @author Ribeiro
 *
 * @param <T>
 */
public class BasicEntityFileSwapper<T> implements Swapper<T>{

	private static final long serialVersionUID = -6380145781552093583L;

	private EntityFileManagerConfigurer efm;

	private String name;
	
	private volatile long maxID;
	
	protected Class<T> type;
	
	@SuppressWarnings("unchecked")
	public BasicEntityFileSwapper(EntityFileManagerConfigurer efm, String name, Class<?> type){
		this.efm   = efm;
		this.name  = name;
		this.type  = (Class<T>) type;
	}
	
	@SuppressWarnings("unchecked")
	public void sendItem(long index, Entry<T> item) throws CacheException{
		assert index == item.getIndex();
		
		EntityFileTransaction eft = null;
		try{
			eft = efm.beginTransaction();
				
			EntityFile<T> ef = efm.getEntityFile(name, eft, type);
			
			if(maxID <= index){
				synchronized(this){
					if(maxID <= index){
						int emptyInsert = (int)(maxID < 0? index : index - maxID) + 1;
						T[] array = (T[]) Array.newInstance(type, emptyInsert);
						long[] ids = ef.insert(array);
						maxID = ids[ids.length-1];
					}
				}
			}
			
			ef.update(index, item.getItem());
			
			eft.commit();
		}
		catch(Throwable e){
			e.printStackTrace();
			if(eft != null){
				try{
					eft.rollback();
				}
				catch(Throwable x){
					throw new CacheException(
							CacheErrors.ERROR_1014, 
							new IllegalStateException(x.toString(), e));
				}
			}
			throw new CacheException(CacheErrors.ERROR_1014, e);
		}		
	}
	
	public Entry<T> getItem(long index) throws CacheException{

		
		EntityFileTransaction eft = null;
		try{
			eft = efm.beginTransaction();
			EntityFile<T> ef = efm.getEntityFile(name, eft, type);
			Entry<T> e = new Entry<T>(index, ef.select(index, true));
			eft.rollback();
			assert index == e.getIndex();
			e.setNeedUpdate(false);
			return e;
		}
		catch(Throwable e){
			e.printStackTrace();
			if(eft != null){
				try{
					eft.rollback();
				}
				catch(Throwable x){
					throw new CacheException(
							CacheErrors.ERROR_1015, 
							new IllegalStateException(x.toString(), e));
				}
			}
			throw new CacheException(CacheErrors.ERROR_1015, e);
		}
		
	}

	public void clear() throws CacheException{
		efm.truncate(name);
	}

	public void destroy() throws CacheException{
	}

}
