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

/**
 * 
 * @author Ribeiro
 *
 */
class StatisticsThread implements Runnable{

    private long lastRead;
    
    private long lastWrite;
    
    private long read;
    
    private long write;
	
    private ConcurrentCache cache;
    
    private long currentReadings;
    
    private long currentWritten;
    
    private long currentTotalMemory;
    
    private long currentFreeMemory;
    
    private long currentMemory;
    
    public StatisticsThread(ConcurrentCache cache){
        this.lastRead  = 0;
        this.lastWrite = 0;
        this.read      = 0;
        this.write     = 0;
        this.cache     = cache;
    }
    
	public void run() {
        Runtime runtime = Runtime.getRuntime();
		while(true){
			try{
                this.currentWritten     = write-lastWrite;
                this.currentReadings    = (read-lastRead);
				this.currentTotalMemory = runtime.totalMemory();
				this.currentFreeMemory  = runtime.freeMemory();
				this.currentMemory      = runtime.totalMemory() - runtime.freeMemory();
                lastRead                = cache.getCountRead();
                lastWrite               = cache.getCountWrite();
                Thread.sleep(1000);
                read                    = cache.getCountRead();
                write                   = cache.getCountWrite();
				
			}
			catch(Throwable e){
				if(!(e instanceof InterruptedException)){
					e.printStackTrace();
				}
			}
		}
	}

	public long getCurrentReadings() {
		return currentReadings;
	}

	public long getCurrentWritten() {
		return currentWritten;
	}

	public long getCurrentTotalMemory() {
		return currentTotalMemory;
	}

	public long getCurrentFreeMemory() {
		return currentFreeMemory;
	}

	public long getCurrentMemory() {
		return currentMemory;
	}

}
