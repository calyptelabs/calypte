package calypte.buffer;

import junit.framework.TestCase;

public class VirtualSegmentMappingTest extends TestCase{

	public void testSinglePut() {
		VirtualSegmentMapping vsm = 
				new VirtualSegmentMapping(new HeapByteArray(1024), 0, 1024);

		//insere um item
		vsm.put(1 << 32, 1 << 12);
		
		//verifica se o item foi inserido corretamente
		assertEquals(1 << 12, vsm.get(1 << 32));
	}

	public void testFullPut() {
		VirtualSegmentMapping vsm = 
				new VirtualSegmentMapping(new HeapByteArray(1024), 0, 1024);

		int maxItens = (vsm.size - vsm.entryOffset) >> 5; //Tamanho da tabela de itens
		
		//Ocupa todo o local de armazenamento dos itens
		for(int i=0;i<maxItens;i++) {
			vsm.put(i << 32, i << 24);
		}

		//Verifica se todos os itens foram inseridos corretamente
		for(int i=0;i<maxItens;i++) {
			assertEquals(i << 24, vsm.get(i << 32));
		}
		
	}

	public void testOverrideFirstPut() {
		VirtualSegmentMapping vsm = 
				new VirtualSegmentMapping(new HeapByteArray(1024), 0, 1024);

		int maxItens = ((vsm.size - vsm.entryOffset) >> 5) + 1; //Tamanho da tabela de itens
		
		//Insere um item a mais da capacidade da tabela
		//Isso força a remoção do primeiro item inserido
		for(int i=0;i<maxItens;i++) {
			vsm.put(i << 32, i << 24);
		}

		//Verifica se todos os itens foram inseridos corretamente e o primeiro for removido.
		for(int i=0;i<maxItens;i++) {
			if(i == 0)
				assertEquals(-1, vsm.get(i << 32));
			else
				assertEquals(i << 24, vsm.get(i << 32));
		}
		
	}
	
	public void testOneRoot() {
		VirtualSegmentMapping vsm = 
				new VirtualSegmentMapping(new HeapByteArray(1024), 0, 1024);

		int maxItens = (vsm.size - vsm.entryOffset) >> 5; //Quantidade máxima de itens
		int tableSize = vsm.tableSize >> 2; //Tamanho da tabela de itens
				
		//verifica se a tabela não foi iniciada
		for(int i=0;i<tableSize;i++) {
			assertEquals(-1, vsm.table.getRootIndex(i));
		}

		//insere todos os itens na posição 0 da tabela
		for(int i=0;i<maxItens;i++) {
			int k = i*((vsm.tableSize >> 2));
			vsm.put(k, i << 1);
			assertEquals(i << 1, vsm.get(k));
		}

		//Verifica se todos foram inseridos na posição 0
		assertTrue(vsm.table.getRootIndex(0) != -1);
		
		for(int i=1;i<tableSize;i++) {
			assertEquals(-1, vsm.table.getRootIndex(i));
		}
		
		//Verifica se os valores foram inseridos corretamente
		for(int i=0;i<maxItens;i++) {
			int k = i*((vsm.tableSize >> 2));
			assertEquals(i << 1, vsm.get(k));
		}
		
	}

	public void testRemoveFirstRoot() {
		VirtualSegmentMapping vsm = 
				new VirtualSegmentMapping(new HeapByteArray(1024), 0, 1024);

		for(int i=0;i<4;i++) {
			int k = i*((vsm.tableSize >> 2));
			vsm.put(k, i << 1);
			assertEquals(i << 1, vsm.get(k));
		}

		for(int i=0;i<4;i++) {
			int k = i*((vsm.tableSize >> 2));
			assertEquals(i << 1, vsm.get(k));
		}
		
		assertTrue(vsm.remove(3*(vsm.tableSize >> 2)));

		for(int i=0;i<4;i++) {
			int k = i*((vsm.tableSize >> 2));
			if(i == 3)
				assertEquals(-1, vsm.get(k));
			else
				assertEquals(i << 1, vsm.get(k));
		}
	}

	public void testRemoveLastRoot() {
		VirtualSegmentMapping vsm = 
				new VirtualSegmentMapping(new HeapByteArray(1024), 0, 1024);

		for(int i=0;i<4;i++) {
			int k = i*((vsm.tableSize >> 2));
			vsm.put(k, i << 1);
			assertEquals(i << 1, vsm.get(k));
		}

		for(int i=0;i<4;i++) {
			int k = i*((vsm.tableSize >> 2));
			assertEquals(i << 1, vsm.get(k));
		}
		
		assertTrue(vsm.remove(0*(vsm.tableSize >> 2)));

		for(int i=0;i<4;i++) {
			int k = i*((vsm.tableSize >> 2));
			if(i == 0)
				assertEquals(-1, vsm.get(k));
			else
				assertEquals(i << 1, vsm.get(k));
		}
		
		assertEquals(-1, vsm.get(0*(vsm.tableSize >> 2)));
	}

	public void testRemoveMiddleRoot() {
		VirtualSegmentMapping vsm = 
				new VirtualSegmentMapping(new HeapByteArray(1024), 0, 1024);

		for(int i=0;i<4;i++) {
			int k = i*((vsm.tableSize >> 2));
			vsm.put(k, i << 1);
			assertEquals(i << 1, vsm.get(k));
		}

		for(int i=0;i<4;i++) {
			int k = i*((vsm.tableSize >> 2));
			assertEquals(i << 1, vsm.get(k));
		}
		
		assertTrue(vsm.remove(2*(vsm.tableSize >> 2)));

		for(int i=1;i<4;i++) {
			int k = i*((vsm.tableSize >> 2));
			if(i == 2)
				assertEquals(-1, vsm.get(k));
			else
				assertEquals(i << 1, vsm.get(k));
		}
		
	}
	
}
