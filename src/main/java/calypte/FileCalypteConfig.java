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

import java.io.File;
import java.io.FileInputStream;

/**
 * Define a configuração de um cache a partir de um arquivo.
 * <pre>
 * ex:
 *     Configuration configuration = ...;
 *     Cache cache = new Cache(new PropertiesBRCacheConfig(configuration));
 * </pre>
 * @author Ribeiro
 *
 */
public class FileCalypteConfig 
	extends PropertiesCalypteConfig{

	private static final long serialVersionUID = 7110634582865791095L;

	public FileCalypteConfig(File f) {
		Configuration c = new Configuration();
		FileInputStream fin = null;
		try{
			c.load(fin);
			apply(c);
		}
		catch(Throwable e){
			throw e instanceof IllegalStateException? 
					(IllegalStateException)e : 
					new IllegalStateException(e);
		}
		finally{
			if(fin != null){
				try{
					fin.close();
				}
				catch(Throwable ex){
				}
			}
		}
	}
    
}
