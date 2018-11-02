package calypte.buffer;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
		int blockSize = 1 << array.blockSHL;
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

		int maxSegs   = (int)(array.dataSize >> array.blockSHL);
		int blockSize = 1 << array.blockSHL;
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

		int maxSegs   = (int)(array.dataSize >> array.blockSHL);
		int blockSize = 1 << array.blockSHL;
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

	public void testWriteLastBytesBufferMemeory() throws IOException {

		long vOff    = (array.dataSize - array.dataOffset) - 1024;
		int bufSize  = 1024;

		Random r = new Random();
		
		byte[] data = new byte[bufSize];
		byte[] val  = new byte[bufSize];

		r.nextBytes(data);

		array.write(data, 0, vOff, data.length);

		assertEquals(0, array.file.length());
		array.memory.read(array.dataOffset + vOff, val, 0, val.length);
		assertArrayEquals(data, val);
		
	}
	
	public void testOverrideFirstSegment() throws IOException {

		long segs    = array.dataSize >> array.blockSHL;
		int segSize  = 1 << array.blockSHL;

		Random r = new Random();
		
		byte[] diff = new byte[segSize];
		byte[] data = new byte[segSize];

		r.nextBytes(diff);
		r.nextBytes(data);

		long off = 0;
		
		for(int i=0;i<segs;i++) {
			array.write(data, 0, off, data.length);
			off += segSize;
		}

		array.write(diff, 0, off, diff.length);
		
		assertEquals(1024, array.file.length());

		
		off = 0;
		byte[] val = new byte[segSize];
		
		for(int i=0;i<segs;i++) {
			array.memory.read(array.dataOffset + off, val, 0, val.length);
			assertTrue(Arrays.equals(val, data));
			off += segSize;
		}
		
		
	}
	
}
