package calypte.buffer;

import java.io.File;
import java.io.IOException;

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
		array.memory.read(0, tmp, 0, 5);
		assertEquals("TESTE", new String(tmp));
	}
}
