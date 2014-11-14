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
	static int readRetry = 0;
	static int appendRetry = 0;
	static int createRetry = 0;
	
	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader("resource/instruction.txt"));
		Client client = new Client();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				readRetry = 0; appendRetry = 0; createRetry = 0;
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
					client.appendToFile(filename, parts[2]);
				}
			}
		} finally {
			reader.close();
		}
	}

	private void createFile(String filename, String message) throws IOException {
		// consult m-server and create a file
		System.out.println("create request filename: "+filename);
		String returnedString = (SetMetadataServer("create",filename));
		int serverNumber = Integer.parseInt(returnedString.split(":")[1]);
		SetUpNetworking(serverNumber,filename, message);
	}

	private void appendToFile(String filename, String message) {
		System.out.println("append request filename : "+filename);
		String lastChunkInfo = null;
		filename = filename.split("\\.")[0];
		try {
			lastChunkInfo = SetMetadataServer("append", filename);
			String[] lastInfos = lastChunkInfo.split(":");
			String chunkName = lastInfos[0];
			int ServerNumber = Integer.parseInt(lastInfos[1]);
			if((ServerNumber == 0 || ServerNumber == -1) && appendRetry < 2) {
				appendRetry++;
				try {
					Thread.sleep(2000);
				} catch(Exception e) {
					e.printStackTrace();
				}
				lastChunkInfo = SetMetadataServer("append", filename);
				lastInfos = lastChunkInfo.split(":");
				chunkName = lastInfos[0];
				ServerNumber = Integer.parseInt(lastInfos[1]);
				if((ServerNumber == 0 || ServerNumber == -1) && appendRetry < 2) {
					appendToFile(filename, message);
				}
			}
			try {
				Thread.sleep(2000);
			} catch(Exception e) {
				e.printStackTrace();
			}
			if(appendRetry < 2) {
				SetUpAppendNetworking(chunkName, ServerNumber, message);
			} else {
				System.out.println("Server Unavailable, tried reaching "+appendRetry+ " times");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readFromFile(String filename, String offset, int bytesToRead) throws IOException {
		//consult meta data server and read
		System.out.println("read request filename : "+filename+" offset : "+offset+ " bytesToRead : "+ bytesToRead);
		String[] chunks = filename.split("\\.");
		int chunkNumber = (Integer.parseInt(offset)/8192)+1;
		String chunkName = chunks[0]+"-"+chunkNumber;
		int seekPosition = Integer.parseInt(offset) % 8192;
		String returnedString = (SetMetadataServer("read", chunkName));
		String[] parts = returnedString.split(":");
		int serverNumber = Integer.parseInt(parts[1]);
		
		if((serverNumber == 0 || serverNumber == -1) && readRetry < 2) {
			readRetry++;
			System.out.println(readRetry+" Trying to reach server......");
			try {
				Thread.sleep(2000);
			} catch(Exception e) {
				e.printStackTrace();
			}
			returnedString = (SetMetadataServer("read", chunkName));
			serverNumber = Integer.parseInt(returnedString.split(":")[1]);
			if((serverNumber == 0 || serverNumber == -1) && readRetry < 2) {
				readFromFile(filename, offset, bytesToRead);
			}
		}
		
		// IF the read extends in more than one file
		if((seekPosition+(bytesToRead)) > 8192) {
			int otherBytesToRead = seekPosition+(bytesToRead) - 8192;
			bytesToRead = 8192 - seekPosition;
			int otherChunkNumber = chunkNumber+1;
			String otherChunkName = chunks[0]+otherChunkNumber;
			int otherServerNumber = 0;
			String otherReturnedString = (SetMetadataServer("read", chunkName));
			String[] otherParts = otherReturnedString.split(":");
			if(otherParts.length > 2) {
				System.out.println("Server Unavailable");
			} 
			else {
				otherServerNumber = Integer.parseInt(otherParts[1]);
			}
			System.out.println("Main Chunk : "+serverNumber +" Other chunks : "+otherServerNumber);
			SetUpReadNetworking(otherServerNumber, otherChunkName, 0, otherBytesToRead);
		}
		try {
			Thread.sleep(15000);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(readRetry < 2) {
			SetUpReadNetworking(serverNumber, chunkName, seekPosition, bytesToRead);
		} else {
			System.out.println("Server Unavailable, tried reaching "+readRetry+ " times");
		}
	}


	private void SetUpAppendNetworking(String chunkName, int serverNumber, String message) {
		Properties ServerPort = UsefulMethods.getUsefulMethodsInstance().getPropertiesFile("spec.properties");
		
		String serverName = ServerPort.getProperty("server"+serverNumber);
		String portString = ServerPort.getProperty("server"+serverNumber+"port");
		int port = Integer.parseInt(portString.trim());
		/*System.out.println("Connecting to "+serverName+".... with port ......"+port);*/
		
		Socket client = null;
		
		try {
			client = new Socket(serverName, port);
			PrintWriter out1 = new PrintWriter(client.getOutputStream(), true);
			out1.println("append:"+chunkName+":"+serverNumber+":"+message);
			client.close();out1.close();			
		}
		catch (IOException e) {
			System.out.println("Server Unavailable");
		}
	}
	
	private void SetUpReadNetworking(int serverNumber, String filename, int seekPosition, int bytesToRead) {
		Properties ServerPort = UsefulMethods.getUsefulMethodsInstance().getPropertiesFile("spec.properties");
		
		String serverName = ServerPort.getProperty("server"+serverNumber);
		String portString = ServerPort.getProperty("server"+serverNumber+"port");
		int port = Integer.parseInt(portString.trim());
		/*System.out.println("Connecting to "+serverName+".... with port ......"+port);*/
		
		Socket client = null;
		
		try {
			client = new Socket(serverName, port);
			PrintWriter out1 = new PrintWriter(client.getOutputStream(), true);
			out1.println("read:"+filename+":"+serverNumber+":"+seekPosition+":"+bytesToRead);
			client.close();out1.close();			
		}
		catch (IOException e) {
			System.out.println("Server Unavailable");
		}
	}

	private void SetUpNetworking(int serverNumber,String filename, String message) {
		
		Properties ServerPort = UsefulMethods.getUsefulMethodsInstance().getPropertiesFile("spec.properties");
		
		String serverName = ServerPort.getProperty("server"+serverNumber);
		String portString = ServerPort.getProperty("server"+serverNumber+"port");
		int port = Integer.parseInt(portString.trim());
		/*System.out.println("Connecting to "+serverName+".... with port ......"+port);*/
		
		Socket client = null;
		
		try {
			client = new Socket(serverName, port);
			PrintWriter out1 = new PrintWriter(client.getOutputStream(), true);
			out1.println("write:"+filename+":"+serverNumber+":"+message);
			client.close();out1.close();			
		}
		catch (IOException e) {
			try{
				Thread.sleep(2000);
				if(createRetry < 2) {
					System.out.println("Server was not up so trying "+createRetry+" again to create a file");
					createRetry++;
					SetUpNetworking(serverNumber, filename, message);
				}
			} catch(InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}
	
	private String SetMetadataServer(String action, String filename) throws IOException {
		
		String serverNumber = null;
		Properties ServerPort = UsefulMethods.getUsefulMethodsInstance().getPropertiesFile("spec.properties");
		
		String serverName = ServerPort.getProperty("metadataserver");
		String portString = ServerPort.getProperty("metadataport");//Integer.parseInt(args[1]);
		int port = Integer.parseInt(portString.trim());
		//System.out.println("Connecting to "+serverName+".... with port ......"+port);
		
		Socket client = null;
		
		try {
			client = new Socket(serverName, port);
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			BufferedReader in =new BufferedReader(new InputStreamReader(client.getInputStream()));
			if(action.equalsIgnoreCase("create")) {
				out.println("create"+":"+filename);
				serverNumber = readResponse(client, in);
				return serverNumber;
			}
			else if(action.equalsIgnoreCase("append")) {
				out.println(action+":"+filename);
				serverNumber = readAppendResponse(client, in);
				return serverNumber;
			}
			else if(action.equalsIgnoreCase("read")) {
				out.println(action+":"+filename);
				serverNumber = readResponse(client, in);
				return serverNumber;
			}
			client.close();out.close();in.close();
		}
		catch (IOException e) {
			System.out.println("MetaDataServer Unavailable");
		} 
		finally {
			//client.close();
		}
		return null;
	}
	
	private String readAppendResponse(Socket client, BufferedReader in) throws IOException {
		String userInput;

		while ((userInput = in.readLine()) != null) {
			System.out.println("Response from Append server:"+userInput+ " Time of Response : "+UsefulMethods.getUsefulMethodsInstance().getTime());
			return userInput;
		}
		return null;
	}

	public String readResponse(Socket client, BufferedReader in) throws IOException {
		String userInput;
		/*BufferedReader stdIn = new BufferedReader(new InputStreamReader(
				client.getInputStream()));*/

		while ((userInput = in.readLine()) != null) {
			System.out.println("Response from server:"+userInput+ " Time of Response : "+UsefulMethods.getUsefulMethodsInstance().getTime());
			//return Integer.parseInt(parts[1]);
			return userInput;
		}
		return null;
	}
}
