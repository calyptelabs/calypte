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

import calypte.collections.treehugemap.TreeMapKey;
import calypte.collections.treehugemap.TreeNode;
import calypte.collections.treehugemap.TreeNodes;

/**
 * 
 * @author Ribeiro
 *
 * @param <K>
 * @param <T>
 */
public class BasicMapReferenceCollection<K,T> 
	implements MapReferenceCollection<K,T>{

    public static final int DEFAULT_MAX_CAPACITY_NODE 			= 2000;
    
    public static final float DEFAULT_CLEAR_FACTOR_NODE 		= 0.25F;
    
    public static final float DEFAULT_FRAGMENT_FACTOR_NODE 		= 0.03F;
    
    public static final int DEFAULT_MAX_CAPACITY_ELEMENT 		= 1000;
    
    public static final float DEFAULT_CLEAR_FACTOR_ELEMENT 		= 0.25F;
    
    public static final float DEFAULT_FRAGMENT_FACTOR_ELEMENT	= 0.03F;
    
    private FlushableReferenceCollection<T> values;
    
    private FlushableReferenceCollection<TreeNode<T>> nodes;
    
    private TreeNodes<T> treeNodes;

    public BasicMapReferenceCollection(){
        this(
            DEFAULT_MAX_CAPACITY_NODE, 
            DEFAULT_CLEAR_FACTOR_NODE, 
            DEFAULT_FRAGMENT_FACTOR_NODE,
            null,
            1,
            1,
            DEFAULT_MAX_CAPACITY_ELEMENT, 
            DEFAULT_CLEAR_FACTOR_ELEMENT, 
            DEFAULT_FRAGMENT_FACTOR_ELEMENT,
            null,
            1,
            1,
            null);
    }

    public BasicMapReferenceCollection(
            int maxCapacityNodes,
            double clearFactorNodes, 
            double fragmentFactorNodes,
            Swapper<TreeNode<T>>[] swapNodes,
            int quantitySwaperThreadNodes,   
            int subListsNodes,
            int maxCapacityElements,
            double clearFactorElements, 
            double fragmentFactorElements,
            Swapper<T> []swapElements,
            int quantitySwaperThreadElements,
            int subListsElements,
            TreeNodes<T> treeNodes){
        
        this.values = 
            new FlushableReferenceCollectionImp<T>(
                maxCapacityElements, 
                clearFactorElements, 
                fragmentFactorElements,
                swapElements,
                quantitySwaperThreadElements,
                subListsElements);
        
        this.nodes = 
            new FlushableReferenceCollectionImp<TreeNode<T>>(
                maxCapacityNodes, 
                clearFactorNodes, 
                fragmentFactorNodes,
                swapNodes,
                quantitySwaperThreadNodes,
                subListsNodes);

        this.treeNodes = treeNodes;
        this.treeNodes.init(this.nodes);
    }
    
    public T put(K key, T element){
    	TreeMapKey k = this.treeNodes.getKey(key);
        TreeNode<T> root = this.treeNodes.getFirst(this.nodes);
        return put(k, root, element);
    }

    public boolean replace(K key, T oldElement, T element){
    	TreeMapKey k = this.treeNodes.getKey(key);
        TreeNode<T> root = this.treeNodes.getFirst(this.nodes);
        return this.replace(k, root, oldElement, element);
    }

    public T replace(K key, T element){
    	TreeMapKey k = this.treeNodes.getKey(key);
        TreeNode<T> root = this.treeNodes.getFirst(this.nodes);
        return this.replace(k, root, element);
    }

    public T putIfAbsent(K key, T element){
    	TreeMapKey k = this.treeNodes.getKey(key);
        TreeNode<T> root = this.treeNodes.getFirst(this.nodes);
        return this.putIfAbsent(k, root, element);
    }
    
    public T get(Object key) {
    	TreeMapKey k = this.treeNodes.getKey(key);
        TreeNode<T> root = this.treeNodes.getFirst(this.nodes);
        if(root == null)
            return null;
        else
            return this.get(k, root);
    }

    public T remove(Object key) {
    	TreeMapKey k = this.treeNodes.getKey(key);
        TreeNode<T> root = this.treeNodes.getFirst(this.nodes);
        if(root == null)
            return null;
        else
            return this.remove(k, root);
    }

    @SuppressWarnings("unchecked")
	public boolean remove(Object key, Object oldValue) {
    	TreeMapKey k = this.treeNodes.getKey(key);
        TreeNode<T> root = this.treeNodes.getFirst(this.nodes);
        if(root == null)
            return false;
        else
            return this.remove(k, root, (T)oldValue);
    }
    
    public void find(Find<T> f) {
        TreeNode<T> root = this.treeNodes.getFirst(this.nodes);
        
        if(root == null)
        	return; 

    	find(f, root);
    	
    }
    
    public void clear() {
        this.values.clear();
        this.nodes.clear();
        this.treeNodes.init(this.nodes);
    }

    public void destroy() {
		this.values.destroy();
		this.nodes.destroy();
    }
    
    public void flush(){
        this.nodes.flush();
        this.values.flush();
    }

    public void setDeleteOnExit(boolean value){
    	this.nodes.setDeleteOnExit(value);
    	this.values.setDeleteOnExit(value);
    }

    public boolean isDeleteOnExit(){
    	return this.nodes.isDeleteOnExit();
    }

    public void setReadOnly(boolean value){
        this.nodes.setReadOnly(value);
        this.values.setReadOnly(value);
    }
    
    public boolean isReadOnly(){
        return this.values.isReadOnly();
    }

    private T put(TreeMapKey key, TreeNode<T> node, T value){
        
        if(!this.treeNodes.isEquals(key, node)){
            TreeNode<T> next = this.treeNodes.getNext(nodes, key, node, false);
            return this.put(key, next, value);
        }
        else{
        	return this.treeNodes.setValue(nodes, values, node, value);
        }
    }

    private boolean replace(TreeMapKey key, TreeNode<T> node, T oldValue, T value){
        
        if(!this.treeNodes.isEquals(key, node)){
            TreeNode<T> next = this.treeNodes.getNext(nodes, key, node, false);
            return this.replace(key, next, oldValue, value);
        }
        else{
        	return this.treeNodes.replaceValue(nodes, values, node, oldValue, value);
        }
    }

    private T replace(TreeMapKey key, TreeNode<T> node, T value){
        
        if(!this.treeNodes.isEquals(key, node)){
            TreeNode<T> next = this.treeNodes.getNext(nodes, key, node, false);
            return this.replace(key, next, value);
        }
        else{
        	return this.treeNodes.replaceValue(nodes, values, node, value);
        }
    }

    private T putIfAbsent(TreeMapKey key, TreeNode<T> node, T value){
        
        if(!this.treeNodes.isEquals(key, node)){
            TreeNode<T> next = this.treeNodes.getNext(nodes, key, node, false);
            return this.putIfAbsent(key, next, value);
        }
        else{
        	return this.treeNodes.putIfAbsentValue(nodes, values, node, value);
        }
    }
    
    private T get(TreeMapKey key, TreeNode<T> node){
        
        if(!this.treeNodes.isEquals(key, node)){
            TreeNode<T> next = this.treeNodes.getNext(nodes, key, node, true);
            if(next == null)
                return null;
            else
                return this.get(key, next);
        }
        else
            return this.treeNodes.getValue(nodes, values, node);
        
    }

    private T remove(TreeMapKey key, TreeNode<T> node){
        
        if(!this.treeNodes.isEquals(key, node)){
            TreeNode<T> next = this.treeNodes.getNext(this.nodes, key, node, true);
            if(next == null)
                return null;
            else
                return (T)remove(key, next);
        }
        else{
        	return this.treeNodes.removeValue(nodes, values, node);
        }
        
    }

    private boolean remove(TreeMapKey key, TreeNode<T> node, T oldValue){
        
        if(!this.treeNodes.isEquals(key, node)){
            TreeNode<T> next = this.treeNodes.getNext(this.nodes, key, node, true);
            if(next == null)
                return false;
            else
                return remove(key, next, oldValue);
        }
        else{
        	return this.treeNodes.removeValue(nodes, values, node, oldValue);
        }
        
    }
    
    private void find(Find<T> f, TreeNode<T> node) {

    	T e = treeNodes.getValue(nodes, values, node);
    	
    	if(e != null && f.accept()) {
    		f.found(e);
    	}
    	
		Object[] nexKeys = node.getNextNodes();
		
		for(Object k: nexKeys) {
			TreeNode<T> n = node.getNext(nodes, k);
			if(n != null && f.acceptNodeKey(k)) {
				f.beforeNextNode(k, n);
				find(f, n);
				f.afterNextNode(k, n);
			}
		}
    	
    }
    
}
