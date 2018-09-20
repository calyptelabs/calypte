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

import calypte.DataMap;

/**
 * 
 * @author Ribeiro
 *
 */
public class DataMapEntityFileDataHandler
	implements EntityFileDataHandler<DataMap, byte[], DataMapEntityFileHeader>{

	private int recordSize;
	
	private byte[] buffer;

	private byte[] empty;
	
	public DataMapEntityFileDataHandler(){
		this.recordSize = 63;
		this.buffer = new byte[this.recordSize - 1];
		this.empty  = new byte[this.recordSize - 1];
	}
	
	public void writeMetaData(DataWritter stream, DataMapEntityFileHeader value)
			throws IOException {
	}

	public DataMapEntityFileHeader readMetaData(DataReader srteam)
			throws IOException {
		return null;
	}

	public void writeEOF(DataWritter stream) throws IOException {
		stream.writeByte((byte)-1);
	}

	public void write(DataWritter stream, DataMap entity)
			throws IOException {
		if(entity == null){
			stream.writeByte((byte)0);
			stream.write(empty);
		}
		else{
			stream.writeByte((byte)1);
			stream.writeLong(entity.getId());
			stream.writeLong(entity.getTimeToLive());
			stream.writeLong(entity.getCreationTime());
			stream.writeLong(entity.getTimeToIdle());
			stream.writeLong(entity.getFirstSegment());
			stream.writeLong(entity.getLength());
			stream.writeLong(entity.getMostRecentTime());
			stream.writeInt(entity.getSegments());
			stream.writeShort(entity.getFlag());
		}
	}

	public void writeRaw(DataWritter stream, byte[] entity) throws IOException {
		stream.write(entity);
	}

	public DataMap read(DataReader stream) throws IOException {
		byte e = stream.readByte();
		if(e == 0){
			stream.read(buffer);
			return null;
		}
		else{
			DataMap b = new DataMap();
			b.setId(stream.readLong());
			b.setTimeToLive(stream.readLong());
			b.setCreationTime(stream.readLong());
			b.setTimeToIdle(stream.readLong());
			b.setFirstSegment(stream.readLong());
			b.setLength(stream.readLong());
			b.setMostRecentTime(stream.readLong());
			b.setSegments(stream.readInt());
			b.setFlag(stream.readShort());
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

	public Class<DataMap> getType() {
		return DataMap.class;
	}

	public Class<byte[]> getRawType() {
		return byte[].class;
	}

}
