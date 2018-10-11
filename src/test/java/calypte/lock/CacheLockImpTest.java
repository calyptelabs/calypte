package calypte.lock;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import calypte.lock.CacheLockImp;
import junit.framework.TestCase;


public class CacheLockImpTest extends TestCase{

	private static DecimalFormat df = new DecimalFormat("###,###,###,###,###,###,###.000");

	public void testCreateLock() throws InterruptedException {
		final CacheLockImp<String> cacheLock = new CacheLockImp<String>();
		final List<Integer> list = new ArrayList<Integer>();
		
		ReadWriteLock rwlock = cacheLock.getLock("teste");
		
		Lock lock = rwlock.writeLock();
		
		lock.lock();
		try {
			new Thread() {
				
				public void run() {
					ReadWriteLock rwlock = cacheLock.getLock("teste");
					Lock lock = rwlock.readLock();
					lock.lock();
					try {
						list.add(2);
					}
					finally {
						lock.unlock();
					}
					
					lock = null;
					rwlock = null;
					
					System.gc();

				}
				
			}.start();
			
			Thread.sleep(1000);
			
			list.add(1);
		}
		finally {
			lock.unlock();
		}
		
		lock = null;
		rwlock = null;
		
		System.gc();
		
		Thread.sleep(2000);
		
		assertEquals(1, list.get(0).intValue());
		assertEquals(2, list.get(1).intValue());
		
		assertNull(cacheLock.counter.get("teste"));
		assertNull(cacheLock.locks.get("teste"));
	}
	
	public void testPerformance() throws InterruptedException {
		
		final CacheLockImp<String> cacheLock = new CacheLockImp<String>();
		final AtomicInteger id = new AtomicInteger();
		
		for(int group=0;group<10;group++) {
			
			int ops                        = (group + 1)*10000;
			final AtomicLong total         = new AtomicLong();
			final CountDownLatch countDown = new CountDownLatch(ops);
			
			for(int th=0;th<ops;th++) {
				
				new Thread() {
					
					public void run() {
						String lockName = String.valueOf(id.incrementAndGet());
						long nanoStart = System.nanoTime();
						ReadWriteLock lock = cacheLock.getLock(lockName);
						long nanoEnd = System.nanoTime();
						total.addAndGet(nanoEnd - nanoStart);
						lock = null;
						countDown.countDown();
					}
					
				}.start();
				
			}
			
			countDown.await();
			
			double timeOp = total.get() / ops;
			double opsSec = 1000000000 / timeOp;
			
			System.out.println("operations: " + ops + ", time: " + total + " nano, ops/Sec: " + df.format(opsSec) + " md: " + timeOp );
			
			Thread.sleep(1000);
			
			System.gc();
		}
		
		
	}
	
}
