package calypte.buffer;

public class VirtualSegmentMapping {

	private ByteArray data;

	private Table table;
	
	private Entry e;
	
	private List free;
	
	private List itens;
	
	private int hashMask;
	
	private int size;
	
	private int tableSize;
	
	private int tableOffset;

	private int entryOffset;
	
	public VirtualSegmentMapping(ByteArray data, int tableOffset, int size) {
		this.size        = size;
		this.tableSize   = size >> 4;
		this.hashMask    = getHashMask(this.tableSize);
		this.tableOffset = tableOffset;
		this.entryOffset = this.tableSize;
		this.data        = data;
		
		this.free  = new List();
		this.itens = new List();
		this.e     = new Entry();
		this.table = new Table();
		
		this.createFreeMap();
	}
	
	private void createFreeMap(){
		int tableIndex = tableSize >> 2;
		for(int i=0;i<tableIndex;i++) {
			this.table.setRootIndex(i, -1);
		}
		
		int indexSize = (size - entryOffset) >> 5;
		for(int i=0;i<indexSize;i++) {
			this.free.add(i);
		}
	}
	
	public long put(long key, long value) {
		return table.putEntry(key, value);
	}

	public long get(long key) {
		return table.getEntry(key);
	}

	public boolean remove(long key) {
		return table.remove(key);
	}
	
	private int getRMSB(int n){ 
        int position = 1; 
        int m = 1; 
  
        while ((n & m) == 0) { 
            m = m << 1; 
            position++; 
        } 
        return position; 
    }

	private int getHashMask(int n){
		int hm = 0;
		
		while(hm < n) {
			hm |= hm << 1 | 1;
		}
		
		hm = hm >> 1;
		return hm;
		
    }
	
	private class Table {
		
		public long getEntry(long key) {
			int root  = getRootIndex(key);
			int index = root;
			
			for(;index != -1 && e.getKey(index) != key;index = e.getNext(index));
			
			return index == -1? -1 : e.getValue(index);
		}

		public long putEntry(long key, long value) {
			
			long oldValue = -1;
			int root      = getRootIndex(key);
			int index     = root;
			
			for(;index != -1 && e.getKey(index) != key;index = e.getNext(index));
			
			
			if(index == -1) {
				index = free.removeFirst();
				
				if(index == -1) {
					index = itens.removeFirst();
					
					int previous = e.getPrevious(index);
					int next     = e.getNext(index);
					long oldKey  = e.getKey(index);
					oldValue     = e.getValue(index);
					if(previous == -1) {
						
						if(next != -1) {
							e.setPrevious(next, -1);
							setRootIndex(oldKey, index);
						}
						else {
							setRootIndex(oldKey, -1);
						}
					}
					else {
						e.setNext(previous, next);
						e.setPrevious(next, previous);
					}
					
				}
				
				e.setKey(index, key);
				e.setValue(index, value);
				
				if(root == -1) {
					setRootIndex(key, index);
					e.setNext(index, -1);
					e.setPrevious(index, -1);
				}
				else {
					e.setNext(index, root);
					e.setPrevious(index, -1);
				}
			}
			else {
				e.setValue(index, value);
			}
			
			return oldValue;
		}
		
		public boolean remove(long key) {
			
			int root    = getRootIndex(key);
			int index   = root;
			
			for(;index != -1 && e.getKey(index) != key;index = e.getNext(index));
			
			if(index == -1) {
				return false;
			}
			
			int previous  = e.getPrevious(index);
			int next      = e.getNext(index);
			
			if(previous == -1) {
				
				if(next != -1) {
					e.setPrevious(next, -1);
					setRootIndex(key, next);
				}
				else {
					setRootIndex(key, -1);
				}
			}
			else {
				e.setNext(previous, next);
				e.setPrevious(next, previous);
			}
			
			free.add(index);
			
			return true;
		}
		
		private int getRootIndex(long key) {
			int hash    = (int)(key & hashMask);
			long offset = tableOffset + (hash << 2);
			return data.readInt(offset);
		}
		
		private void setRootIndex(long key, int index) {
			int hash    = (int)(key & hashMask);
			long offset = tableOffset + (hash << 2);
			data.writeInt(offset, index);
		}
		
	}
	
	public class Entry{
		
		private static final int PREVIOUS_OFFSET = 0;
		
		private static final int NEXT_OFFSET     = 4;

		private static final int KEY_OFFSET      = 8;
		
		private static final int VALUE_OFFSET    = 16;
		
		public int getNext(int index) {
			int offset = entryOffset + (index << 5) + NEXT_OFFSET;
			return data.readInt(offset);
		}

		public int getPrevious(int index) {
			int offset = entryOffset + (index << 5) + PREVIOUS_OFFSET;
			return data.readInt(offset);
		}

		public long getKey(int index) {
			int offset = entryOffset + (index << 5) + KEY_OFFSET;
			return data.readLong(offset);
		}
		
		public long getValue(int index) {
			int offset = entryOffset + (index << 5) + VALUE_OFFSET;
			return data.readLong(offset);
		}

		public void setNext(int index, int value) {
			int offset = entryOffset + (index << 5) + NEXT_OFFSET;
			data.writeInt(offset, value);
		}

		public void setPrevious(int index, int value) {
			int offset = entryOffset + (index << 5) + PREVIOUS_OFFSET;
			data.writeInt(offset, value);
		}

		public void setKey(int index, long value) {
			int offset = entryOffset + (index << 5) + KEY_OFFSET;
			data.writeLong(offset, value);
		}
		
		public void setValue(int index, long value) {
			int offset = entryOffset + (index << 5) + VALUE_OFFSET;
			data.writeLong(offset, value);
		}
		
	}
	
	public class List {

		private static final int PREVIOUS_ITEM_OFFSET  = 24;
		
		private static final int NEXT_ITEM_OFFSET      = 28;
		
		private int first;
		
		public List() {
			this.first = -1;
		}
		
		public int removeFirst() {
			
			if(first == -1) {
				return -1;
			}
			
			int off = first;
			remove(first);
			return off;
		}
		
	    protected void add(int off){
	        
	        if(first == -1){
	        	first = off;
	        	setNext(first, first);
	        	setPrevious(first, first);
	        }
	        else{
	            int lastOff = getPrevious(first);

	        	setNext(off, first);
	        	setPrevious(off, lastOff);

	        	setPrevious(first, off);
	        	setNext(lastOff, off);
	        }
	        
	    }

	    protected void remove(int off){
	    	int previous = getPrevious(off);
	    	int next     = getNext(off);
	    	
	        if(first == off){
	            if(first == next)
	            	first = -1;
	            else{
	            	first = next;
	            	setNext(previous, next);
	            	setPrevious(next, previous);
	            }
	        }
	        else{
	        	setNext(previous, next);
	        	setPrevious(next, previous);
	        }
	    }
		
		private int getNext(int index) {
			long off = entryOffset + (index << 5) + NEXT_ITEM_OFFSET; 
			return data.readInt(off);
		}

		private void setNext(int index, int value) {
			long off = entryOffset + (index << 5) + NEXT_ITEM_OFFSET; 
			data.writeInt(off, value);
		}

		private int getPrevious(int index) {
			long off = entryOffset + (index << 5) + PREVIOUS_ITEM_OFFSET; 
			return data.readInt(off);
		}

		private void setPrevious(int index, int value) {
			long off = entryOffset + (index << 5) + PREVIOUS_ITEM_OFFSET; 
			data.writeInt(off, value);
		}
		
	}
	
}
