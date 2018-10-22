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

import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Ribeiro
 *
 */
public class BasicCacheHandlerCleanTask implements Runnable{

	private CacheHandler handler;
	
	public BasicCacheHandlerCleanTask(CacheHandler handler) {
		this.handler = handler;
	}
	
	public void run() {
		while(!handler.isDestroyed()) {
			try {
				Thread.sleep(TimeUnit.SECONDS.toMillis(10));
				clean();
			}
			catch(Throwable e) {
				if(!handler.isDestroyed()) {
					e.printStackTrace();
				}
			}
		}
		
		//gc
		handler = null;
	}

	private void clean() {
		handler.find(new Find(null));
	}
	
	private class Find extends AbstractFindCacheHandler{

		public Find(String key) {
			super(key);
		}

		public boolean acceptNodeKey(Object key) {
			try {
				Thread.sleep(100);
			}
			catch(Throwable e) {
				throw new IllegalStateException(e);
			}
			return super.acceptNodeKey(key);
		}
		
		@Override
		protected void found(String request, String key, DataMap value) {
			
			if(handler.isDestroyed()) {
				throw new IllegalStateException();
			}
			
			handler.removeIfInvalid(key);
		}
		
	}
}
