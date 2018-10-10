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

package calypte;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

import org.brandao.entityfilemanager.EntityFileAccess;
import org.brandao.entityfilemanager.EntityFileManagerConfigurer;
import org.brandao.entityfilemanager.EntityFileManagerImp;
import org.brandao.entityfilemanager.EntityFileTransactionFactory;
import org.brandao.entityfilemanager.LockProvider;
import org.brandao.entityfilemanager.LockProviderImp;
import org.brandao.entityfilemanager.SimpleEntityFileAccess;
import org.brandao.entityfilemanager.TransactionLog;
import org.brandao.entityfilemanager.tx.EntityFileTransactionFactoryImp;
import org.brandao.entityfilemanager.tx.EntityFileTransactionManagerConfigurer;
import org.brandao.entityfilemanager.tx.EntityFileTransactionManagerImp;
import org.brandao.entityfilemanager.tx.RecoveryTransactionLog;
import org.brandao.entityfilemanager.tx.RecoveryTransactionLogImp;
import org.brandao.entityfilemanager.tx.TransactionLogImp;

import calypte.HugeListCalculator.HugeListInfo;
import calypte.collections.BasicMapReferenceCollection;
import calypte.collections.BlockEntityFileDataHandler;
import calypte.collections.BlockEntityFileHeader;
import calypte.collections.CharNodeEntityFileDataHandler;
import calypte.collections.CharNodeEntityFileHeader;
import calypte.collections.DataMapEntityFileDataHandler;
import calypte.collections.DataMapEntityFileHeader;
import calypte.collections.EntityFileSwapper;
import calypte.collections.FlushableReferenceCollection;
import calypte.collections.FlushableReferenceCollectionImp;
import calypte.collections.MapReferenceCollection;
import calypte.collections.MapReferenceCollection.Find;
import calypte.collections.Swapper;
import calypte.collections.treehugemap.CharNode;
import calypte.collections.treehugemap.CharNodeUtil;
import calypte.collections.treehugemap.StringTreeNodes;
import calypte.collections.treehugemap.TreeNode;
import calypte.memory.Memory;
import calypte.memory.RegionMemory;

/**
 * 
 * @author Ribeiro
 *
 */
public class BasicCacheHandler implements CacheHandler{

    private static final long serialVersionUID                 = 8023029671447700902L;

    private static final int ENTRY_BINARY_SIZE                 = 48;
    
    private static final int NODE_BINARY_SIZE                  = CharNodeUtil.DATA_SIZE + ENTRY_BINARY_SIZE;

    private static final int INDEX_BINARY_SIZE                 = 58 + ENTRY_BINARY_SIZE;
    
    private static final Class<?> ITEM_CACHE_INPUTSTREAM_CLASS = ItemCacheInputStream.class;
    
    private Memory memory;
    
    protected MapReferenceCollection<String, DataMap> dataMap;

    protected FlushableReferenceCollection<Block> dataList;
    
    private int segmentSize;
    
    private long maxBytesToStorageEntry;
    
    private int maxLengthKey;
    
    private AtomicLong modCount;
    
    protected EntityFileManagerConfigurer entityFileManager;
    
	protected CalypteConfig config;
    
    AtomicLong countRead;
    
    AtomicLong countWrite;
    
    AtomicLong countRemoved;
    
    AtomicLong countReadData;

    AtomicLong countWriteData;

    AtomicLong countRemovedData;
    
    private boolean deleteOnExit;
    
    private boolean enabled;

    private volatile long creationTime;
    
    private BasicCacheHandlerCleanTask cleanTask;
    
    public BasicCacheHandler(String name, CalypteConfig config) throws CacheException{
    	this.config                 = config;
    	this.memory                 = config.getMemory();
        this.modCount               = new AtomicLong();
        this.segmentSize            = (int)config.getDataBlockSize();
        this.maxBytesToStorageEntry = config.getMaxSizeEntry();
        this.maxLengthKey           = config.getMaxSizeKey();
        this.deleteOnExit           = true;
    	this.entityFileManager      = this.createEntityFileManager(config);
        this.dataList               = this.createDataBuffer(name, this.entityFileManager, config);
        this.dataMap                = this.createDataMap(name, this.entityFileManager, config);
        this.enabled                = true;
        this.creationTime           = System.currentTimeMillis();
        this.countRead              = new AtomicLong();
        this.countWrite             = new AtomicLong();
        this.countRemoved           = new AtomicLong();
        this.countReadData          = new AtomicLong();
        this.countWriteData         = new AtomicLong();
        this.countRemovedData       = new AtomicLong();
        this.cleanTask              = new BasicCacheHandlerCleanTask(this);
        
        Thread th = new Thread(null, this.cleanTask, "clean cache task");
        th.start();
    }
    
    private EntityFileManagerConfigurer createEntityFileManager(CalypteConfig config){
    	try{
    		if(config.getEntityFileManager() != null){
    			return (EntityFileManagerConfigurer)config.getEntityFileManager();
    		}
    		
			File path   = new File(config.getDataPath(), "data");
			File txPath = new File(config.getDataPath(), "tx");
			
			path.mkdirs();
			txPath.mkdirs();
			
			EntityFileManagerConfigurer efm           = new EntityFileManagerImp();
			LockProvider lp                           = new LockProviderImp();
			EntityFileTransactionManagerConfigurer tm = new EntityFileTransactionManagerImp();
			//Somente pode ser usado depois 
			//que AsyncRecoveryTransactionLog suportar trucat e
			//AbstractVirutalEntityFileAccess.map não provocar out of memory
			//AsyncRecoveryTransactionLog rtl           = new AsyncRecoveryTransactionLog("recovery", txPath, tm);
			RecoveryTransactionLog rtl                = new RecoveryTransactionLogImp("recovery", txPath, tm);
			TransactionLog tl                         = new TransactionLogImp("binlog", txPath, tm);
			EntityFileTransactionFactory eftf         = new EntityFileTransactionFactoryImp();
			//EntityFileTransactionFactory eftf         = new AsyncEntityFileTransactionFactory(rtl);
			
			rtl.setForceReload(true);
			
			tm.setTransactionLog(tl);
			tm.setRecoveryTransactionLog(rtl);
			tm.setEntityFileTransactionFactory(eftf);
			tm.setLockProvider(lp);
			tm.setTimeout(EntityFileTransactionManagerImp.DEFAULT_TIMEOUT);
			tm.setTransactionPath(txPath);
			tm.setEntityFileManagerConfigurer(efm);
			tm.setEnabledTransactionLog(false);
			
			efm.setEntityFileTransactionManager(tm);
			efm.setLockProvider(lp);
			efm.setPath(path);
			
			efm.init();
			
			return efm;    	
    	}
    	catch(IllegalArgumentException e){
    		throw new IllegalArgumentException("fail create persistence manager", e);
    	}
    }
    
    @SuppressWarnings("unchecked")
    private FlushableReferenceCollection<Block> createDataBuffer(String name, 
    		EntityFileManagerConfigurer efm, CalypteConfig config){
    	try{
	    	HugeListInfo dataInfo = 
	    			HugeListCalculator
	    				.calculate(
	    						config.getDataBufferSize(),
	    						config.getDataPageSize(),
	    						config.getDataBlockSize(),
	    						config.getDataSwapFactor());
	    	
	    	Swapper<Block>[] swappers = new Swapper[dataInfo.getSubLists()];
	    	
	    	for(int i=0;i<dataInfo.getSubLists();i++) {
	    		EntityFileAccess<Block, ?, ?> efa =
	    				new SimpleEntityFileAccess<Block, byte[], BlockEntityFileHeader>(
	    		    			name + i + "_dta", 
	    		    			new File(efm.getPath(), name + i + "_dta"), 
	    		    			new BlockEntityFileDataHandler(this.memory, (int)config.getDataBlockSize()));
	    		
	    		efa.createNewFile();
		    	
		    	swappers[i] = new EntityFileSwapper<Block>(efa);
	    	}
	    	
	    	FlushableReferenceCollection<Block> dataList =
	                new FlushableReferenceCollectionImp<Block>(
	                dataInfo.getMaxCapacityElements(),
	                dataInfo.getClearFactorElements(),
	                dataInfo.getFragmentFactorElements(),
	                swappers,
	                config.getSwapperThread(),
	                dataInfo.getSubLists()
	                );
	        dataList.setDeleteOnExit(false);
	        
	        return dataList;
    	}
    	catch(Throwable e){
    		throw new IllegalArgumentException("fail create data buffer", e);
    	}
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private MapReferenceCollection<String, DataMap> createDataMap(
    		String name, EntityFileManagerConfigurer efm, CalypteConfig config){

    	try{
    		//nós da arvore de busca
    		HugeListInfo nodeInfo = 
	    			HugeListCalculator
	    				.calculate(
	    						config.getNodesBufferSize(),
	    						config.getNodesPageSize(),
	    						NODE_BINARY_SIZE, 
	    						config.getNodesSwapFactor());

    		Swapper[] nodesSwappers = new Swapper[nodeInfo.getSubLists()];
	    	
	    	for(int i=0;i<nodeInfo.getSubLists();i++) {
	    		
	    		EntityFileAccess<CharNode, ?, ?> efa =
	    				new SimpleEntityFileAccess<CharNode, byte[], CharNodeEntityFileHeader>(
	    		    			name + i + "_idx",
	    		    			new File(efm.getPath(), name + i + "_idx"), 
	    		    			new CharNodeEntityFileDataHandler());
	    		
	    		efa.createNewFile();
		    	nodesSwappers[i] = (Swapper<TreeNode<DataMap>>)new EntityFileSwapper(efa);
	    	}
    		
	    	//índice dos itens
    		HugeListInfo indexInfo = 
	    			HugeListCalculator
	    				.calculate(
	    						config.getIndexBufferSize(),
	    						config.getIndexPageSize(),
	    						INDEX_BINARY_SIZE, 
	    						config.getIndexSwapFactor());
    		

    		Swapper<DataMap>[] indexSwappers = new Swapper[indexInfo.getSubLists()];
	    	
	    	for(int i=0;i<indexInfo.getSubLists();i++) {
	    		EntityFileAccess<DataMap, ?, ?> efa =
	    				new SimpleEntityFileAccess<DataMap, byte[], DataMapEntityFileHeader>(
	    		    			name + i + "_idxv", 
	    		    			new File(efm.getPath(), name + i + "_idxv"), 
	    		    			new DataMapEntityFileDataHandler());	    		
		    	
	    		efa.createNewFile();
	    		
		    	indexSwappers[i] = new EntityFileSwapper<DataMap>(efa);
	    	}
	    	
    		MapReferenceCollection<String, DataMap> dataMap =
            		new BasicMapReferenceCollection<String, DataMap>(
                            nodeInfo.getMaxCapacityElements(),
                            nodeInfo.getClearFactorElements(),
                            nodeInfo.getFragmentFactorElements(),
                            nodesSwappers,
                            config.getSwapperThread(), 
                            nodeInfo.getSubLists(), 
                            indexInfo.getMaxCapacityElements(),
                            indexInfo.getClearFactorElements(),
                            indexInfo.getFragmentFactorElements(),
                            indexSwappers,
                            config.getSwapperThread(), 
                            indexInfo.getSubLists(), 
                            new StringTreeNodes<DataMap>()
    				);
            		
            		
	        dataMap.setDeleteOnExit(false);
	        return dataMap;
    	}
    	catch(Throwable e){
    		throw new IllegalArgumentException("fail data map", e);
    	}
    	
    }
    
    public void find(String key, ResultFind result) {
    	dataMap.find(new DataMapResultFind(key, result));
    }
    
    public boolean putStream(String key, InputStream inputData, 
    		long timeToLive, long timeToIdle) throws StorageException{
        
    	if(timeToLive < 0)
            throw new StorageException(CacheErrors.ERROR_1029);

    	if(timeToIdle < 0)
            throw new StorageException(CacheErrors.ERROR_1028);
    	
        if(key.length() > this.maxLengthKey)
            throw new StorageException(CacheErrors.ERROR_1008);
        
        DataMap oldMap = null;
        DataMap map    = new DataMap();
        
    	//ItemCacheInputStream permite manipular além dos dados os metadados do item.
        if(ITEM_CACHE_INPUTSTREAM_CLASS.isAssignableFrom(inputData.getClass())){
        	ItemCacheInputStream input = (ItemCacheInputStream)inputData;
        	DataMap itemMetadata = input.getMap();
        	
            map.setCreationTime(itemMetadata.getCreationTime());
            map.setMostRecentTime(itemMetadata.getMostRecentTime());
            map.setTimeToIdle(itemMetadata.getTimeToIdle());
            map.setTimeToLive(itemMetadata.getTimeToLive());
            
        	//o cache transacional pode tentar restaurar um item já expirado.
            //Nesse caso, tem que remove-lo. 
            //Somente será removido se o item ainda for o mesmo gerenciado pela transação.
            if(map.isDead()){
            	this.remove(key, map);
            	return false;
            }
            
        }
        else{
        	//Gera os metadados do item.
            map.setCreationTime(System.currentTimeMillis());
            map.setMostRecentTime(map.getCreationTime());
            map.setTimeToIdle(timeToIdle);
            map.setTimeToLive(timeToLive);
        }
        
        //Todo item inserido tem que ter uma nova id. Mesmo que ela exista.
        map.setId(modCount.incrementAndGet());

        try{
            //Registra os dados no buffer de dados.
            this.putData(map, inputData);
        }
        catch(Throwable e){
        	try{
        		this.releaseSegments(map);
        	}
        	catch(Throwable ex){
        		ex.printStackTrace();
        	}
        	
            throw 
        	e instanceof StorageException? 
        		(StorageException)e : 
        		new StorageException(e, CacheErrors.ERROR_1020);
        	
        }

        try{
            //Faz a indexação do item e retorna o índice atual, caso exista.
            oldMap = this.dataMap.put(key, map);
        }
        catch(Throwable e){
        	try{
    	    	this.releaseSegments(map);
            	this.dataMap.remove(key, map);
        	}
        	catch(Throwable ex){
        		ex.printStackTrace();
        	}
        	
        	throw new StorageException(e, CacheErrors.ERROR_1020);
        }
        finally{
	    	if(oldMap != null){
	    		this.releaseSegments(oldMap);
	            this.countRemoved.incrementAndGet();
	    	}
        }
        
        this.countWrite.incrementAndGet();
        return oldMap != null;
    }

    public boolean replaceStream(String key, InputStream inputData, 
    		long timeToLive, long timeToIdle) throws StorageException{
        
    	if(timeToLive < 0)
            throw new StorageException(CacheErrors.ERROR_1029);

    	if(timeToIdle < 0)
            throw new StorageException(CacheErrors.ERROR_1028);
    	
        if(key.length() > this.maxLengthKey)
            throw new StorageException(CacheErrors.ERROR_1008);
        
        DataMap oldMap = null;
        DataMap map    = new DataMap();
        
        map.setCreationTime(System.currentTimeMillis());
        map.setMostRecentTime(map.getCreationTime());
        map.setTimeToIdle(timeToIdle);
        map.setTimeToLive(timeToLive);
        
        //Toda item inserido tem que ter uma nova id. Mesmo que ele exista.
        map.setId(modCount.incrementAndGet());

        try{
            //Registra os dados no buffer de dados.
            this.putData(map, inputData);
        }
        catch(Throwable e){
        	try{
        		this.releaseSegments(map);
        	}
        	catch(Throwable ex){
        		ex.printStackTrace();
        	}
        	
            throw 
        	e instanceof StorageException? 
        		(StorageException)e : 
        		new StorageException(e, CacheErrors.ERROR_1020);
        	
        }

        try{
            //Faz a indexação do item e retorna o índice atual, caso exista.
            oldMap = this.dataMap.replace(key, map);
        }
        catch(Throwable e){
        	try{
    	    	this.releaseSegments(map);
            	this.dataMap.remove(key, map);
        	}
        	catch(Throwable ex){
        		ex.printStackTrace();
        	}
        	
        	throw new StorageException(e, CacheErrors.ERROR_1020);
        }
        finally{
	    	if(oldMap != null){
	    		this.releaseSegments(oldMap);
	    	}
        }
        
        if(oldMap != null){
            this.countWrite.incrementAndGet();
        	return true;
        }
        else
        	return false;
    }
    
    public InputStream putIfAbsentStream(String key, InputStream inputData, 
    		long timeToLive, long timeToIdle) throws StorageException{
        
    	if(timeToLive < 0)
            throw new StorageException(CacheErrors.ERROR_1029);

    	if(timeToIdle < 0)
            throw new StorageException(CacheErrors.ERROR_1028);
    	
        if(key.length() > this.maxLengthKey)
            throw new StorageException(CacheErrors.ERROR_1008);
        
        DataMap oldMap = null;
        DataMap map    = new DataMap();
        InputStream in = null;

        map.setCreationTime(System.currentTimeMillis());
        map.setMostRecentTime(map.getCreationTime());
        map.setTimeToIdle(timeToIdle);
        map.setTimeToLive(timeToLive);
        
        //Toda item inserido tem que ter uma nova id. Mesmo que ele exista.
        map.setId(modCount.incrementAndGet());

        try{
            //Registra os dados no buffer de dados.
            this.putData(map, inputData);
        }
        catch(Throwable e){
        	try{
        		this.releaseSegments(map);
        	}
        	catch(Throwable ex){
        		ex.printStackTrace();
        	}
        	
            throw 
        	e instanceof StorageException? 
        		(StorageException)e : 
        		new StorageException(e, CacheErrors.ERROR_1020);
        	
        }

        try{
            //Faz a indexação do item e retorna o índice atual, caso exista.
            oldMap = dataMap.putIfAbsent(key, map);
        }
        catch(Throwable e){
        	try{
    	    	this.releaseSegments(map);
            	this.dataMap.remove(key, map);
        	}
        	catch(Throwable ex){
        		ex.printStackTrace();
        	}
        	
        	throw new StorageException(e, CacheErrors.ERROR_1020);
        }
        
    	//se oldMap for diferente de null, significa que já existe um item no cache
        if(oldMap != null){
        	//remove os segmentos alocados para o item atual.
        	//se oldMap for diferente de null, map não foi registrado
        	//somente precisa liberar os segmentos alocados
    		this.releaseSegments(map);
    		
        	//tenta obter o stream do item no cache
        	in = this.getStream(key, oldMap);
        }
        else{
        	this.countWrite.incrementAndGet();
        }
        
        if(oldMap != null){
	    	if(in == null){
	    		//será lançada uma exceção se o item não existir
	    		throw new StorageException(CacheErrors.ERROR_1030);
	    	}
	    	else{
	    		//retorna o stream
	    		return in;
	    	}
        }
        else{
        	return null;
        }
        
    }
    
    public InputStream getStream(String key) throws RecoverException {
        DataMap map = dataMap.get(key);
    	return map == null || map.getCreationTime() < creationTime? null : getStream(key, map);
    }
    
	public boolean removeIfInvalid(String key) throws StorageException {
        try{
        	DataMap data = this.dataMap.get(key);

            if(data != null && (data.getCreationTime() < creationTime || data.isDead())){
            	remove(key, data);
            	return true;
            }
            else
                return false;
        }
        catch(Throwable e){
            throw new StorageException(e, CacheErrors.ERROR_1022);
        }
	}
    
    public boolean removeStream(String key) throws StorageException{
        
        try{
        	DataMap data = this.dataMap.get(key);

            if(data != null){
            	remove(key, data);
            	return true;
            }
            else
                return false;
        }
        catch(Throwable e){
            throw new StorageException(e, CacheErrors.ERROR_1022);
        }
        
    }
    
    public boolean containsKey(String key){
    	return dataMap.get(key) != null;
    }

    public DataMap getPointer(String key) throws RecoverException {
        return dataMap.get(key);
    }

    public void setPointer(String key, DataMap newDta) throws RecoverException {
    	dataMap.put(key, newDta);
    }
    
    public boolean replacePointer(String key, DataMap originalDta, DataMap newDta) throws RecoverException {
    	return dataMap.replace(key, originalDta, newDta);
    }
    
    public void remove(String key, DataMap data){
    	if(this.dataMap.remove(key, data)){
	    	this.releaseSegments(data);
	        countRemoved.incrementAndGet();
    	}
    }
    
    public void releaseSegments(DataMap map){
    	long segmentId = map.getFirstSegment();
    	
    	if(segmentId == -1)
    		return;
    	
        Block current = this.dataList.get(segmentId);
        
        int i=0;
        while(current != null){
			if(current.id == map.getId() && current.segment == i){
				this.dataList.remove(segmentId, current);
			}
            
			segmentId = current.nextBlock;
        	current = segmentId < 0? null : this.dataList.get(segmentId);
        	i++;
        }
    	
    	map.setFirstSegment(-1);
    }
    
    public InputStream getStream(String key, DataMap map) throws RecoverException {
        
        try{
            countRead.incrementAndGet();

        	//Verifica se o item já expirou
        	if(map.isDead()){
        		//Se expirou, remove do cache e retorna null.
        		remove(key, map);
        		return null;
        	}
        	
        	
        	//Se timeToIdle foi definido, é atualizado o horário do último acesso.
        	if(map.getTimeToIdle() > 0){
            	map.setMostRecentTime(System.currentTimeMillis());
            	//a instância no momento do replace porde não ser a mesma passada em oldElement.
            	dataMap.replace(key, map, map);
        	}
        	
        	int readData     = 0;
            Block[] segments = new Block[map.getSegments()];
            Block current    = map.getFirstSegment() < 0? null : dataList.get(map.getFirstSegment());
            int i            = 0;
            
            while(current != null){

                /*
                Se id for diferente da
                id do DataMap, significa que essa entrada foi ou está sendo
                removida.
                */
				if(current.id != map.getId() || current.segment != i)
				    throw new CorruptedDataException("invalid segment: " + current.id + ":" + map.getId() + " " + current.segment + ":" + i);
                
            	readData    += current.length;
                segments[i]  = current;
            	current      = current.nextBlock < 0? null : dataList.get(current.nextBlock);
            	i++;
            }
            
            if(readData <= 0) {
            	return null;
            }
            
            if(map.getLength() != readData) {
                throw new RecoverException(CacheErrors.ERROR_1021);
            }
            
            countReadData.addAndGet(readData);
            
            return new CacheInputStream(this, map, segments, segmentSize);
        }
        catch(CorruptedDataException e){
            return null;
        }
        catch(Throwable e){
        	e.printStackTrace();
            throw new RecoverException(e, CacheErrors.ERROR_1021);
        }
    }
    
    public void putData(DataMap map, InputStream inputData) throws StorageException, InterruptedException{
        
        int writeData    = 0;
        byte[] buffer    = new byte[this.segmentSize];
        int index        = 0;
        Block lastBlock  = null;
        long lastSegment = -1;
        int read;
        
        try{
            while((read = inputData.read(buffer, 0, this.segmentSize)) != -1){
            	writeData += read;
            	
        		RegionMemory data = this.memory.alloc(this.segmentSize);
        		data.write(0, buffer, 0, read);
        		
            	Block block = new Block(map.getId(), index++, data, read);
                Long segment = dataList.insert(block);

                if(lastBlock != null){
                	lastBlock.nextBlock = segment;
                	dataList.set(lastSegment, lastBlock);
                }
                else
                	map.setFirstSegment(segment);
                
            	lastBlock   = block;
                lastSegment = segment;
            }

            this.countWriteData.addAndGet(writeData);
            
    		if(writeData > this.maxBytesToStorageEntry){
                throw new StorageException(CacheErrors.ERROR_1007);
    		}
            
            map.setLength(writeData);
            map.setSegments(index);
        }
        catch(StorageException e){
            this.countRemovedData.addAndGet(writeData);
            this.releaseSegments(map);
            throw e;
        }
        catch(IOException e){
            this.countRemovedData.addAndGet(writeData);
            this.releaseSegments(map);
            throw new StorageException(e, CacheErrors.ERROR_1014);
        }
    }

    public long getNextModCount(){
    	return modCount.incrementAndGet();
    }
    
	public CalypteConfig getConfig() {
		return config;
	}
    
    public long getCountRead(){
        return this.countRead.longValue();
    }

    public long getCountWrite(){
        return this.countWrite.longValue();
    }

    public long getCountRemoved() {
		return countRemoved.longValue();
	}

    public long getCountReadData() {
        return countReadData.longValue();
    }
    
    public long getCountWriteData() {
        return countWriteData.longValue();
    }

    public long getCountRemovedData() {
        return countRemovedData.longValue();
    }
    
    public boolean isDeleteOnExit() {
		return deleteOnExit;
	}

	public void setDeleteOnExit(boolean deleteOnExit) {
		this.deleteOnExit = deleteOnExit;
	}

	public long size() {
		return countRemoved.longValue() - countWrite.longValue();
	}

	public int getMaxKeySize() {
		return maxLengthKey;
	}
	
	public boolean isEmpty() {
		return (countRemoved.longValue() - countWrite.longValue()) == 0;
	}
	
    public long getCreationTime() {
    	return creationTime;
    }
	
	public void clear(){
		/*
		this.countRead 			= 0;
		this.countReadData 		= 0;
		this.countRemoved 		= 0;
		this.countRemovedData 	= 0;
		this.countWrite 		= 0;
		this.countWriteData 	= 0;
		*/
		this.creationTime       = System.currentTimeMillis();
		//this.dataList.clear();
		//this.dataMap.clear();
	}
	
	public void destroy(){
		if(enabled){
			dataList.destroy();
			dataMap.destroy();
			entityFileManager.destroy();
			enabled = false;
		}
	}
	
	public boolean isDestroyed() {
		return !enabled;
	}
	
    protected void finalize() throws Throwable{
    	try{
    		if(deleteOnExit){
    			destroy();
    		}
    	}
    	finally{
    		super.finalize();
    	}
    }
	
    public class DataMapResultFind 
    	implements ResultFind, Find<DataMap>{

    	private ResultFind original;
    	
    	public DataMapResultFind(String key, ResultFind original) {
    		this.original   = original;
    		this.chars      = key == null? null : key.toCharArray();
    		this.index      = 0;
    		this.currentKey = new StringBuilder();
    	}
    	
		public void found(String key, CacheHandler cache) {
			original.found(key, cache);
		}

		/* map */
		
		private char[] chars;
		
		private int index;
		
		private StringBuilder currentKey;
		
		public void found(DataMap value) {
			found(currentKey.toString(), BasicCacheHandler.this);
		}

		public boolean accept() {
			return chars == null? true : index == chars.length;
		}

		public boolean acceptNodeKey(Object key) {
			return chars == null? true : key.equals(chars[index]);
		}
		
		public void beforeNextNode(Object key, TreeNode<DataMap> node) {
			currentKey.append(key);
			index++;
		}

		public void afterNextNode(Object key, TreeNode<DataMap> node) {
			currentKey.setLength(currentKey.length() - 1);
			index--;
		}
    	
    }

}
