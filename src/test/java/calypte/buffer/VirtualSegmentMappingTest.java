package calypte.buffer;

import java.text.DecimalFormat;

import junit.framework.TestCase;

public class VirtualSegmentMappingTest extends TestCase{

	private static DecimalFormat df = new DecimalFormat("###,###,###,###,###,###,###.000");

	private VirtualSegmentMapping vsm;
	
	private ByteArray memory;
	
	private long mappingOffset;
	
	private int maxItens;
	
	private long bufferSize;
	
	private long tableSize;
	
	public void setUp() {
		bufferSize = 1024;
		memory = new HeapByteArray(bufferSize);
		mappingOffset = 0;
		maxItens = (int)(bufferSize - bufferSize*0.1) >> 6;
		vsm = new VirtualSegmentMapping(memory, mappingOffset, maxItens, bufferSize);
		tableSize = vsm.tableSize >> VirtualSegmentMapping.ITEM_TABLE_BITS_LENGTH;		
	}
	
	public void tearDown() {
	}
	
	public void testSinglePut() {
		//insere um item
		assertEquals(0, vsm.allocSegment(1 << 32));
		
		//verifica se o item foi inserido corretamente
		assertEquals(0, vsm.get(1 << 32));
	}

	public void testFullPut() {
		
		//Ocupa todo o local de armazenamento dos itens
		for(int i=0;i<maxItens;i++) {
			assertEquals(i, vsm.allocSegment(i << 32));
		}

		//Verifica se todos os itens foram inseridos corretamente
		for(int i=0;i<maxItens;i++) {
			assertEquals(i, vsm.get(i << 32));
		}
		
	}

	public void testOverrideFirstPut() {
		
		long itens = maxItens + 1;
		
		//Insere um item a mais da capacidade da tabela
		//Isso força a remoção do primeiro item inserido
		for(int i=0;i<itens;i++) {
			assertEquals(i % maxItens, vsm.allocSegment(i << 32));
		}

		//Verifica se todos os itens foram inseridos corretamente e o primeiro for removido.
		for(int i=0;i<itens;i++) {
			if(i == 0)
				assertEquals(-1, vsm.get(i << 32));
			else
				assertEquals(i % maxItens, vsm.get(i << 32));
		}
		
	}
	
	public void testOneRoot() {
		long tableSize = vsm.tableSize >> VirtualSegmentMapping.ITEM_TABLE_BITS_LENGTH; //Tamanho da tabela de itens
				
		//verifica se a tabela não foi iniciada
		for(int i=0;i<tableSize;i++) {
			assertEquals(-1, vsm.table.getRootIndex(i));
		}

		//insere todos os itens na posição 0 da tabela
		for(int i=0;i<maxItens;i++) {
			long k = i*tableSize;
			assertEquals(i, vsm.allocSegment(k));
			assertEquals(i, vsm.get(k));
		}

		//Verifica se todos foram inseridos na posição 0
		assertTrue(vsm.table.getRootIndex(0) != -1);
		for(int i=1;i<tableSize;i++) {
			assertEquals(-1, vsm.table.getRootIndex(i));
		}
		
		//Verifica se os valores foram inseridos corretamente
		for(int i=0;i<maxItens;i++) {
			long k = i*tableSize;
			assertEquals(i, vsm.get(k));
		}
		
	}

	public void testRemoveFirstRoot() {
		
		for(int i=0;i<4;i++) {
			long k = i*tableSize;
			assertEquals(i, vsm.allocSegment(k));
			assertEquals(i, vsm.get(k));
		}

		for(int i=0;i<4;i++) {
			long k = i*tableSize;
			assertEquals(i, vsm.get(k));
		}
		
		assertTrue(vsm.remove(3*tableSize));

		for(int i=0;i<4;i++) {
			long k = i*tableSize;
			if(i == 3)
				assertEquals(-1, vsm.get(k));
			else
				assertEquals(i, vsm.get(k));
		}
	}

	public void testRemoveLastRoot() {
		
		for(int i=0;i<4;i++) {
			long k = i*tableSize;
			assertEquals(i, vsm.allocSegment(k));
			assertEquals(i, vsm.get(k));
		}

		for(int i=0;i<4;i++) {
			long k = i*tableSize;
			assertEquals(i, vsm.get(k));
		}
		
		assertTrue(vsm.remove(0*tableSize));

		for(int i=0;i<4;i++) {
			long k = i*tableSize;
			if(i == 0)
				assertEquals(-1, vsm.get(k));
			else
				assertEquals(i, vsm.get(k));
		}

	}

	public void testRemoveMiddleRoot() {
		
		for(int i=0;i<4;i++) {
			long k = i*tableSize;
			assertEquals(i, vsm.allocSegment(k));
			assertEquals(i, vsm.get(k));
		}

		for(int i=0;i<4;i++) {
			long k = i*tableSize;
			assertEquals(i, vsm.get(k));
		}
		
		assertTrue(vsm.remove(2*tableSize));

		for(int i=0;i<4;i++) {
			long k = i*tableSize;
			if(i == 2)
				assertEquals(-1, vsm.get(k));
			else
				assertEquals(i, vsm.get(k));
		}

		
	}

	public void testPerformance(){
		VirtualSegmentMapping vsm = 
				new VirtualSegmentMapping(new HeapByteArray(32*1024*1024), 0, 943718, 32*1024*1024);
		
		long total = 0;
		int ops    = 1000000;
		
		for(int i=0;i<ops;i++){
			long nanoStart = System.nanoTime();
			vsm.allocSegment(i << 2);
			long nanoEnd = System.nanoTime();
			total += nanoEnd - nanoStart;
		}
		
		double timeOp = total / ops;
		double opsSec = 1000000000 / timeOp;
		
		System.out.println("VSM operations put: " + ops + ", time: " + total + " nano, ops/Sec: " + df.format(opsSec) );
		
		for(int i=0;i<ops;i++){
			long nanoStart = System.nanoTime();
			vsm.get(i << 2);
			long nanoEnd = System.nanoTime();
			total += nanoEnd - nanoStart;
		}
		
		timeOp = total / ops;
		opsSec = 1000000000 / timeOp;
		
		System.out.println("VSM operations read: " + ops + ", time: " + total + " nano, ops/Sec: " + df.format(opsSec) );
		
	}

}
