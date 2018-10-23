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

/**
 * 
 * @author Ribeiro
 *
 */
public class TestCalypteConfig 
	extends PropertiesCalypteConfig{

	private static final long serialVersionUID = -8953971203516129784L;

	public TestCalypteConfig(){
        apply(new Configuration());
        System.setProperty("java.io.tmpdir", super.getDataPath() + File.separator + "tmp");
	}
	
}
