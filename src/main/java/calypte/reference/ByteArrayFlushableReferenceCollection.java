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

package calypte.reference;

import calypte.buffer.Buffer;
import calypte.collections.FlushableReferenceCollection;

/**
 * 
 * @author Ribeiro
 *
 * @param <T>
 */
public class ByteArrayFlushableReferenceCollection<T> 
	implements FlushableReferenceCollection<T>{

	private static final long serialVersionUID = -4992359882040193153L;

	private static final long INVALID_OFFSET = 0xffffffffffffffffL;
	
	private static final byte IN_USE      = Byte.parseByte("10000000", 2);

	private static final byte NOT_IN_USE  = Byte.parseByte("00000000", 2);
	
	private static final byte NOT_EMPTY   = Byte.parseByte("01000000", 2);

	private long segment;
	
	private long free;
	
	private Buffer buffer;
	
	private long offset;
	
	private Transcoder<T> transcoder;
	
	private long size;
	
	public ByteArrayFlushableReferenceCollection(Buffer buffer, Transcoder<T> transcoder) {
		this.buffer     = buffer;
		this.segment    = buffer.alloc();
		this.transcoder = transcoder;
		this.offset     = 0;
		this.free       = INVALID_OFFSET;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}

	public long insert(T e) {
		
		long off = INVALID_OFFSET;
		
		try {
			off = getOffset();
			buffer.writeByte(off, buffer.readByte(off) | NOT_EMPTY);
			transcoder.encode(e, buffer, off + 1);
			return off;
		}
		catch(Throwable ex) {
			if(off != -1)
				release(off);
			throw new IllegalStateException(ex);
		}
		
	}
	
	public T set(long reference, T value) {
		
		byte b = buffer.readByte(reference);
		
		if((b & IN_USE) == 0) {
			throw new IllegalStateException("reference: " + reference);
		}
		
		T old = (b & NOT_EMPTY) != 0? transcoder.decode(buffer, reference + 1) : null;
		
		if(value == null) {
			buffer.writeByte(reference, b & ~NOT_EMPTY);
		}
		else {
			transcoder.encode(value, buffer, reference + 1);
			buffer.writeByte(reference, b | NOT_EMPTY);
		}
		
		return old;
	}

	public T get(long reference) {
		
		byte b = buffer.readByte(reference);
		
		if((b & IN_USE) == 0) {
			throw new IllegalStateException("reference: " + reference);
		}
		
		return (b & NOT_EMPTY) != 0? transcoder.decode(buffer, reference + 1) : null;
	}

	public boolean remove(long reference) {
		byte b = buffer.readByte(reference);
		
		if((b & IN_USE) == 0) {
			return false;
		}

		release(reference);
		return true;
	}

	public long length() {
		return size;
	}

	public boolean replace(long reference, T oldValue, T value) {
		
		byte b = buffer.readByte(reference);
		
		if((b & IN_USE) == 0) {
			throw new IllegalStateException("reference: " + reference);
		}
		
		T old = (b & NOT_EMPTY) != 0? transcoder.decode(buffer, reference + 1) : null;
		
		if(value == old || value.equals(old)) {
			
			if(value == null) {
				buffer.writeByte(reference, b & ~IN_USE);
			}
			else {
				transcoder.encode(value, buffer, reference + 1);
				buffer.writeByte(reference, b | IN_USE);
			}
			
			return true;
		}
		
		return false;
	}

	public T replace(long reference, T value) {
		
		byte b = buffer.readByte(reference);
		
		if((b & IN_USE) == 0) {
			throw new IllegalStateException("reference: " + reference);
		}
		
		T old = (b & NOT_EMPTY) != 0? transcoder.decode(buffer, reference + 1) : null;
		
		if(value == null) {
			buffer.writeByte(reference, b & ~NOT_EMPTY);
		}
		else {
			transcoder.encode(value, buffer, reference + 1);
			buffer.writeByte(reference, b | NOT_EMPTY);
		}
		
		return old;
	}

	public T putIfAbsent(long reference, T value) {
		
		byte b = buffer.readByte(reference);
		
		if((b & IN_USE) == 0) {
			throw new IllegalStateException("reference: " + reference);
		}
		
		if((b & NOT_EMPTY) != 0) {
			T old = transcoder.decode(buffer, reference + 1);
			return old;
		}
		else {
			transcoder.encode(value, buffer, reference + 1);
			buffer.writeByte(reference, b | NOT_EMPTY);
			return null;
		}
		
	}

	public boolean remove(long reference, T oldValue) {
		
		byte b = buffer.readByte(reference);
		
		if((b & IN_USE) == 0) {
			return false;
		}
		
		T old = (b & NOT_EMPTY) != 0? transcoder.decode(buffer, reference + 1) : null;
		
		if(oldValue == old || oldValue.equals(old)) {
			release(reference);
			return true;
		}
		
		return false;
	}

	public void destroy() {
	}

	public void clear() {
		buffer.releaseAll();
	}

	public void setReadOnly(boolean value) {
		throw new UnsupportedOperationException();
	}

	public boolean isReadOnly() {
		throw new UnsupportedOperationException();
	}

	public void setDeleteOnExit(boolean value) {
		throw new UnsupportedOperationException();
	}

	public boolean isDeleteOnExit() {
		throw new UnsupportedOperationException();
	}

	public void flush() {
		throw new UnsupportedOperationException();
	}

	private synchronized long getOffset() {
		
		long off;
		
		if(free != INVALID_OFFSET) {
			long offset = free;
			free = buffer.readLong(free + 1);
			off = offset;
		}
		
		if(offset + transcoder.size() > buffer.getBlockSize()) {
			segment = buffer.alloc();
			offset  = 0;
			off = segment;
		}
		else {
			off = segment + offset;
			offset += transcoder.size();
		}

		buffer.writeByte(off, IN_USE);
		
		return off;
	}

	private synchronized void release(long off) {
		
		buffer.writeByte(off, NOT_IN_USE);
		
		if(free == -1) {
			free = off;
		}
		else {
			buffer.writeLong(off + 1, free);
			free = off;
		}
	}
	
}
