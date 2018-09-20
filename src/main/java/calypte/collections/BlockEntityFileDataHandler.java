/*
 * Calypte http://calypte.uoutec.com.br/
 * Copyright (C) 2018 UoUTec. (calypte@uoutec.com.br)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package calypte.collections;

import java.io.IOException;

import org.brandao.entityfilemanager.DataReader;
import org.brandao.entityfilemanager.DataWritter;
import org.brandao.entityfilemanager.EntityFileDataHandler;

import calypte.Block;
import calypte.memory.Memory;
import calypte.memory.RegionMemory;

/**
 * 
 * @author Ribeiro
 *
 */
public class BlockEntityFileDataHandler 
	implements EntityFileDataHandler<Block, byte[], BlockEntityFileHeader>{

	private int blockSize;
	
	private int dataSize;
	
	private byte[] empty;
	
	private int recordSize;
	
	private Memory memory;
	
	public BlockEntityFileDataHandler(Memory memory, int blockSize){
		this.blockSize  = blockSize;
		this.recordSize = 25 + blockSize;
		this.dataSize   = this.recordSize - 1;
		this.empty      = new byte[this.dataSize];
		this.memory     = memory;
	}
	
	public void writeMetaData(DataWritter stream, BlockEntityFileHeader value)
			throws IOException {
	}

	public BlockEntityFileHeader readMetaData(DataReader srteam)
			throws IOException {
		return new BlockEntityFileHeader();
	}

	public void writeEOF(DataWritter stream) throws IOException {
		stream.writeByte((byte)-1);
	}

	public void write(DataWritter stream, Block entity) throws IOException {
		if(entity == null){
			stream.writeByte((byte)0);
			stream.write(empty);
		}
		else{
			byte[] b = new byte[blockSize]; 
			entity.buffer.read(0, b, 0, blockSize);
			
			stream.writeByte((byte)1);
			stream.writeLong(entity.id);
			stream.writeLong(entity.nextBlock);
			stream.writeInt(entity.length);
			stream.writeInt(entity.segment);
			stream.write(b);
		}
	}

	public void writeRaw(DataWritter stream, byte[] entity) throws IOException {
		stream.write(entity);
	}

	public Block read(DataReader stream) throws IOException {
		byte e = stream.readByte();
		if(e == 0){
			byte[] b = new byte[dataSize];
			stream.read(b);
			return null;
		}
		else{
			byte[] b = new byte[blockSize];
			
			long id = stream.readLong();
			long nextBlock = stream.readLong();
			int length     = stream.readInt();
			int segment    = stream.readInt();
			stream.read(b);
			
			RegionMemory rm = memory.alloc(blockSize);
			rm.write(0, b, 0, b.length);
			
			Block block = new Block(id, segment, rm, length);
			block.nextBlock = nextBlock;
			return block;
		}
	}

	public byte[] readRaw(DataReader stream) throws IOException {
		byte[] b = new byte[recordSize];
		stream.read(b);
		return b;
	}

	public long getFirstPointer() {
		return 0;
	}

	public int getHeaderLength() {
		return 0;
	}

	public int getRecordLength() {
		return recordSize;
	}

	public int getEOFLength() {
		return 1;
	}

	public int getFirstRecord() {
		return 0;
	}

	public Class<Block> getType() {
		return Block.class;
	}

	public Class<byte[]> getRawType() {
		return byte[].class;
	}

}
