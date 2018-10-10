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

import calypte.collections.treehugemap.CharNode;
import calypte.collections.treehugemap.CharNodeUtil;

/**
 * 
 * @author Ribeiro
 *
 */
@SuppressWarnings("rawtypes")
public class CharNodeEntityFileDataHandler
	implements EntityFileDataHandler<CharNode, byte[], CharNodeEntityFileHeader>{

	private int recordSize;
	
	private byte[] buffer;

	private byte[] empty;
	
	public CharNodeEntityFileDataHandler(){
		this.recordSize = CharNodeUtil.DATA_SIZE + 1;
		this.buffer = new byte[this.recordSize - 1];
		this.empty  = new byte[this.recordSize - 1];
	}
	
	public void writeMetaData(DataWritter stream, CharNodeEntityFileHeader value)
			throws IOException {
	}

	public CharNodeEntityFileHeader readMetaData(DataReader srteam)
			throws IOException {
		return null;
	}

	public void writeEOF(DataWritter stream) throws IOException {
		stream.writeByte((byte)-1);
	}

	public void write(DataWritter stream, CharNode entity)
			throws IOException {
		if(entity == null){
			stream.writeByte((byte)0);
			stream.write(empty);
		}
		else{
			stream.writeByte((byte)1);
			stream.writeLong(entity.getId());
			stream.writeLong(entity.getValueId());
			
			for(long n: entity.getNextIndexNodes()){
				stream.writeLong(n);
			}
		}
	}

	public void writeRaw(DataWritter stream, byte[] entity) throws IOException {
		stream.write(entity);
	}

	public CharNode read(DataReader stream) throws IOException {
		byte e = stream.readByte();
		if(e == 0){
			stream.read(buffer);
			return null;
		}
		else{
			long[] nextNodes = new long[CharNodeUtil.LEN_NODES];
			
			long id = stream.readLong();
			long valueId = stream.readLong();
			
			for(int i=0;i<nextNodes.length;i++){
				nextNodes[i] = stream.readLong();
			}
			
			CharNode b = new CharNode(id, valueId, nextNodes);
			return b;
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

	public Class<CharNode> getType() {
		return CharNode.class;
	}

	public Class<byte[]> getRawType() {
		return byte[].class;
	}

}
