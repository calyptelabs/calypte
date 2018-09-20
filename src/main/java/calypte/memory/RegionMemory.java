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

package calypte.memory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * 
 * @author Ribeiro
 *
 */
public interface RegionMemory extends Serializable{

	long size();
	
	int read(long thisOff, byte[] buf, int off, int len);
	
	long read(long thisOff, RegionMemory buf, long off, long len);
	
	void read(OutputStream out, int off, int len) throws IOException;
	
	void write(long thisOff, byte[] buf, int off, int len);

	void write(long thisOff, RegionMemory buf, long off, long len);
	
	byte get(long off);
	
}
