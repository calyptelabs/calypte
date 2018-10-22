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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * @author Ribeiro
 *
 */
public class RouletteLock {

	private Lock[] locks;

	public RouletteLock(){
		this(20);
	}
	
	public RouletteLock(int size){
		this.locks = new Lock[size];
		for(int i=0;i<size;i++){
			this.locks[i] = new ReentrantLock();
		}
	}
	
	public Lock getLock(long value){
		return this.locks[(int)(value % this.locks.length)];
	}
	
}
