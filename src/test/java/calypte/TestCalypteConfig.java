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

import calypte.memory.DirectMemory;

/**
 * 
 * @author Ribeiro
 *
 */
public class TestCalypteConfig 
	extends CalypteConfig{

	private static final long serialVersionUID = -8953971203516129784L;

	public TestCalypteConfig(){

        this.nodesBufferSize = 1*1024*1024;
        this.nodesPageSize   = 1024;
        this.indexBufferSize = 32*1024*1024;
        this.indexPageSize   = 1024;
        this.dataBufferSize  = 32*1024*1024;
        this.dataBlockSize   = 1024;
        this.dataPageSize    = 2*1024;
        this.maxSizeEntry    = 1024*1024;
        this.maxSizeKey      = 100;
        this.swapperThread   = 4;
        this.dataPath        = "/mnt/calypte";
        this.memory          = new DirectMemory();
	}
	
}
