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
 * @param <T>
 */
public class FlushableReferenceCollectionImp<T> 
	implements FlushableReferenceCollection<T>{

	private static final long serialVersionUID = 1406571295066759006L;

	private SimpleReferenceCollection<T>[] lists;

    private boolean deleteOnExit;
	
    public FlushableReferenceCollectionImp() {
        this(
            HugeArrayList.DEFAULT_MAX_CAPACITY_ELEMENT, 
            HugeArrayList.DEFAULT_FRAGMENT_FACTOR_ELEMENT,
            null,
            1);
    }

    @SuppressWarnings("unchecked")
	public FlushableReferenceCollectionImp(
            int maxCapacityElements,
            double fragmentFactorElements,
            Swapper<T>[] swap,
            int lists) {
    
    	this.lists        = new SimpleReferenceCollection[lists];
        this.deleteOnExit = true;
    	
    	for(int i=0;i<this.lists.length;i++){
            this.lists[i] = 
                    new SimpleReferenceCollection<T>(
                        maxCapacityElements, 
                        fragmentFactorElements,
                        swap[i]);
    		
    	}
    	
    }
    
	public long insert(T e) {
		int threadReference  = (int)(Thread.currentThread().getId() % lists.length);
		long index           = lists[threadReference].insert(e);
		return ((index & 0xffffffffL) << 8) | (threadReference & 0xff);
	}

	public T set(long reference, T e) {
		return lists[(int)(reference & 0xff)].set(reference >> 8, e);
	}

	public T get(long reference) {
		return lists[(int)(reference & 0xff)].get(reference >> 8);
	}

	public boolean remove(long reference) {
		return lists[(int)(reference & 0xff)].remove(reference >> 8);
	}
	
	public boolean replace(long reference, T oldValue, T value) {
		return lists[(int)(reference & 0xff)].replace(reference >> 8, oldValue, value);
	}

	public T replace(long reference, T value) {
		return lists[(int)(reference & 0xff)].replace(reference >> 8, value);
	}

	public T putIfAbsent(long reference, T value) {
		return lists[(int)(reference & 0xff)].putIfAbsent(reference >> 8, value);
	}

	public boolean remove(long reference, T oldValue) {
		return lists[(int)(reference & 0xff)].remove(reference >> 8, oldValue);
	}
	
    public void setDeleteOnExit(boolean value){
    	this.deleteOnExit = value;
    }

    public boolean isDeleteOnExit(){
    	return this.deleteOnExit;
    }
	
    public int size(){
    	return (int)this.length();
    }
    
    public long length(){
    	long size = 0;
    	for(SimpleReferenceCollection<T> l: lists){
    		size += l.length();
    	}
    	return size;
    }

    public boolean isEmpty(){
    	return this.size() == 0;
    }
    
    public void clear() {
    	for(SimpleReferenceCollection<T> l: lists){
    		l.clear();
    	}
    }

    public void destroy(){
    	for(SimpleReferenceCollection<T> l: lists){
    		l.destroy();
    	}
    }
    
    public void flush(){
    	for(SimpleReferenceCollection<T> l: lists){
    		l.flush();
    	}
    	
    }

	public void setReadOnly(boolean value) {
    	for(SimpleReferenceCollection<T> l: lists){
    		l.setReadOnly(value);
    	}
	}

	public boolean isReadOnly() {
    	return lists[0].isReadOnly();
	}

}
