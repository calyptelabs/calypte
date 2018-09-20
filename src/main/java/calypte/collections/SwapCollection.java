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

import java.util.concurrent.locks.Lock;

/**
 * 
 * @author Ribeiro
 *
 * @param <T>
 */
public interface SwapCollection<T> {

    long getId();

	void add(long index, T item);
    
	T set(long index, T item);

	T get(long index);

	boolean replace(long index, T oldValue, T item);

	T replace(long index, T item);

	T putIfAbsent(long index, T item);
	
	boolean isReadOnly();

	void setReadOnly(boolean readOnly);

	int getNumberOfGroups();
	
	Lock getGroupLock(long index);
	
	Lock getLock();

	Swapper<T> getSwap();

	void setForceSwap(boolean value);
	
	boolean isForceSwap();

	long getMaxSegmentCapacity();
    
    void flush();

    void clear();

    void destroy();
    
}
