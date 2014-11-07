package client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;

import utilities.UsefulMethods;

public class Client {
	
	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader("resource/instruction.txt"));
		Client client = new Client();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String parts[] = line.split("\\|");
				if(parts[0].toUpperCase().equals(("r").toUpperCase())) {
					client.readFromFile(parts[1], parts[2], Integer.parseInt(parts[3]));
				}
				else if(parts[0].toUpperCase().equals(("w").toUpperCase())) {
					String filename = parts[1];
					client.createFile(filename, parts[2]);
				}
				else if(parts[0].toUpperCase().equals(("a").toUpperCase())) {
					String filename = parts[1];
					client.appendToFile(filename);
				}
			}
		} finally {
			reader.close();
		}
	}

	private void createFile(String filename, String message) throws IOException {
		// consult m-server and create a file
		System.out.println("create request filename: "+filename);
		int serverNumber = SetMetadataServer("create",filename);
		SetUpNetworking(serverNumber,filename, message);
	}

	private void appendToFile(String filename) {
		// still have to consult m-server for append to the end of the file
		//presently just using the create
	}

	private void readFromFile(String filename, String offset, int bytesToRead) throws IOException {
		//consult meta data server and read
		System.out.println("read request filename : "+filename+" offset : "+offset+ " bytesToRead : "+ bytesToRead);
		String[] chunks = filename.split("\\.");
		int chunkNumber = (Integer.parseInt(offset)/8192)+1;
		String chunkName = chunks[0]+"-"+chunkNumber;
		int seekPosition = Integer.parseInt(offset) % 8192;
		int serverNumber = SetMetadataServer("read", chunkName);
		
		// IF the read extends in more than one file
		if((seekPosition+(bytesToRead)) > 8192) {
			int otherBytesToRead = seekPosition+(bytesToRead) - 8192;
			bytesToRead = 8192 - seekPosition;
			int otherChunkNumber = chunkNumber+1;
			String otherChunkName = chunks[0]+otherChunkNumber;
			int otherServerNumber = SetMetadataServer("read", otherChunkName);
			System.out.println("Main Chunk : "+serverNumber +" Other chunks : "+otherServerNumber);
			SetUpReadNetworking(otherServerNumber, otherChunkName, 0, otherBytesToRead);
		}
		SetUpReadNetworking(serverNumber, chunkName, seekPosition, bytesToRead);
	}
	
	private void SetUpReadNetworking(int serverNumber, String filename, int seekPosition, int bytesToRead) {
		Properties ServerPort = UsefulMethods.getUsefulMethodsInstance().getPropertiesFile("spec.properties");
		
		String serverName = ServerPort.getProperty("server"+serverNumber);
		String portString = ServerPort.getProperty("server"+serverNumber+"port");
		int port = Integer.parseInt(portString.trim());
		System.out.println("Connecting to "+serverName+".... with port ......"+port);
		
		Socket client = null;
		
		try {
			client = new Socket(serverName, port);
			PrintWriter out1 = new PrintWriter(client.getOutputStream(), true);
			out1.println("read:"+filename+":"+serverNumber+":"+seekPosition+":"+bytesToRead);
			client.close();out1.close();			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void SetUpNetworking(int serverNumber,String filename, String message) {
		
		Properties ServerPort = UsefulMethods.getUsefulMethodsInstance().getPropertiesFile("spec.properties");
		
		String serverName = ServerPort.getProperty("server"+serverNumber);
		String portString = ServerPort.getProperty("server"+serverNumber+"port");
		int port = Integer.parseInt(portString.trim());
		System.out.println("Connecting to "+serverName+".... with port ......"+port);
		
		Socket client = null;
		
		try {
			client = new Socket(serverName, port);
			PrintWriter out1 = new PrintWriter(client.getOutputStream(), true);
			out1.println("write:"+filename+":"+serverNumber+":"+message);
			client.close();out1.close();			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int SetMetadataServer(String action, String filename) throws IOException {
		
		int serverNumber = 0;
		Properties ServerPort = UsefulMethods.getUsefulMethodsInstance().getPropertiesFile("spec.properties");
		
		String serverName = ServerPort.getProperty("metadataserver");
		String portString = ServerPort.getProperty("metadataport");//Integer.parseInt(args[1]);
		int port = Integer.parseInt(portString.trim());
		System.out.println("Connecting to "+serverName+".... with port ......"+port);
		
		Socket client = null;
		
		try {
			client = new Socket(serverName, port);
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			BufferedReader in =new BufferedReader(new InputStreamReader(client.getInputStream()));
			if(action.equalsIgnoreCase("create")) {
				out.println("create"+":"+filename);
			}
			else if(action.equalsIgnoreCase("append")) {
				
			}
			else if(action.equalsIgnoreCase("read")) {
				out.println(action+":"+filename);
			}
			
			serverNumber = readResponse(client, in);
			client.close();out.close();in.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			//client.close();
		}
		return serverNumber;
	}
	
	public int readResponse(Socket client, BufferedReader in) throws IOException {
		String userInput;
		/*BufferedReader stdIn = new BufferedReader(new InputStreamReader(
				client.getInputStream()));*/

		while ((userInput = in.readLine()) != null) {
			System.out.println("Response from server:"+userInput);
			String parts[] = userInput.split(":");
			return Integer.parseInt(parts[1]);
		}
		return 0;
	}
}
