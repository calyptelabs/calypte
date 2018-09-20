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

import calypte.tx.TXCache;

/**
 * 
 * @author Ribeiro
 *
 */
public class TXCacheHelper {

	public static abstract class ConcurrentTask extends Thread{
		
		private Throwable error;
		
		private TXCache cache;
		
		private Object value;
		
		private String key;
		
		private Object value2;
		
		public ConcurrentTask(TXCache cache, String key,
				Object value, Object value2) {
			this.cache = cache;
			this.value = value;
			this.key = key;
			this.value2 = value2;
		}

		public void run(){
			try{
				this.execute(cache, key, value, value2);
			}
			catch(Throwable e){
				this.error = e; 
			}
		}

		protected abstract void execute(TXCache cache, String key, Object value,
				Object value2) throws Throwable;
		
		public Throwable getError() {
			return error;
		}
		
	}
}
