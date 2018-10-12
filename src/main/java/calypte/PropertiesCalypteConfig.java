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

import calypte.memory.Memory;

/**
 * 
 * @author Ribeiro
 *
 */
public class PropertiesCalypteConfig extends CalypteConfig{

	private static final long serialVersionUID = 7110634582865791095L;

	private static final String MEMORY_PREFIX = "calypte.memory.";

	private static final String MEMORY_SUFFIX = "Memory";

	private Configuration configuration;
	
	public PropertiesCalypteConfig(){
	}

	public PropertiesCalypteConfig(Configuration config) {
		apply(config);
	}
	
    protected void apply(Configuration config){
    	
    	this.configuration = config;
    	
    	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    	
    	this.configuration   = config;
        this.nodesBufferSize = config.getLong(CacheConstants.NODES_BUFFER_SIZE,		"1m");
        this.nodesPageSize   = config.getLong(CacheConstants.NODES_PAGE_SIZE,		"5k");
        this.indexBufferSize = config.getLong(CacheConstants.INDEX_BUFFER_SIZE,		"1m");
        this.indexPageSize   = config.getLong(CacheConstants.INDEX_PAGE_SIZE,		"1k");
        this.dataBufferSize  = config.getLong(CacheConstants.DATA_BUFFER_SIZE,		"64m");
        this.dataBlockSize   = config.getLong(CacheConstants.DATA_BLOCK_SIZE,		"1k");
        this.dataPageSize    = config.getLong(CacheConstants.DATA_PAGE_SIZE,		"8k");
        this.maxSizeEntry    = config.getLong(CacheConstants.MAX_SIZE_ENTRY,		"1m");
        this.maxSizeKey      = config.getInt(CacheConstants.MAX_SIZE_KEY,			"100");
        this.swapperThread   = config.getInt(CacheConstants.SWAPPER_THREAD,			"4");
        this.dataPath        = config.getString(CacheConstants.DATA_PATH,			"/mnt/calypte");
        this.memory          = createMemory(config.getString(CacheConstants.MEMORY_ACCESS_TYPE,"heap"), classLoader);
        
    }

    /**
     * Obtém os metadados de configuração.
     * @return metadados.
     */
	public Configuration getConfiguration() {
		return configuration;
	}
    
    @SuppressWarnings("unchecked")
    protected Memory createMemory(String name, ClassLoader classLoader){
    	try{
        	String className = 
        			MEMORY_PREFIX + 
        			Character.toUpperCase(name.charAt(0)) + name.substring(1, name.length()).toLowerCase() +
        			MEMORY_SUFFIX;
        	
        	Class<Memory> clazz = (Class<Memory>)Class.forName(className, true, classLoader);
    		return clazz.newInstance();
    	}
    	catch(Throwable e){
    		throw new IllegalStateException("invalid memory type: " + name, e);
    	}
    }
	
}
