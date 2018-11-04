package calypte.buffer;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import junit.framework.TestCase;

public class VirtualByteArrayTest extends TestCase{

	private VirtualByteArray array;
	
	public void setUp() throws IOException {
		array = new VirtualByteArray(1*1024*1024, 256*1024*1024, 1*1024, new File("./test.dta"));
	}
	
	public void tearDown() {
		array = null;
	}
	
	public void testWriteBytes() throws IOException {
		array.write("TESTE".getBytes(), 0, 0, 5);
		assertEquals(0, array.file.length());
		
		byte[] tmp = new byte[5];
		array.memory.read(array.dataOffset, tmp, 0, 5);
		assertEquals("TESTE", new String(tmp));
	}
	
	public void testWriteTwoBlocks() throws IOException {
		int blockSize = 1 << array.blockBitDesc;
		int arraylen  = blockSize << 1; 
		
		byte[] val  = new byte[arraylen];
		byte[] data = new byte[arraylen];
		
		Random r = new Random();
		r.nextBytes(data);
		
		array.write(data, 0, 0, data.length);
		
		assertEquals(0, array.file.length());
		
		array.memory.read(array.dataOffset, val, 0, val.length);
		assertArrayEquals(val, data);
	}

	public void testWriteWithoutLastSegment() throws IOException {

		int maxSegs   = (int)(array.dataSize >> array.blockBitDesc);
		int blockSize = 1 << array.blockBitDesc;
		int arraylen  = blockSize*(maxSegs - 1);
		
		byte[] val  = new byte[arraylen];
		byte[] data = new byte[arraylen];
		
		Random r = new Random();
		r.nextBytes(data);
		
		array.write(data, 0, 0, data.length);
		
		assertEquals(0, array.file.length());
		
		array.memory.read(array.dataOffset, val, 0, val.length);
		assertArrayEquals(val, data);
		
	}

	public void testWriteAllsSegments() throws IOException {

		int maxSegs   = (int)(array.dataSize >> array.blockBitDesc);
		int blockSize = 1 << array.blockBitDesc;
		int arraylen  = blockSize*maxSegs;
		
		byte[] val  = new byte[arraylen];
		byte[] data = new byte[arraylen];
		
		Random r = new Random();
		r.nextBytes(data);
		
		array.write(data, 0, 0, data.length);
		
		assertEquals(0, array.file.length());
		
		array.memory.read(array.dataOffset, val, 0, val.length);
		assertArrayEquals(val, data);
		
	}
	
	public void testWriteMemoryOnly() throws IOException {

		int arraylen  = (int)array.dataSize;
		
		byte[] val  = new byte[arraylen];
		byte[] data = new byte[arraylen];
		
		Random r = new Random();
		r.nextBytes(data);
		
		array.write(data, 0, 0, data.length);
		
		assertEquals(0, array.file.length());
		
		array.memory.read(array.dataOffset, val, 0, val.length);
		assertArrayEquals(val, data);		
		
	}

	public void testWriteLastBytesMemoryBuffer() throws IOException {

		int blockSize = 1 << array.blockBitDesc;
		long vOff     = array.dataSize - blockSize;
		long reallOff = array.dataOffset + (vOff & array.blockMask);
		Random r = new Random();
		
		byte[] data = new byte[blockSize];
		byte[] val  = new byte[blockSize];

		r.nextBytes(data);

		array.write(data, 0, vOff, data.length);

		assertEquals(0, array.file.length());
		
		array.memory.read(reallOff, val, 0, val.length);
		assertArrayEquals(data, val);
		
	}
	
	public void testOverrideFirstSegment() throws IOException {
		Random r = new Random();
		
		byte[] diff = new byte[1 << array.blockBitDesc];
		byte[] data = new byte[(int)array.dataSize];
		byte[] val  = new byte[(int)array.dataSize];

		r.nextBytes(diff);
		r.nextBytes(data);

		array.write(data, 0, 0, data.length);
		array.write(diff, 0, data.length, diff.length);

		System.arraycopy(diff, 0, data, 0, diff.length);
		
		array.memory.read(array.dataOffset, val, 0, val.length);
		
		assertEquals(1024, array.file.length());
		
		assertArrayEquals(data, val);
		
	}
	
}
