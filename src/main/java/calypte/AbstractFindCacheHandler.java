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

import calypte.collections.MapReferenceCollection.Find;
import calypte.collections.treehugemap.TreeNode;

/**
 * 
 * @author Ribeiro
 *
 */
public abstract class AbstractFindCacheHandler 
	implements Find<DataMap>{

	private String key;
	
	public AbstractFindCacheHandler(String key) {
		this.chars      = key == null? null : key.toCharArray();
		this.index      = 0;
		this.currentKey = new StringBuilder();
	}
	
	/* map */
	
	private char[] chars;
	
	private int index;
	
	private StringBuilder currentKey;
	
	public void found(DataMap value) {
		found(key, currentKey.toString(), value);
	}

	protected abstract void found(String request, String key, DataMap value);
	
	public boolean accept() {
		return chars == null? true : index == chars.length;
	}

	public boolean acceptNodeKey(Object key) {
		return chars == null? true : key.equals(chars[index]);
	}
	
	public void beforeNextNode(Object key, TreeNode<DataMap> node) {
		currentKey.append(key);
		index++;
	}

	public void afterNextNode(Object key, TreeNode<DataMap> node) {
		currentKey.setLength(currentKey.length() - 1);
		index--;
	}
	
}
