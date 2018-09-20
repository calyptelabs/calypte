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

package calypte.memory;

/**
 * Gerencia a memória usada no cache.
 * 
 * @author Ribeiro
 *
 */
public interface Memory {
	
	/**
	 * Aloca uma quantidade específica de memória.
	 * 
	 * @param size Quantidade.
	 * @return Região da memória.
	 */
	RegionMemory alloc(long size);

	/**
	 * Realoca uma região da memória.
	 * @param size Quantidade.
	 * @param region Região da memória.
	 */
	void realloc(long size, RegionMemory region);
	
	/**
	 * Aloca uma quantidade específica de memória.
	 * 
	 * @param size Quantidade.
	 * @param region Região da memória.
	 */
	void alloc(long size, RegionMemory region);
	
	/**
	 * Libera uma região da memória.
	 * 
	 * @param region Região da memória.
	 */
	void release(RegionMemory region);
	
}
