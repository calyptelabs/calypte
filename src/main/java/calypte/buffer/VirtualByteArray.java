package calypte.buffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;

import calypte.buffer.VirtualSegmentMapping.Item;
import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class VirtualByteArray implements ByteArray{

	private static final Unsafe UNSAFE;
	
	private static final long BYTE_ARRAY_OFFSET;

	private static final long LONG_ARRAY_OFFSET;
	
    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
    		BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
    		LONG_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(long[].class);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
	private long size;
	
	protected ByteArray memory;
	
	protected VirtualSegmentMapping segmentMapping;
	
	protected int blockSize;
	
	protected long dataSize;
	
	protected long dataOffset;
	
	protected long mappingSize;
	
	protected long mappingOffset;
	
	protected int blockBitDesc;
	
	protected int blockMask;
	
	public VirtualByteArray(long bytesMemory, long size, int blockSize, File file) throws IOException {
		
		this.size           = size;
		
		//calcula o tamanho do bloco de dados
		this.blockBitDesc   = createBitDesloc(blockSize) - 1;
		this.blockSize      = 1 << this.blockBitDesc;
		this.blockMask      = this.blockSize - 1;

		//Calcula o tamanho da região que será usada para armazenar os dados.
		this.dataSize       = this.calculateDataSize(bytesMemory, blockSize);
		
		//Calcula o tamanho da região que será usada para armazenar as informações de mapeamento.
		this.mappingSize    = bytesMemory - this.dataSize;
		
		//Define o offset das informações de mapeamento.
		this.mappingOffset  = 0;
		
		//Define o offset dos dados.
		this.dataOffset     = this.mappingSize;
		
		//Cria o buffer usado para armazenar o mapeamento e os dados
		this.memory         = this.createByteArray(bytesMemory);

		//Cria o mapeamento dos segmentos.
		this.segmentMapping = this.createVirtualSegmentMapping();
		
		//Cria o arquivo de dump dos segmentos.
		file.createNewFile();
		this.file = new RandomAccessFile(file, "rw");
		this.file.setLength(0);
	}

	private VirtualSegmentMapping createVirtualSegmentMapping() {
		return
		new VirtualSegmentMapping(
				memory, 
				mappingOffset, 
				(int)(dataSize/blockSize), 
				mappingSize
			);
	}
	
	private long calculateDataSize(long total, long blockSize) {
		
		BigDecimal maxBlockUsage = 
			new BigDecimal(
				blockSize + 
				VirtualSegmentMapping.ITEM_LENGTH + 
				(VirtualSegmentMapping.ITEM_TABLE_LENGTH >> 1)
			);
		
		BigDecimal blockUsage = new BigDecimal(blockSize);
				
		BigDecimal dataUsage = 
			new BigDecimal(total)
				.divide(
					maxBlockUsage.divide(blockUsage, MathContext.DECIMAL128)
				, MathContext.DECIMAL128);
		
		return dataUsage.longValue();
	}
	
	protected ByteArray createByteArray(long bytesMemory) {
		return new HeapByteArray(bytesMemory);
	}
	
	public long readLong(long offset) {
		return readNumber(offset, 8);
	}

	public int readInt(long offset) {
		return (int)readNumber(offset, 4);
	}

	public short readShort(long offset) {
		return (short)readNumber(offset, 2);
	}

	public byte readByte(long offset) {
		return (byte)readNumber(offset, 1);
	}

	public long readNumber(long vOffset, int bytes) {
		long[] val       = new long[] {0};
		byte[] dta       = new byte[bytes];
		long blockOffset = vOffset % blockSize;
		long segment     = vOffset - blockOffset;
		
		if(blockOffset - bytes < 0) {
			int r1 = (int)(blockSize - blockOffset);
			int r2 = bytes - r1;

			synchronized(segmentMapping) {
				long offset = getSegment(segment);
				memory.read(dataOffset + offset + blockOffset, dta, 0, r1);
			}				
			
			segment += blockSize;
			
			synchronized (segmentMapping) {
				long offset = getSegment(segment);
				memory.read(dataOffset + offset, dta, r1, r2);
			}
		}
		else {
			synchronized(segmentMapping) {
				long offset = getSegment(segment);
				memory.read(dataOffset + offset, dta, 0, bytes);
			}
		}
		
		UNSAFE.copyMemory(dta, BYTE_ARRAY_OFFSET, val, LONG_ARRAY_OFFSET, bytes);
		return val[0];
	}
	
	public long read(long srcOff, RandomAccessFile dest, long destOff, long len) throws IOException{

		if(destOff + len > dest.length()) {
			throw new IndexOutOfBoundsException((destOff + len) + " > " + dest.length());
		}
		
		long vOff = destOff & blockMask;
		long vSeg = destOff ^ vOff;
		
		long maxCopy = size - srcOff;
		maxCopy = len > maxCopy? maxCopy : len;
		
		len = (int)maxCopy;
		
		long copy;
		long maxSegCopy;
		
		while(len > 0) {
			maxSegCopy = blockSize - vOff;
			copy       = maxSegCopy > len? len : maxSegCopy;
			
			synchronized(segmentMapping) {
				long offsetSeg = getSegment(vSeg);
				memory.read(dataOffset + offsetSeg + vOff, dest, destOff, (int)copy);
				
				Item item = segmentMapping.getItem(); 
				item.setNeedUpdate(offsetSeg >> blockBitDesc, true);
			}
			
			len     -= copy;
			srcOff  += copy;
			vOff     = 0;
			vSeg    += copy;
		}
		
		return maxCopy;
	}
	
	public int read(long srcOff, byte[] dest, int destOff, int len) {

		if(destOff + len > dest.length) {
			throw new IndexOutOfBoundsException((destOff + len) + " > " + dest.length);
		}
		
		long vOff = destOff & blockMask;
		long vSeg = destOff ^ vOff;
		
		long maxCopy = size - srcOff;
		maxCopy = len > maxCopy? maxCopy : len;
		
		len = (int)maxCopy;
		
		long copy;
		long maxSegCopy;
		
		while(len > 0) {
			maxSegCopy = blockSize - vOff;
			copy       = maxSegCopy > len? len : maxSegCopy;
			
			synchronized(segmentMapping) {
				long offsetSeg = getSegment(vSeg);
				memory.read(dataOffset + offsetSeg + vOff, dest, destOff, (int)copy);
				
				Item item = segmentMapping.getItem(); 
				item.setNeedUpdate(offsetSeg >> blockBitDesc, true);
			}
			
			len     -= copy;
			srcOff  += copy;
			vOff     = 0;
			vSeg    += copy;
		}
		
		return (int)maxCopy;
	}

	public void writeLong(long offset, long value) {
		writeNumber(offset, value, 8);
	}

	public void writeInt(long offset, long value) {
		writeNumber(offset, value, 4);
	}

	public void writeShort(long offset, long value) {
		writeNumber(offset, value, 2);
	}

	public void writeByte(long offset, long value) {
		writeNumber(offset, value, 1);
	}
	
	private void writeNumber(long vOffset, long value, int bytes) {
		long[] val       = new long[] {value};
		byte[] dta       = new byte[bytes];
		
		UNSAFE.copyMemory(val, LONG_ARRAY_OFFSET, dta, BYTE_ARRAY_OFFSET, bytes);
		write(dta, 0, vOffset, bytes);
	}

	public void write(RandomAccessFile src, long srcOff, long destOff, long len) throws IOException{

		if(destOff + len > size) {
			throw new IndexOutOfBoundsException((destOff + len) + " > " + size);
		}
		
		long vOff = destOff & blockMask;
		long vSeg = destOff ^ vOff;
		
		long copy;
		long maxSegCopy;
		
		while(len > 0) {
			maxSegCopy = blockSize - vOff;
			copy       = maxSegCopy > len? len : maxSegCopy;
			
			synchronized(segmentMapping) {
				long offsetSeg = getSegment(vSeg);
				memory.write(src, srcOff, dataOffset + offsetSeg + vOff, (int)copy);
				
				Item item = segmentMapping.getItem(); 
				item.setNeedUpdate(offsetSeg >> blockBitDesc, true);
			}
			
			len     -= copy;
			srcOff  += copy;
			vOff     = 0;
			vSeg    += copy;
		}				
		
	}
	
	public void write(byte[] src, int srcOff, long destOff, int len) {

		if(destOff + len > size) {
			throw new IndexOutOfBoundsException((destOff + len) + " > " + size);
		}
		
		long vOff = destOff & blockMask;
		long vSeg = destOff ^ vOff;

		long copy;
		long maxSegCopy;
		
		while(len > 0) {
			maxSegCopy = blockSize - vOff;
			copy       = maxSegCopy > len? len : maxSegCopy;
			
			synchronized(segmentMapping) {
				long offsetSeg = getSegment(vSeg);
				memory.write(src, srcOff, dataOffset + offsetSeg + vOff, (int)copy);
				
				Item item = segmentMapping.getItem(); 
				item.setNeedUpdate(offsetSeg >> blockBitDesc, true);
			}
			
			len     -= copy;
			srcOff  += copy;
			vOff     = 0;
			vSeg    += copy;
		}
		
	}

	public long size() {
		return size;
	}
	
	protected long getSegment(long vOffset) {
		
		long offset = segmentMapping.get(vOffset);
		
		if(offset == -1) {
			offset     = segmentMapping.allocSegment(vOffset);
			Item item  = segmentMapping.getItem(); 
			offset     = offset << blockBitDesc;
			reloadSegment(vOffset, offset, item);
		}
		else {
			offset = offset << blockBitDesc;
		}
		
		return offset;
	}
	
	protected RandomAccessFile file;
	
	protected void reloadSegment(long vOffset, long offset, Item item) {

		try {
			long index         = offset >> blockBitDesc;
			long oldVOffset    = item.getVOffset(index);
			boolean needUpdate = item.isNeedUpdate(index);
			
			if(needUpdate && oldVOffset != -1) {
				if(oldVOffset + blockSize > file.length()) {
					file.setLength(oldVOffset + blockSize);
				}
				memory.read(dataOffset + offset, file, oldVOffset, blockSize);
			}
			
			if(vOffset < file.length()) {
				memory.write(file, vOffset, dataOffset + offset, blockSize);
			}
			
			item.setVOffset(index, vOffset);
			item.setNeedUpdate(index, false);
		}
		catch(Throwable e) {
			throw new IllegalStateException(e);
		}
		
	}

	private int createBitDesloc(long value) {
		return getRMSB(value);
	}
	
	private int getRMSB(long n){ 
        int position = 1; 
        int m = 1; 
  
        while ((n & m) == 0) { 
            m = m << 1; 
            position++; 
        } 
        return position; 
    }

}
