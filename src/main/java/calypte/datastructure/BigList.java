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

package calypte.datastructure;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * 
 * @author Ribeiro
 *
 * @param <E>
 */
public interface BigList<E> 
	extends List<E>, BigCollection<E> {

    long extendSize();
	
    boolean addAll(long index, Collection<? extends E> c);
    
    E get(long index);
    
    E set(long index, E element);
    
    void add(long index, E element);
    
    E remove(long index);
    
    long extendIndexOf(Object o);
    
    long extendLastIndexOf(Object o);
    
    ListIterator<E> listIterator(long index);
    
    List<E> subList(long fromIndex, long toIndex);
    
}
