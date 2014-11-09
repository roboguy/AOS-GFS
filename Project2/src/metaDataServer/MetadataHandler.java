package metaDataServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import utilities.UsefulMethods;

public class MetadataHandler implements Runnable {
	
	UsefulMethods usefulmethods = UsefulMethods.getUsefulMethodsInstance();
	MetadataStorage storage = MetadataStorage.getMetadataStorageInstance();
	PrintWriter writer;
	BufferedReader reader;
	Socket sock;
	boolean heartbeatReceived = false;
	HashMap<Integer, Long> lastMsgSentTime = new HashMap<>(); 
	
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
					String filename = parts[1];
					int serverNumber = usefulmethods.randomServer();
					if(storage.fileExists(filename)) {
						//Send error message saying file exits
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
					String lastChunkInfo = storage.getLastChunkInfo(parts[1]);
					try {
						writer = new PrintWriter(sock.getOutputStream(), true);
			            writer.println(lastChunkInfo);
			            writer.flush();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				else if(action.equalsIgnoreCase("read")) {
					System.out.println("metadataHandler read operation");
					String fileName = parts[1].split("-")[0];
					System.out.println("metadataHandler filename:" +fileName);
					String chunkName = parts[1];
					System.out.println("metadataHandler chunkName:" +chunkName);
					int serverNumber = storage.readHashMap(fileName, chunkName);
					if(heartbeatReceived) {
						System.out.println("failure detection in progree....");
						if(checkForAvailabilityofServer(serverNumber)) {
							sendWelcomeMessage(sock, serverNumber);
						} 
						else {
							String info = serverNumber+":"+"ServerUnavailable";
							sendRequiredInfo(sock, info);
							System.out.println("Server not available");
						}
					} 
					else {
						System.out.println("No heartbeat msg yet so no failure detection");
						sendWelcomeMessage(sock, serverNumber);
					}
				}
				else if(action.equalsIgnoreCase("heartbeat")) {
					heartbeatReceived = true;
					String serverNumber = parts[1];					
					String fileLength = parts[3];
					int byteSize = Integer.parseInt(fileLength);
					String lastModified = parts[4];
					
					String chunkName = parts[2];
					String[] names = chunkName.split("-");
					String fileName = names[0];
					
					updateLastMsgSentTime(Integer.parseInt(serverNumber));
					
					if(storage.fileExists(fileName)) {
						storage.updateHashMap(Integer.parseInt(serverNumber), fileName, chunkName, byteSize, lastModified);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void updateLastMsgSentTime(int serverNumber) {
		if(lastMsgSentTime.get(serverNumber) != null) {
			lastMsgSentTime.put(serverNumber, System.currentTimeMillis());
		} 
		else {
			lastMsgSentTime.put(serverNumber, System.currentTimeMillis());
		}
	}
	
	private boolean checkForAvailabilityofServer(int serverNumber) {
		Long presentTime = System.currentTimeMillis();
		Long serverTime = lastMsgSentTime.get(serverNumber);
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
	
	private void sendRequiredInfo(Socket client, String info) throws IOException {
        try {
        	writer = new PrintWriter(client.getOutputStream(), true);
            writer.println("serverNumber:"+info);
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
