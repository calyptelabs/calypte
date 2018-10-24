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

import java.util.concurrent.locks.Lock;

import calypte.BasicCacheHandler;
import calypte.DataMap;
import calypte.collections.ReferenceCollection;

public class DataMapStringTreeNodes extends StringTreeNodes<DataMap>{

	private static final long serialVersionUID = -6243281632750373123L;

	private BasicCacheHandler basicCacheHandler;
	
	public DataMapStringTreeNodes(BasicCacheHandler basicCacheHandler) {
		this.basicCacheHandler = basicCacheHandler;
	}
	
	public DataMap setValue(ReferenceCollection<TreeNode<DataMap>> nodes, 
    		ReferenceCollection<DataMap> values, TreeNode<DataMap> node, DataMap value){
    	Lock lock = locks.getLock(node.getId());
    	lock.lock();
    	try{
    		node = nodes.get(node.getId());
    		DataMap r = node.setValue(values, value);
    		nodes.set(node.getId(), node);
    		return r;
    	}
    	finally{
    		lock.unlock();
    	}
    }
    
    public boolean replaceValue(ReferenceCollection<TreeNode<DataMap>> nodes, 
    		ReferenceCollection<DataMap> values, TreeNode<DataMap> node, DataMap oldValue, DataMap value){
    	Lock lock = this.locks.getLock(node.getId());
    	lock.lock();
    	try{
    		node = nodes.get(node.getId());
    		DataMap e = node.getValue(values);
    		if(e != null && !e.isDead(basicCacheHandler.getCreationTime()) && e.equals(oldValue) ) {
    			node.setValue(values, value);
        		nodes.set(node.getId(), node);
        		return true;
    		}
    		return false;
    	}
    	finally{
    		lock.unlock();
    	}
    }

    public DataMap replaceValue(ReferenceCollection<TreeNode<DataMap>> nodes, 
    		ReferenceCollection<DataMap> values, TreeNode<DataMap> node, DataMap value){
    	Lock lock = this.locks.getLock(node.getId());
    	lock.lock();
    	try{
    		node = nodes.get(node.getId());
    		DataMap e = node.getValue(values);
    		if(e != null && !e.isDead(basicCacheHandler.getCreationTime())) {
    			node.setValue(values, value);
        		nodes.set(node.getId(), node);
        		return e;
    		}
    		return null;
    	}
    	finally{
    		lock.unlock();
    	}
    }

    public DataMap putIfAbsentValue(ReferenceCollection<TreeNode<DataMap>> nodes, 
    		ReferenceCollection<DataMap> values, TreeNode<DataMap> node, DataMap value){
    	Lock lock = this.locks.getLock(node.getId());
    	lock.lock();
    	try{
    		node = nodes.get(node.getId());
    		DataMap e = node.getValue(values);
    		if(e == null || e.isDead(basicCacheHandler.getCreationTime())) {
    			node.setValue(values, value);
        		nodes.set(node.getId(), node);
    		}
    		return e;
    	}
    	finally{
    		lock.unlock();
    	}
    }
    
    public DataMap removeValue(ReferenceCollection<TreeNode<DataMap>> nodes, 
    		ReferenceCollection<DataMap> values, TreeNode<DataMap> node) {
    	Lock lock = this.locks.getLock(node.getId());
    	lock.lock();
    	try{
    		node = nodes.get(node.getId());
    		DataMap r = node.removeValue(values);
    		nodes.set(node.getId(), node);
    		return r;
    	}
    	finally{
    		lock.unlock();
    	}
    }

    public boolean removeValue(ReferenceCollection<TreeNode<DataMap>> nodes, 
    		ReferenceCollection<DataMap> values, TreeNode<DataMap> node, DataMap oldValue) {
    	Lock lock = this.locks.getLock(node.getId());
    	lock.lock();
    	try{
    		node = nodes.get(node.getId());
    		boolean r = node.removeValue(values, oldValue);
    		nodes.set(node.getId(), node);
    		return r;
    	}
    	finally{
    		lock.unlock();
    	}
    }
	
}
