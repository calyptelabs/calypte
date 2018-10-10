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

/**
 * 
 * @author Ribeiro
 *
 */
public class CharNodeUtil {
	
	public static final char[] CHAR_MAP;
	
	public static int MIN_CHARGROUP    			= 0x5b;

    public static int MAX_CHARGROUP    			= 0x7d;

    public static int LEN_CHARGROUP    			= MAX_CHARGROUP - MIN_CHARGROUP;

    public static int MIN_NUMBERGROUP  			= 0x21;

    public static int MAX_NUMBERGROUP  			= 0x3f;

    public static int LEN_NUMBERGROUP  			= MAX_NUMBERGROUP - MIN_NUMBERGROUP;

	public static int MIN_CHAR2GROUP    		= 0xe0;

	public static int MAX_CHAR2GROUP    		= 0xff;
	
	public static int LEN_CHAR2GROUP    		= MAX_CHAR2GROUP - MIN_CHAR2GROUP;
    
	public static int LEN_NODES        			= LEN_NUMBERGROUP + LEN_CHARGROUP + LEN_CHAR2GROUP; 

    public static int DATA_SIZE        			= LEN_NODES*8 + 16; 

    public static final long MAX_NODES 			= Long.MAX_VALUE / DATA_SIZE;
	
    static {
    	CHAR_MAP = new char[LEN_NODES];
    	
    	for(int i=0;i<LEN_NUMBERGROUP;i++) {
    		CHAR_MAP[i] = (char)(MIN_NUMBERGROUP + i);
    	}
    	
    	for(int i=0;i<LEN_CHARGROUP;i++) {
    		CHAR_MAP[i + LEN_NUMBERGROUP] = (char)(MIN_CHARGROUP + i);
    	}
    	
    	for(int i=0;i<LEN_CHAR2GROUP;i++) {
    		CHAR_MAP[i + LEN_NUMBERGROUP + LEN_CHARGROUP] = (char)(MIN_CHAR2GROUP + i);
    	}
    	
    }
    
	public static int toIndex(char c) {
		
		int index = c/* & 0xff*/;
		
		if(index < MIN_CHARGROUP && index < MIN_NUMBERGROUP && index < MIN_CHAR2GROUP)
			return -1;
		
		if(index > MAX_CHARGROUP && index > MAX_NUMBERGROUP &&  index > MAX_CHAR2GROUP)
			return -1;

		if(index <= MAX_NUMBERGROUP)
			index = index - MIN_NUMBERGROUP;
		else
		if(index <= MAX_CHARGROUP)
			index = LEN_NUMBERGROUP + (index - MIN_CHARGROUP);
		else
			index = LEN_NUMBERGROUP + LEN_CHARGROUP + (index - MIN_CHAR2GROUP);
		
		return index;
	}

	public static char toMap(int index) {
		
		if(index > LEN_NODES || LEN_NODES < 0)
			return 0;
		
		if(index >= LEN_NUMBERGROUP + LEN_CHARGROUP) {
			return (char)(MIN_CHAR2GROUP + index - LEN_NUMBERGROUP - LEN_CHARGROUP);
		}
		if(index >= LEN_NUMBERGROUP) {
			return (char)(MIN_CHARGROUP + index - LEN_NUMBERGROUP);
		}
		else
			return (char)(MIN_NUMBERGROUP + index);
		
	}
	
}
