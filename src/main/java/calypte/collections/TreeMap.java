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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Ribeiro
 */
public class TreeMap<K,T> 
    implements HugeMap<K,T>, Serializable{

    private static final long serialVersionUID                  = 4577949145861315961L;

    public static final int DEFAULT_MAX_CAPACITY_NODE 			= 2000;
    
    public static final float DEFAULT_CLEAR_FACTOR_NODE 		= 0.25F;
    
    public static final float DEFAULT_FRAGMENT_FACTOR_NODE 		= 0.03F;
    
    public static final int DEFAULT_MAX_CAPACITY_ELEMENT 		= 1000;
    
    public static final float DEFAULT_CLEAR_FACTOR_ELEMENT 		= 0.25F;
    
    public static final float DEFAULT_FRAGMENT_FACTOR_ELEMENT	= 0.03F;
    
    private FlushableReferenceCollection<T> values;
    
    private FlushableReferenceCollection<TreeNode<T>> nodes;
    
    private TreeNodes<T> treeNodes;
    
    public TreeMap(){
        this(
            DEFAULT_MAX_CAPACITY_NODE, 
            DEFAULT_FRAGMENT_FACTOR_NODE,
            null,
            1,
            DEFAULT_MAX_CAPACITY_ELEMENT, 
            DEFAULT_FRAGMENT_FACTOR_ELEMENT,
            null,
            1,
            null);
    }

    public TreeMap(
            int maxCapacityNodes,
            double fragmentFactorNodes,
            Swapper<TreeNode<T>>[] swapNodes,
            int subListsNodes,
            int maxCapacityElements,
            double fragmentFactorElements,
            Swapper<T>[] swapElements,
            int subListsElements,
            TreeNodes<T> treeNodes){
        
        this.values = 
            new FlushableReferenceCollectionImp<T>(
                maxCapacityElements, 
                fragmentFactorElements,
                swapElements,
                subListsElements);
        
        this.nodes = 
            new FlushableReferenceCollectionImp<TreeNode<T>>(
                maxCapacityNodes, 
                fragmentFactorNodes,
                swapNodes,
                subListsNodes);

        this.treeNodes = treeNodes;
        this.treeNodes.init(this.nodes);
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
            TreeNode<T> next = this.treeNodes.getNext(nodes, key, node, true);
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
            TreeNode<T> next = this.treeNodes.getNext(nodes, key, node, true);
            if(next == null)
                return false;
            else
                return remove(key, next, oldValue);
        }
        else{
        	return this.treeNodes.removeValue(nodes, values, node, oldValue);
        }
        
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
    
    public void setDeleteOnExit(boolean value){
    	this.nodes.setDeleteOnExit(value);
    	this.values.setDeleteOnExit(value);
    }

    public boolean isDeleteOnExit(){
    	return this.nodes.isDeleteOnExit();
    }
    
    public int size() {
        return (int)this.values.length();
    }

    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    public boolean containsKey(Object key) {
        return this.get(key) != null;
    }

    public boolean containsValue(Object value) {
    	throw new UnsupportedOperationException("not implemented yet");
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
    
    public void putAll(Map<? extends K, ? extends T> m) {
        for(K key: m.keySet())
            this.put(key, m.get(key));
    }

    public void clear() {
        this.values.clear();
        this.nodes.clear();
    }

    public void destroy() {
		this.values.destroy();
		this.nodes.destroy();
    }
    
    public void flush(){
        this.nodes.flush();
        this.values.flush();
    }
    
    public Set<K> keySet() {
        throw new UnsupportedOperationException("not implemented yet");
    }

	public Set<java.util.Map.Entry<K, T>> entrySet() {
        throw new UnsupportedOperationException("not implemented yet");
	}
    
    public Collection<T> values() {
    	throw new UnsupportedOperationException("not implemented yet");
    }

    public void setReadOnly(boolean value){
        this.nodes.setReadOnly(value);
        this.values.setReadOnly(value);
    }
    
    public boolean isReadOnly(){
        return this.values.isReadOnly();
    }
    
}
