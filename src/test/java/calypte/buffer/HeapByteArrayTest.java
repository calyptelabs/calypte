package calypte.buffer;

import junit.framework.TestCase;

public class HeapByteArrayTest extends TestCase{

	public void testWriteByte() {
		HeapByteArray hba = new HeapByteArray(64);
		hba.writeByte(0, Byte.MIN_VALUE);
		assertEquals(Byte.MIN_VALUE, hba.readByte(0));
		
		hba.writeByte(0, Byte.MAX_VALUE);
		assertEquals(Byte.MAX_VALUE, hba.readByte(0));
		
		hba.writeByte(0, Byte.MIN_VALUE >> 1);
		assertEquals(Byte.MIN_VALUE >> 1, hba.readByte(0));
		
		hba.writeByte(0, Byte.MAX_VALUE >> 1);
		assertEquals(Byte.MAX_VALUE >> 1, hba.readByte(0));
	}

	public void testWriteShort() {
		HeapByteArray hba = new HeapByteArray(64);
		hba.writeShort(0, Short.MIN_VALUE);
		assertEquals(Short.MIN_VALUE, hba.readShort(0));
		
		hba.writeShort(0, Short.MAX_VALUE);
		assertEquals(Short.MAX_VALUE, hba.readShort(0));
		
		hba.writeShort(0, Short.MIN_VALUE >> 1);
		assertEquals(Short.MIN_VALUE >> 1, hba.readShort(0));
		
		hba.writeShort(0, Short.MAX_VALUE >> 1);
		assertEquals(Short.MAX_VALUE >> 1, hba.readShort(0));
	}

	public void testWriteInt() {
		HeapByteArray hba = new HeapByteArray(64);
		hba.writeInt(0, Integer.MIN_VALUE);
		assertEquals(Integer.MIN_VALUE, hba.readInt(0));
		
		hba.writeInt(0, Integer.MAX_VALUE);
		assertEquals(Integer.MAX_VALUE, hba.readInt(0));
		
		hba.writeInt(0, Integer.MIN_VALUE >> 1);
		assertEquals(Integer.MIN_VALUE >> 1, hba.readInt(0));
		
		hba.writeInt(0, Integer.MAX_VALUE >> 1);
		assertEquals(Integer.MAX_VALUE >> 1, hba.readInt(0));
	}

	public void testWriteLong() {
		HeapByteArray hba = new HeapByteArray(64);
		hba.writeLong(0, Long.MIN_VALUE);
		assertEquals(Long.MIN_VALUE, hba.readLong(0));
		
		hba.writeLong(0, Long.MAX_VALUE);
		assertEquals(Long.MAX_VALUE, hba.readLong(0));
		
		hba.writeLong(0, Long.MIN_VALUE >> 1);
		assertEquals(Long.MIN_VALUE >> 1, hba.readLong(0));
		
		hba.writeLong(0, Long.MAX_VALUE >> 1);
		assertEquals(Long.MAX_VALUE >> 1, hba.readLong(0));
	}

	public void testWriteByteMultipleSegments() {
		HeapByteArray hba = new HeapByteArray(32, 8);
		
		for(int i=0;i<8;i++) {
			hba.writeByte(i, Byte.MIN_VALUE);
			assertEquals(Byte.MIN_VALUE, hba.readByte(i));
			
			hba.writeByte(i, Byte.MAX_VALUE);
			assertEquals(Byte.MAX_VALUE, hba.readByte(i));
			
			hba.writeByte(i, Byte.MIN_VALUE >> 1);
			assertEquals(Byte.MIN_VALUE >> 1, hba.readByte(i));
			
			hba.writeByte(i, Byte.MAX_VALUE >> 1);
			assertEquals(Byte.MAX_VALUE >> 1, hba.readByte(i));
		}
	}

	public void testWriteShortMultipleSegments() {
		HeapByteArray hba = new HeapByteArray(32, 8);
		
		for(int i=0;i<8;i++) {
			hba.writeShort(i, Short.MIN_VALUE);
			assertEquals(Short.MIN_VALUE, hba.readShort(i));
			
			hba.writeShort(i, Short.MAX_VALUE);
			assertEquals(Short.MAX_VALUE, hba.readShort(i));
			
			hba.writeShort(i, Short.MIN_VALUE >> 1);
			assertEquals(Short.MIN_VALUE >> 1, hba.readShort(i));
			
			hba.writeShort(i, Short.MAX_VALUE >> 1);
			assertEquals(Short.MAX_VALUE >> 1, hba.readShort(i));
		}
	}

	public void testWriteIntMultipleSegments() {
		HeapByteArray hba = new HeapByteArray(32, 8);
		
		for(int i=0;i<8;i++) {
			hba.writeInt(i, Integer.MIN_VALUE);
			assertEquals(Integer.MIN_VALUE, hba.readInt(i));
			
			hba.writeInt(i, Integer.MAX_VALUE);
			assertEquals(Integer.MAX_VALUE, hba.readInt(i));
			
			hba.writeInt(i, Integer.MIN_VALUE >> 1);
			assertEquals(Integer.MIN_VALUE >> 1, hba.readInt(i));
			
			hba.writeInt(i, Integer.MAX_VALUE >> 1);
			assertEquals(Integer.MAX_VALUE >> 1, hba.readInt(i));
		}
	}

	public void testWriteLongMultipleSegmets() {
		HeapByteArray hba = new HeapByteArray(64, 8);

		for(int i=0;i<8;i++) {
			hba.writeLong(i, Long.MIN_VALUE);
			assertEquals(Long.MIN_VALUE, hba.readLong(i));
			
			hba.writeLong(i, Long.MAX_VALUE);
			assertEquals(Long.MAX_VALUE, hba.readLong(i));
			
			hba.writeLong(i, Long.MIN_VALUE >> 1);
			assertEquals(Long.MIN_VALUE >> 1, hba.readLong(i));
			
			hba.writeLong(i, Long.MAX_VALUE >> 1);
			assertEquals(Long.MAX_VALUE >> 1, hba.readLong(i));
		}

		
	}
	
}
