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

import java.io.IOException;
import java.io.InputStream;


/**
 * Representa o fluxo de bytes de um item de um cache 
 * permitindo manipular seus metadados.
 * 
 * @author Ribeiro
 *
 */
public class ItemCacheInputStream 
	extends CacheInputStream{

	private InputStream stream;
	
	public ItemCacheInputStream(ItemCacheMetadata metadata, InputStream stream){
		this(
			metadata.getId(),
			metadata.getTimeToLive(), 
			metadata.getTimeToIdle(),
			metadata.getCreationTime(), 
			metadata.getMostRecentTime(), 
			metadata.getFlag(), 
			metadata.getSize(),
			stream);
	}
	
	public ItemCacheInputStream(long id, long timeToLive, 
			long timeToIdle, long creationTime, long mostRecentTime, 
			short flag, long size, InputStream stream){
		super();
		this.setMap(
			new DataMap(id, timeToLive, creationTime, 
					timeToIdle, flag, -1, 0, size, mostRecentTime));
		this.stream = stream;
	}
	
    @Override
    public int read(byte[] bytes, int i, int i1) throws IOException {
        return this.stream.read(bytes, i, i1);
    }
    
    @Override
    public int read() throws IOException {
    	return this.stream.read();
    }
	
}
