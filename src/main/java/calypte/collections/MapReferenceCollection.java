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

/**
 * 
 * @author Ribeiro
 *
 * @param <K>
 * @param <T>
 */
public interface MapReferenceCollection<K,T> {

    T put(K key, T element);

    boolean replace(K key, T oldElement, T element);

    T replace(K key, T element);

    T putIfAbsent(K key, T element);
    
    T get(Object key);

    T remove(Object key);

	boolean remove(Object key, Object oldValue);

    void clear();

    void destroy();
    
    void flush();
	
    void setDeleteOnExit(boolean value);

    boolean isDeleteOnExit();
    
    void setReadOnly(boolean value);
    
    boolean isReadOnly();
    
}
