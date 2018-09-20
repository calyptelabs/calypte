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

package calypte.collections.fileswapper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * 
 * @author Ribeiro
 *
 */
public abstract class AbstractDataBlockOutputStream 
	extends OutputStream{

    private int offset;
    
    private byte[] buffer;
	
    private int capacity;
    
    public AbstractDataBlockOutputStream(int blockSize){
        this.offset    = 0;
        this.capacity  = blockSize;
        this.buffer    = new byte[capacity];
    }
    
    public void write(byte[] buffer, int offset, int len) throws IOException{
    	
        int maxDesloc = offset + len;
        
        if(this.offset == this.capacity)
        	this.flush();
        
        while(offset != maxDesloc){
            int maxRead  = maxDesloc - offset;
            int maxWrite = this.capacity - this.offset;
            
            if(maxRead > maxWrite){
                System.arraycopy(buffer, offset, this.buffer, this.offset, maxWrite);
                offset += maxWrite;
                this.offset += maxWrite;
                this.flush();
            }
            else{
                System.arraycopy(buffer, offset, this.buffer, this.offset, maxRead);
                offset      += maxRead;
                this.offset += maxRead;
            }
        }
        
    }
	
	public void write(int b) throws IOException {
        this.write(new byte[]{(byte)(b & 0xff)}, 0, 1);
    }

    public void flush() throws IOException{
    	if(this.offset > 0){
	    	byte[] block = Arrays.copyOf(this.buffer, this.offset);
	    	DataBlock dataBlock = new DataBlock();
	    	dataBlock.setData(block);
	    	addBlock(dataBlock);
	        this.offset = 0;
    	}
    }
	
    public void close() throws IOException{
    	this.flush();
    }

    protected abstract void addBlock(DataBlock dataBlock);
    
}
