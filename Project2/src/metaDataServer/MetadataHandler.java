package metaDataServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import utilities.UsefulMethods;

public class MetadataHandler implements Runnable {
	
	UsefulMethods usefulmethods = UsefulMethods.getUsefulMethodsInstance();
	MetadataStorage storage = MetadataStorage.getMetadataStorageInstance();
	PrintWriter writer;
	BufferedReader reader;
	Socket sock;
	
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
					sendWelcomeMessage(sock, serverNumber);
				}
				else if(action.equalsIgnoreCase("heartbeat")) {
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
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
