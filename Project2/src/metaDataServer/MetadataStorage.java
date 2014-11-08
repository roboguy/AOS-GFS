package metaDataServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import utilities.UsefulMethods;

public class MetadataStorage {
	UsefulMethods usefulmethods = UsefulMethods.getUsefulMethodsInstance();
	private final ReentrantLock lock = new ReentrantLock();
	private static volatile MetadataStorage storageInstance = null;
	private volatile ArrayList<String> metadataFilenames = new ArrayList<String>();
	private volatile HashMap<String, HashMap<String, ArrayList<Object>>> hashMap = new HashMap<>();
	
	private MetadataStorage() {
		
	}

	public static MetadataStorage getMetadataStorageInstance() {
		synchronized (MetadataStorage.class) {
			// Double check
			if (storageInstance == null) {
				System.out.println("MetadataStorage : I am being created");
				storageInstance = new MetadataStorage();
			}
		}
		return storageInstance;
	}

	public boolean fileExists(String file) {
		for(int i=0; i < metadataFilenames.size(); i++) {
			if(file.equalsIgnoreCase(metadataFilenames.get(i))) {
				return true;
			}
		}
		return false; 
	}

	public void buildArraylist(String file) {
		lock.lock();  // block until condition holds
	     try {
	 		metadataFilenames.add(file);
	     } finally {
	       lock.unlock();
	     }
	}
	
	
	public void createHashMap(String filename, int serverNumber) {

		lock.lock();
		try {
			String[] chunk = filename.split("\\.");
			String chunkName = chunk[0] + "-1";
			int bytesize = 0;

			HashMap<String, ArrayList<Object>> internalHashMap = new HashMap<>();
			ArrayList<Object> internlArrayList = new ArrayList<Object>();
			internlArrayList.add(serverNumber);
			internlArrayList.add(bytesize);
			internlArrayList.add(usefulmethods.getTime());

			internalHashMap.put(chunkName, internlArrayList);
			hashMap.put(chunk[0], internalHashMap);

		} finally {
			lock.unlock();
		}
	}
	// I have not implemented add here
	public void updateHashMap(int serverNumber, String fileName, String chunkName, int byteSize, String lastModified) {
		Iterator<Entry<String, HashMap<String, ArrayList<Object>>>> it = hashMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, HashMap<String, ArrayList<Object>>> pairs = (Entry<String, HashMap<String, ArrayList<Object>>>)it.next();
	        HashMap<String, ArrayList<Object>> internalHashMap = new HashMap<String, ArrayList<Object>>();
	        
	        if(fileName.equalsIgnoreCase((pairs.getKey()).toString())) {
	        	internalHashMap = (HashMap<String, ArrayList<Object>>) pairs.getValue();
	        	Iterator<Entry<String, ArrayList<Object>>> internal = internalHashMap.entrySet().iterator();
		        while(internal.hasNext()) {
		        	Map.Entry<String, ArrayList<Object>> chunk = (Map.Entry<String, ArrayList<Object>>)internal.next();
		        	
		        	if(chunkName.equalsIgnoreCase((chunk.getKey()).toString())) {
		        	
			        	ArrayList<Object> internalArrayList = new ArrayList<Object>();
			        	internalArrayList = (ArrayList<Object>) chunk.getValue();
			        	for(int i=0; i< internalArrayList.size(); i++) {
							lock.lock();
							try {
								internalArrayList.set(1, byteSize);
								internalArrayList.set(2, lastModified);
							} finally {
								lock.unlock();
							}
			        	}
		        	} 
		        	else {
		        		ArrayList<Object> newArraylsit = new ArrayList<Object>();
		        		newArraylsit.add(serverNumber);
		        		newArraylsit.add(byteSize);
		        		newArraylsit.add(lastModified);
		        		internalHashMap.put(chunkName, newArraylsit);
		        		hashMap.put(fileName, internalHashMap);
		        		break;
		        	}
		        }
	        }
	    }
	    System.out.println("HeartBeat Update Server:"+serverNumber+ " file:"+fileName+ ""
				+ " chunkName:"+chunkName+ " byteSize:"+byteSize+ " lastModified:"+ usefulmethods.getTime());
	}
	
	public int readHashMap(String fileName, String chunkName) {
		int returnServerNumber = 0;
		Iterator<Entry<String, HashMap<String, ArrayList<Object>>>> it = hashMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, HashMap<String, ArrayList<Object>>> pairs = (Entry<String, HashMap<String, ArrayList<Object>>>)it.next();
	        HashMap<String, ArrayList<Object>> internalHashMap = new HashMap<String, ArrayList<Object>>();
	        
	        if(fileName.equalsIgnoreCase((pairs.getKey()).toString())) {
	        	internalHashMap = (HashMap<String, ArrayList<Object>>) pairs.getValue();
	        	Iterator<Entry<String, ArrayList<Object>>> internal = internalHashMap.entrySet().iterator();
		        while(internal.hasNext()) {
		        	Map.Entry<String, ArrayList<Object>> chunk = (Map.Entry<String, ArrayList<Object>>)internal.next();
		        	
		        	if(chunkName.equalsIgnoreCase((chunk.getKey()).toString())) {
		        	
			        	ArrayList<Object> internalArrayList = new ArrayList<Object>();
			        	internalArrayList = (ArrayList<Object>) chunk.getValue();
						lock.lock();
						try {
							returnServerNumber = (int) internalArrayList.get(0);
						} finally {
							lock.unlock();
						}
		        	}
		        }
	        }
	    }
		return returnServerNumber;
	}

	public String getLastChunkInfo(String fileName) {
		int NoOfChunks = 0; 
		String chunkName = null;
		StringBuilder sb = new StringBuilder();
		
		Iterator<Entry<String, HashMap<String, ArrayList<Object>>>> it = hashMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, HashMap<String, ArrayList<Object>>> pairs = (Entry<String, HashMap<String, ArrayList<Object>>>)it.next();
	        HashMap<String, ArrayList<Object>> internalHashMap = new HashMap<String, ArrayList<Object>>();
	        
	        if(fileName.equalsIgnoreCase((pairs.getKey()).toString())) {
	        	internalHashMap = (HashMap<String, ArrayList<Object>>) pairs.getValue();
	        	
	        	NoOfChunks = internalHashMap.size();
	        	chunkName = fileName+"-"+NoOfChunks;
	        	
	        	Iterator<Entry<String, ArrayList<Object>>> internal = internalHashMap.entrySet().iterator();
	        	while(internal.hasNext()) {
		        	Map.Entry<String, ArrayList<Object>> chunk = (Map.Entry<String, ArrayList<Object>>)internal.next();
		        	
		        	if(chunkName.equalsIgnoreCase((chunk.getKey()).toString())) {
		        	
			        	ArrayList<Object> internalArrayList = new ArrayList<Object>();
			        	internalArrayList = (ArrayList<Object>) chunk.getValue();
						lock.lock();
						try {
							 sb.append(chunkName);
							 sb.append(":");
							 sb.append(internalArrayList.get(0));
						} finally {
							lock.unlock();
						}
		        	}
		        }
	        }
	    }
	    return sb.toString();
	}
}
