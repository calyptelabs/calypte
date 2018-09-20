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

package calypte.tx;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import calypte.CacheErrors;
import calypte.CacheException;
import calypte.CacheHandler;

/**
 * 
 * @author Ribeiro
 *
 */
public final class CacheHandlerTransactionFactory {

	public static TransactionCacheHandler createCacheHandler(
			CacheHandler cache, CacheTransactionManager transactionManager){
    	return (TransactionCacheHandler)Proxy.newProxyInstance(
    			cache.getClass().getClassLoader(), 
					new Class[]{TransactionCacheHandler.class}, 
					new CacheHandlerTransactionInvocationHandler(cache, transactionManager)
    			);
	}
	
	private static class CacheHandlerTransactionInvocationHandler implements InvocationHandler{

		private CacheHandler cache;
		
		private CacheTransactionManager transactionManager;
		
		public CacheHandlerTransactionInvocationHandler(
				CacheHandler cache, CacheTransactionManager transactionManager){
			this.cache = cache;
			this.transactionManager = transactionManager;
		}
		
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			CacheTransactionHandler currentTx = transactionManager.getCurrrent(false);
	    	CacheTransactionHandler tx        = 
	    			currentTx == null? 
	    					transactionManager.begin(cache) : 
	    					currentTx;
	    	
	    	try{
	    		Object r = method.invoke(tx, args);
				if(currentTx == null){
					tx.commit();
				}
				return r;
	    	}
	    	catch(InvocationTargetException e){
	    		
	    		Throwable ex = e.getTargetException();
	    		
	    		try{
	    			if(currentTx == null){
	    				tx.rollback();
	    			}
	    		}
				catch(Throwable x){
		    		throw new CacheException(
	    				new Exception(
							"bug: exception not recognized (rollback fail): " + x.toString(), ex), 
							CacheErrors.ERROR_1018);
					
				}
	    		
	    		if(ex instanceof CacheException){
	        		throw ex;
	    		}
	    		else{
	        		throw new CacheException(
	        				new Exception("bug: exception not recognized: ", e), 
	        				CacheErrors.ERROR_1023
    				);
	    		}
	    		
	    	}
    	}
		
	}
}
