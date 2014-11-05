package metaDataServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import utilities.UsefulMethods;

public class MetadataStorage {
	UsefulMethods usefulmethods = UsefulMethods.getUsefulMethodsInstance();
	private final ReentrantLock lock = new ReentrantLock();
	private static volatile MetadataStorage storageInstance = null;
	private volatile ArrayList<String> metadataFilenames = new ArrayList<String>();
	private volatile HashMap<String, String> metadataHashMap = new HashMap<>(); 
	
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
		String chunkName = filename+"1";
		String compose = filename+","+chunkName+","+serverNumber;
		metadataHashMap.put(compose, usefulmethods.getTime());
		System.out.println("Hashmap elements : "+metadataHashMap);
	}
}
