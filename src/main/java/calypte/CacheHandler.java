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
import java.io.Serializable;

import calypte.collections.MapReferenceCollection.Find;

/**
 * 
 * @author Ribeiro
 *
 */
public interface CacheHandler extends Serializable{
    
	void find(Find<DataMap> result);
	
    boolean putStream(String key, InputStream inputData, 
    		long timeToLive, long timeToIdle) throws StorageException;

    boolean replaceStream(String key, InputStream inputData, 
    		long timeToLive, long timeToIdle) throws StorageException;
    
    InputStream putIfAbsentStream(String key, InputStream inputData, 
    		long timeToLive, long timeToIdle) throws StorageException;
    
    InputStream getStream(String key) throws RecoverException;
    
    boolean removeIfInvalid(String key) throws StorageException;
    
    boolean removeStream(String key) throws StorageException;
    
    boolean containsKey(String key);

    DataMap getPointer(String key) throws RecoverException;

    void setPointer(String key, DataMap newDta) throws RecoverException;
    
    boolean replacePointer(String key, DataMap originalDta, DataMap newDta) throws RecoverException;
    
    void remove(String key, DataMap data);
    
    void releaseSegments(DataMap map);
    
    InputStream getStream(String key, DataMap map) throws RecoverException;
    
    void putData(DataMap map, InputStream inputData) throws StorageException, InterruptedException;

    long getNextModCount();
    
    int getMaxKeySize();
    
	CalypteConfig getConfig();
    
    long getCountRead();

    long getCountWrite();

    long getCountRemoved();

    long getCountReadData();
    
    long getCountWriteData();

    long getCountRemovedData();
    
    boolean isDeleteOnExit();

    long getCreationTime();
    
	void setDeleteOnExit(boolean deleteOnExit);

	boolean isDestroyed();
	
	long size();
	
	boolean isEmpty();
	
	void clear();
	
	void destroy();
	
}
