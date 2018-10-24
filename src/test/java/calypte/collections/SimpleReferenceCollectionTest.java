package calypte.collections;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import calypte.CacheException;
import junit.framework.TestCase;

public class SimpleReferenceCollectionTest extends TestCase{

	private File tmpPath;
	
	public void setUp() {
		tmpPath = new File(System.getProperty("user.dir"), "tmp");
		tmpPath.mkdirs();
		System.setProperty("java.io.tmpdir", tmpPath.getAbsolutePath());
	}
	
	public void tearDown() {
	}
	
	@SuppressWarnings("serial")
	public void testNotSwap() {
		
		final Map<Long,Long> values = new HashMap<Long, Long>();
		
		SimpleReferenceCollection<Long> src = 
				new SimpleReferenceCollection<Long>(
						100, 
						5f/100f, 
						new Swapper<Long>() {

							public void sendItem(long index, Entry<Long> item) throws CacheException {
								values.put(index, item.getIndex());
							}

							public Entry<Long> getItem(long index) throws CacheException {
								return new Entry<Long>(index, values.get(index));
							}

							public void clear() throws CacheException {
								values.clear();
							}

							public void destroy() throws CacheException {
							}
							
						});
		
		for(int i=0;i<100;i++) {
			src.insert(new Long(i));
		}
		
		assertEquals(0, values.size());
		
		for(int i=0;i<100;i++) {
			assertEquals(i, src.get(i).intValue());
		}
		
	}

	@SuppressWarnings("serial")
	public void testSwap() {
		
		final Map<Long,Long> values = new HashMap<Long, Long>();
		
		SimpleReferenceCollection<Long> src = 
				new SimpleReferenceCollection<Long>(
						100, 
						5f/100f, 
						new Swapper<Long>() {

							public void sendItem(long index, Entry<Long> item) throws CacheException {
								values.put(index, item.getIndex());
							}

							public Entry<Long> getItem(long index) throws CacheException {
								return new Entry<Long>(index, values.get(index));
							}

							public void clear() throws CacheException {
								values.clear();
							}

							public void destroy() throws CacheException {
							}
							
						});
		
		for(int i=0;i<200;i++) {
			src.insert(new Long(i));
		}
		
		assertEquals(100, values.size());
		
		for(int i=0;i<200;i++) {
			assertEquals(i, src.get(i).intValue());
		}
		
	}

	@SuppressWarnings({ "serial", "static-access" })
	public void testFree() {
		
		final Map<Long,Long> values = new HashMap<Long, Long>();
		
		SimpleReferenceCollection<Long> src = 
				new SimpleReferenceCollection<Long>(
						100, 
						5f/100f, 
						new Swapper<Long>() {

							public void sendItem(long index, Entry<Long> item) throws CacheException {
								values.put(index, item.getIndex());
							}

							public Entry<Long> getItem(long index) throws CacheException {
								return new Entry<Long>(index, values.get(index));
							}

							public void clear() throws CacheException {
								values.clear();
							}

							public void destroy() throws CacheException {
							}
							
						});
		
		for(int i=0;i<SimpleReferenceCollection.GROUP_SIZE*4;i++) {
			src.insert(new Long(i));
		}

		for(int i=0;i<SimpleReferenceCollection.GROUP_SIZE*4;i++) {
			assertEquals(i, src.get(i).intValue());
		}

		for(int i=SimpleReferenceCollection.GROUP_SIZE*4 - 1;i>=0;i--) {
			src.remove(i);
		}
		
		assertTrue(new File(tmpPath, (src.FreeManagerIDS - 1) + "_0" + src.FILE_TYPE).exists());
		assertTrue(new File(tmpPath, (src.FreeManagerIDS - 1) + "_1" + src.FILE_TYPE).exists());
		assertTrue(new File(tmpPath, (src.FreeManagerIDS - 1) + "_2" + src.FILE_TYPE).exists());
		assertFalse(new File(tmpPath, (src.FreeManagerIDS - 1) + "_3" + src.FILE_TYPE).exists());
		
		for(int i=0;i<SimpleReferenceCollection.GROUP_SIZE*4;i++) {
			src.insert(new Long(i));
		}
		
		for(int i=0;i<SimpleReferenceCollection.GROUP_SIZE*4;i++) {
			assertEquals(i, src.get(i).intValue());
		}
		
	}
	
}
