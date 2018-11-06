package calypte.buffer;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import junit.framework.TestCase;

public class VirtualByteArrayTest extends TestCase{

	private VirtualByteArray array;
	
	public void setUp() throws IOException {
		array = new VirtualByteArray(1*1024*1024, 256*1024*1024, 8*1024, new File("./test.dta"));
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

		int arraylen  = (int)(array.dataSize - (array.dataSize & array.blockMask));
		
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
		int blockSize = (int)(1 << array.blockBitDesc);
		int dataSize  = (int)(array.dataSize - (array.dataSize & array.blockMask));

		byte[] diff = new byte[blockSize];
		byte[] data = new byte[dataSize];
		byte[] val  = new byte[dataSize];

		r.nextBytes(diff);
		r.nextBytes(data);

		//escreve os dados do array
		array.write(data, 0, 0, data.length);
		array.write(diff, 0, data.length, diff.length);

		//verifica os dados da memória
		byte[] mem  = new byte[dataSize];
		System.arraycopy(data, 0, mem, 0, data.length);
		System.arraycopy(diff, 0, mem, 0, diff.length);
		array.memory.read(array.dataOffset, val, 0, val.length);
		assertArrayEquals(mem, val);
		
		//Verifica os dados do arquivo
		byte[] fileData = new byte[blockSize];
		byte[] fileVal  = new byte[blockSize];
		
		System.arraycopy(data, 0, fileData, 0, fileData.length);
		
		array.file.seek(0);
		array.file.read(fileVal, 0, fileVal.length);
		assertEquals(blockSize, array.file.length());
		assertArrayEquals(fileData, fileVal);
		

		//Verifica todos os dados do array
		byte[] fullData  = new byte[dataSize + blockSize];
		byte[] fullVal   = new byte[dataSize + blockSize];
		
		System.arraycopy(data, 0, fullData, 0, data.length);
		System.arraycopy(diff, 0, fullData, data.length, diff.length);
		
		array.read(0, fullVal, 0, fullVal.length);
		assertArrayEquals(fullData, fullVal);
		
	}
	
	public void testWriteBuffer() throws IOException {
		
		Random r = new Random();
		long bufferSize   = array.size();
		int blockSize     = (int)(1 << array.blockBitDesc) + 1;
		int lastBlockSize = (int)(bufferSize % blockSize);
		
		byte[] data  = new byte[blockSize];
		byte[] lData = new byte[lastBlockSize];

		r.nextBytes(data);
		r.nextBytes(lData);

		long off = 0;
		
		while(bufferSize - off > 0) {
			
			if(bufferSize - off > data.length) {
				array.write(data, 0, off, data.length);
				off += data.length;
			}
			else {
				array.write(lData, 0, off, lData.length);
				off += lData.length;
			}
			
		}
		
		assertEquals(0, bufferSize - off);

		byte[] val  = new byte[blockSize];
		byte[] lVal  = new byte[lastBlockSize];

		off = 0;
		
		while(bufferSize - off > 0) {
			
			if(bufferSize - off > data.length) {
				array.read(off, val, 0, val.length);
				assertArrayEquals("offset: " + off, data, val);
				off += data.length;
			}
			else {
				array.read(off, lVal, 0, lVal.length);
				assertArrayEquals("offset: " + off, lData, lVal);
				off += lData.length;
			}
			
		}
		
	}
	
}
