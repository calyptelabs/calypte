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

import calypte.collections.treehugemap.StringTreeNodes;

/**
 *
 * @author Ribeiro
 */
public class StringTreeMap<T> extends TreeMap<String, T> {
    
	private static final long serialVersionUID = 4262873183379962091L;

	public StringTreeMap(
            int maxCapacityNodes,
            double clearFactorNodes, 
            double fragmentFactorNodes,
            Swapper[] swap,
            int quantitySwaperThreadNodes,
            int subListsNodes,
            int maxCapacityElements,
            double clearFactorElements, 
            double fragmentFactorElements,
            int quantitySwaperThreadElements,
            int subListsElements){
        super(maxCapacityNodes, clearFactorNodes, fragmentFactorNodes, 
                swap, quantitySwaperThreadNodes, subListsNodes,
                maxCapacityElements, clearFactorElements, fragmentFactorElements, 
                swap, quantitySwaperThreadElements, subListsElements, 
                new StringTreeNodes<T>());
    }    
}
