package metaDataServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import utilities.UsefulMethods;
import java.util.concurrent.locks.ReentrantLock;

public class MetadataHandler implements Runnable {
	
	private final ReentrantLock lock = new ReentrantLock();
	UsefulMethods usefulmethods = UsefulMethods.getUsefulMethodsInstance();
	MetadataStorage storage = MetadataStorage.getMetadataStorageInstance();
	PrintWriter writer;
	BufferedReader reader;
	Socket sock;
	public static boolean heartbeatReceived = false;
	public static volatile HashMap<Integer, Long> lastMsgSentTime = new HashMap<>(); 
	
	public MetadataHandler(Socket client) {
		sock = client;
		try {
			InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
			reader = new BufferedReader(isReader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		String msg;
		try {
			while((msg = reader.readLine()) != null) {
				String[] parts = msg.split(":");
				String action = parts[0];
				
				if(action.equalsIgnoreCase("create")) {
					System.out.println("metadataHandler create operation");
					String filename = parts[1];
					int serverNumber = usefulmethods.randomServer();
					if(storage.fileExists(filename)) {
						System.out.println("file exits appending the message at the end");
					}
					else {
						String[] chunks = filename.split("\\.");
						filename = chunks[0];
						storage.buildArraylist(filename);
						storage.createHashMap(filename, serverNumber);
					}
					sendWelcomeMessage(sock, serverNumber);
				}
				else if(action.equalsIgnoreCase("append")) {
					System.out.println("metadataHandler append operation");
					String lastChunkInfo = storage.getLastChunkInfo(parts[1]);
					String chunkName = lastChunkInfo.split(":")[0];
					int serverNumber = Integer.parseInt(lastChunkInfo.split(":")[1]);
					if(heartbeatReceived && lastMsgSentTime.get(serverNumber) != null) {
						System.out.println("failure detection in progrees....");
						if(checkForAvailabilityofServer(serverNumber)) {
							sendAppendWelcomeMessage(sock, lastChunkInfo);
						} 
						else {
							serverNumber = -1;
							String sendErrorlastChunkInfo = chunkName+":"+serverNumber;
							sendAppendWelcomeMessage(sock, sendErrorlastChunkInfo);
							System.out.println("Server not available");
						}
					} 
					else {
						System.out.println("No heartbeat msg yet so no failure detection");
						sendAppendWelcomeMessage(sock, lastChunkInfo);
					}
				}
				else if(action.equalsIgnoreCase("read")) {
					System.out.println("metadataHandler read operation" + heartbeatReceived);
					String fileName = parts[1].split("-")[0];
					String chunkName = parts[1];
					int serverNumber = storage.readHashMap(fileName, chunkName);
					if(heartbeatReceived && lastMsgSentTime.get(serverNumber) != null) {
						System.out.println("failure detection in progrees....");
						if(checkForAvailabilityofServer(serverNumber)) {
							sendWelcomeMessage(sock, serverNumber);
						} 
						else {
							serverNumber = -1;
							sendWelcomeMessage(sock, serverNumber);
							System.out.println("Server not available");
						}
					} 
					else {
						System.out.println("No heartbeat msg yet so no failure detection");
						sendWelcomeMessage(sock, serverNumber);
					}
				}
				else if(action.equalsIgnoreCase("heartbeat")) {
					System.out.println("metadataHandler heartBeat operation"+ UsefulMethods.getUsefulMethodsInstance().getTime()+ " Server Number : "+ parts[1]);
					String serverNumber = parts[1];					
					String fileLength = parts[3];
					int byteSize = Integer.parseInt(fileLength);
					String lastModified = parts[4];
					
					String chunkName = parts[2];
					String[] names = chunkName.split("-");
					String fileName = names[0];
					
					if(storage.fileExists(fileName)) {
						storage.updateHashMap(Integer.parseInt(serverNumber), fileName, chunkName, byteSize, lastModified);
					}

					heartbeatReceived = true;
					updateLastMsgSentTime(Integer.parseInt(serverNumber));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void updateLastMsgSentTime(int serverNumber) {
		lock.lock();
		try {
			lastMsgSentTime.put(serverNumber, System.currentTimeMillis());
		} finally {
			lock.unlock();
		}
		System.out.println("lastMsgSentTime: "+lastMsgSentTime);
	}
	
	private boolean checkForAvailabilityofServer(int serverNumber) {
		System.out.println("lastMsgSentTime : " + lastMsgSentTime);
		Long presentTime = System.currentTimeMillis();
		Long serverTime = 0L;
		lock.lock();
		try{
			serverTime = lastMsgSentTime.get(serverNumber);
		}finally {
			lock.unlock();
		}
		Long difference = presentTime - serverTime;
		if(difference > 15000) {
			return false;
		}
		return true;
	}
	
	private void sendWelcomeMessage(Socket client, int serverNumber) throws IOException {
        try {
        	writer = new PrintWriter(client.getOutputStream(), true);
            writer.println("serverNumber:"+serverNumber);
            writer.flush();
        } finally {
            //writer.close();
        }
    }
	
	private void sendAppendWelcomeMessage(Socket client, String lastChunkInfo) throws IOException {
		try {
			writer = new PrintWriter(client.getOutputStream(), true);
            writer.println(lastChunkInfo);
            writer.flush();
		} finally {
            //writer.close();
        }
	}
	
	public void closeEverything() {
		try{
			sock.close();
			writer.close();
			reader.close();
		} catch(Exception w) {
			w.printStackTrace();
		}
	}
}
