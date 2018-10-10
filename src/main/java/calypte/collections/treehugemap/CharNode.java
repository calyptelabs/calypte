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

package calypte.collections.treehugemap;

import calypte.collections.ReferenceCollection;

/**
 * 
 * @author Ribeiro
 *
 * @param <T>
 */
public class CharNode<T> implements TreeNode<T>{

	private static final long serialVersionUID 	= 480902938041176366L;

    private long id;

    private long valueId;

    private long[] nextNodes;

    public CharNode(long id, long valueId, long[] nextNodes){
        this.nextNodes  = nextNodes;
        this.id         = id;
        this.valueId    = valueId;
    }
    
    public CharNode(){
        this.nextNodes  = new long[CharNodeUtil.LEN_NODES];
        this.id         = -1;
        this.valueId    = -1;

        for(int i=0;i<this.nextNodes.length;i++){
            this.nextNodes[i] = -1;
        }
    }

    public long[] getNextIndexNodes() {
    	return nextNodes;
    }
    
    public Object[] getNextNodes() {
    	
    	Object[] o = new Object[CharNodeUtil.LEN_NODES];
    	
    	for(int i=0;i<CharNodeUtil.LEN_NODES;i++) {
    		o[i] = CharNodeUtil.toMap(i);
    	}
    	
    	return o;
    }
    
    public void setNext(ReferenceCollection<TreeNode<T>> nodes, Object key, TreeNode<T> node){
    	int index = CharNodeUtil.toIndex((Character)key);
    	
    	if(index == -1)
    		throw new IllegalArgumentException("invalid char: " + id);
    	
        nextNodes[index] = node.getId();
        nodes.set(id, this);
    }

    public TreeNode<T> getNext(ReferenceCollection<TreeNode<T>> nodes, Object key) {
    	int index = CharNodeUtil.toIndex((Character)key);

    	if(index == -1)
    		throw new IllegalArgumentException("invalid char: " + id);
    	
		
		long nexNode = nextNodes[index];

        if(nexNode != -1){
            return nodes.get(nexNode);
        }
        else
            return null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getValueId() {
        return valueId;
    }

	public T setValue(ReferenceCollection<T> values, T value) {
        if(this.valueId == -1){
        	this.valueId = values.insert(value);
            return null;
        }
        else{
    		T old = values.get(valueId); 
            values.set(this.valueId, value);
            return old;
        }
    }

    public T removeValue(ReferenceCollection<T> values) {
        if(this.valueId != -1){
            T old = values.set(this.valueId, null);
            this.valueId = -1;
            return old;
        }
        else{
        	return null;
        }
    }

    public T getValue(ReferenceCollection<T> values) {
    	long id = this.valueId;
        if(id != -1)
            return values.get(id);
        else
            return null;
    }

	public boolean replaceValue(ReferenceCollection<T> values, T oldValue,
			T value) {
        if(this.valueId != -1){
            return values.replace(this.valueId, oldValue, value);
        }
        else{
        	return false;
        }
	}

	public T replaceValue(ReferenceCollection<T> values, T value) {
        if(this.valueId != -1){
            return values.replace(this.valueId, value);
        }
        else{
        	return null;
        }
	}

	public T putIfAbsentValue(ReferenceCollection<T> values, T value) {
        if(this.valueId != -1){
            return values.putIfAbsent(this.valueId, value);
        }
        else{
        	this.valueId = values.insert(value);
        	return null;
        }
	}

	public boolean removeValue(ReferenceCollection<T> values, T oldValue) {
        if(this.valueId != -1){
            boolean success = values.remove(this.valueId, oldValue);
            if(success){
            	this.valueId = -1;
            }
            return success;
        }
        else{
        	return false;
        }
	}

	
}
