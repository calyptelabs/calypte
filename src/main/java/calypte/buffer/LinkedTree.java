package calypte.buffer;

public class LinkedTree {

	private static final int PREVIOUS_ITEM_OFFSET  = 0;
	
	private static final int NEXT_ITEM_OFFSET = 8;
	
	private HeapByteArray data;

	private long first;
	
    protected void add(long off){
        
        if(first == -1){
        	first = off;
        	setNext(first, first);
        	setPrevious(first, first);
        }
        else{
            long lastOff = getPrevious(first);

        	setNext(off, first);
        	setPrevious(off, lastOff);

        	setPrevious(first, off);
        	setNext(lastOff, off);
        }
        
    }

    protected void remove(long off){
    	long previous = getPrevious(off);
    	long next     = getNext(off);
    	
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
	
	private long getNext(long offset) {
		long off = offset + NEXT_ITEM_OFFSET; 
		return data.readLong(off);
	}

	private void setNext(long offset, long pointer) {
		long off = offset + NEXT_ITEM_OFFSET; 
		data.writeLong(off, pointer);
	}

	private long getPrevious(long offset) {
		long off = offset + PREVIOUS_ITEM_OFFSET; 
		return data.readLong(off);
	}

	private void setPrevious(long offset, long pointer) {
		long off = offset + PREVIOUS_ITEM_OFFSET; 
		data.writeLong(off, pointer);
	}
	
}
