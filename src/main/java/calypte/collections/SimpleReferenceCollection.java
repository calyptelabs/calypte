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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * @author Ribeiro
 *
 * @param <T>
 */
public class SimpleReferenceCollection<T> 
	implements ReferenceCollection<T>{

	private static final long serialVersionUID = 4658022218986426713L;

	protected static volatile long FreeManagerIDS = 0;
	
	public static final int GROUP_SIZE = 131072; //1mb
	
	public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
	
	public static final String FILE_TYPE = ".fsr";
	
	private FreeManager freeAddress;
	
	private SwapCollection<T> collection;
	
	private long lastPos;
	
    private boolean deleteOnExit;
	
    private Lock lock;

    private long length;
    
    static {
    	File root = new File(TMP_DIR);
    	File[] list = root.listFiles(new FileFilter() {
			
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().endsWith(".fsr");
			}
		});
    	
    	for(File f: list) {
    		if(f.isFile()) {
    			f.delete();
    		}
    	}
    	
    }
    
    public SimpleReferenceCollection() {
        this(
            HugeArrayList.DEFAULT_MAX_CAPACITY_ELEMENT, 
            HugeArrayList.DEFAULT_FRAGMENT_FACTOR_ELEMENT,
            null);
    }

	public SimpleReferenceCollection(
            int maxCapacityElements,
            double fragmentFactorElements,
            Swapper<T> swap) {

    	if(swap == null){
    		throw new NullPointerException("swap");
    	}
		
    	this.freeAddress  = new FreeManager(GROUP_SIZE);
    	this.lastPos      = 0;
    	this.lock         = new ReentrantLock();
        this.deleteOnExit = true;
        this.collection   = 
                    new SegmentedSwapCollectionImp<T>(
                        maxCapacityElements, 
                        fragmentFactorElements,
                        swap);
    }
    
	public long insert(T e) {
		
		long index = freeAddress.pop();
		
		if(index == -1){
			lock.lock();
			try{
				long i = length++;
				collection.add(i, e);
				return i;
			}
			finally{
				lock.unlock();
			}
		}
		else{
			collection.set(index, e);
			return index;
		}
			
	}

	public T set(long reference, T e) {
		return collection.set(reference, e);
	}

	public T get(long reference) {
		return collection.get(reference);
	}

	public boolean remove(long reference) {
		freeAddress.push(reference);
		return collection.set(reference, null) != null;
	}
	
	public boolean replace(long reference, T oldValue, T value) {
		return collection.replace(reference, oldValue, value);
	}

	public T replace(long reference, T value) {
		return collection.replace(reference, value);
	}

	public T putIfAbsent(long reference, T value) {
		return collection.putIfAbsent(reference, value);
	}

	public boolean remove(long reference, T oldValue) {
		freeAddress.push(reference);
		return collection.replace(reference, oldValue, null);
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
    	return this.lastPos;
    }

    public boolean isEmpty(){
    	return this.size() == 0;
    }
    
    public void clear() {
    	this.length = 0;
		collection.clear();
    }

    public void destroy(){
    	this.length = 0;
		collection.clear();
    	collection.destroy();
    }
    
    public void flush(){
    	collection.flush();
    }

	public void setReadOnly(boolean value) {
		collection.setReadOnly(value);
	}

	public boolean isReadOnly() {
    	return this.collection.isReadOnly();
	}
	
	private class FreeManager{
		
		private String prefixFileName;
		
		private long[] data;
		
		private int group;
		
		private int off;
		
		private int groupSize;
		
		private FreeManager(int groupSize) {
			this.data           = new long[groupSize];
			this.groupSize      = groupSize;
			this.off            = 0;
			this.group          = 0;
			this.prefixFileName = (FreeManagerIDS++) + "_";
		}
		
		public synchronized void push(long value) {
			
			if(off == groupSize) {
				persistData();
				group++;
				off = 0;
			}
			
			data[off++] = value;
		}
		
		public synchronized long pop() {
			
			if(off == 0)
				if(group > 0) {
					group--;
					off = data.length - 1;
					loadData();
				}
				else
					return -1;
			
			return data[--off];
		}
		
		private void persistData() {
			File f = new File(TMP_DIR, prefixFileName + group + FILE_TYPE);
			FileOutputStream stream         = null;
			ObjectOutputStream objectStream = null;
			try {
				stream       = new FileOutputStream(f);
				objectStream = new ObjectOutputStream(stream);
				
				objectStream.writeObject(data);
				objectStream.flush();
			}
			catch(Throwable e) {
				throw new IllegalStateException(e);
			}
			finally {
				if(objectStream != null){
					try {
						objectStream.close();
					}
					catch(Throwable e) {
						//suppress exception
					}
				}
				
				if(stream != null){
					try {
						stream.close();
					}
					catch(Throwable e) {
						//suppress exception
					}
				}
				
				stream = null;
				objectStream = null;
			}
		}
		
		private void loadData() {
			File f = new File(TMP_DIR, prefixFileName + group + FILE_TYPE);
			FileInputStream stream          = null;
			ObjectInputStream objectStream  = null;
			try {
				stream       = new FileInputStream(f);
				objectStream = new ObjectInputStream(stream);
				
				data = (long[])objectStream.readObject();
			}
			catch(Throwable e) {
				throw new IllegalStateException(e);
			}
			finally {
				if(objectStream != null){
					try {
						objectStream.close();
					}
					catch(Throwable e) {
						//suppress exception
					}
				}
				
				if(stream != null){
					try {
						stream.close();
					}
					catch(Throwable e) {
						//suppress exception
					}
				}
				
				f.delete();
				stream = null;
				objectStream = null;
			}
		}
		
	}
}
