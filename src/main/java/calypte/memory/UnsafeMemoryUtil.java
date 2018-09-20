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

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * 
 * @author Ribeiro
 *
 */
@SuppressWarnings("restriction")
public class UnsafeMemoryUtil {

	private static final Unsafe UNSAFE;
	
	private static final long UNSAFE_COPY_THRESHOLD;

    private static final long arrayBaseOffset;
	
    static {
        try{
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        UNSAFE_COPY_THRESHOLD = 1024L * 1024L;
        arrayBaseOffset       = (long)UNSAFE.arrayBaseOffset(byte[].class);
    }

	public static long alloc(long size){
		return UNSAFE.allocateMemory(size);
	}

	public static long realloc(long address, long size){
		return UNSAFE.reallocateMemory(address, size);
	}
	
	public static void free(long address){
		UNSAFE.freeMemory(address);		
	}
	
	public static void arrayCopy(long origin, long originOff, long dest, long destOff, long len){
		UNSAFE.copyMemory(origin + originOff, dest + destOff, len);
	}
	
	public static byte getByte(long address, long off){
		return UNSAFE.getByte(address + off);
	}
	
    public static void arrayCopy(byte[] src, long srcPos, long dstAddr, long dstPos, long length){
		
    	srcPos  += arrayBaseOffset;
		dstAddr += dstPos;
		
		while(length > 0) {
			long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
			UNSAFE.copyMemory(src, srcPos, null, dstAddr, size);
			length  -= size;
			srcPos  += size;
			dstAddr += size;
		}
    }

    public static void arrayCopy(long srcAddr, long srcPos, byte[] dst, long dstPos, long length){
    	
    	dstPos  += arrayBaseOffset;
    	srcAddr += srcPos;
		
		while(length > 0) {
			long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
			UNSAFE.copyMemory(null, srcAddr, dst, dstPos, size);
			length  -= size;
			dstPos  += size;
			srcAddr += size;
		}
    }
    
	public static long getAddress(Object o) {
		Object[] array = new Object[] {o};
		long baseOffset     = UNSAFE.arrayBaseOffset(Object[].class);
		long dataBaseOffset = UNSAFE.arrayBaseOffset(o.getClass());
		int addressSize     = UNSAFE.addressSize();
		
		long address;
		switch (addressSize){
			case 4:
				address = normalize(UNSAFE.getInt(array, baseOffset)) + dataBaseOffset;
				break;
			case 8:
				address = UNSAFE.getLong(array, baseOffset) + dataBaseOffset;
				break;
			default:
				throw new Error("unsupported address size: " + addressSize);
		}
		return address;
	}
		
	public static Object getObject(long address) {
	    Object[] array = new Object[] {null};
	    long baseOffset = UNSAFE.arrayBaseOffset(Object[].class);
	    UNSAFE.putLong(array, baseOffset, address);
	    return array[0];
	}
	
	private static long normalize(int value) {
	    if(value >= 0) return value;
	    return (~0L >>> 32) & value;
	}	
	
}
