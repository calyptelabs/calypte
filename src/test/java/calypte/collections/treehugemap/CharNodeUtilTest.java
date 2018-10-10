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

package calypte.collections.treehugemap;

import junit.framework.TestCase;

/**
 * 
 * @author Ribeiro
 *
 */
public class CharNodeUtilTest extends TestCase{

	public void testAllMap() {
		
		for(int i=0;i<CharNodeUtil.CHAR_MAP.length;i++) {
			char c = CharNodeUtil.toMap(i);
			assertEquals(CharNodeUtil.CHAR_MAP[i], c);
		}
		
	}
	
	public void testMap() {
		
		for(char c: CharNodeUtil.CHAR_MAP) {
			int i   = CharNodeUtil.toIndex(c);
			char nc = CharNodeUtil.toMap(i);
			assertEquals(c, nc);
		}
		
	}
	
}
