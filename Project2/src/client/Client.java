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
					client.readFromFile(parts[1]);
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
		int serverNumber = SetMetadataServer("create",filename);
		
		// user serverNumber to send files to particular file
		SetUpNetworking(serverNumber,filename, message);
		// Send a message about read, write or append and then the message 
		// But how to make the server know about the particular opearation
	}

	private void appendToFile(String filename) {
		// still have to consult m-server for append to the end of the file
		//presently just using the create
		try {
			int serverNumber = SetMetadataServer("append", filename);
			System.out.println(serverNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readFromFile(String filename) {
		//consult meta data server and read
	}
	
	private void SetUpNetworking(int serverNumber,String filename, String message) throws IOException {
		
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
		finally {
			//client.close();
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
				
			}
			
			serverNumber = readResponse(client, in);
			System.out.println("Server response is:"+ serverNumber);
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

		System.out.println("Response from server:");
		while ((userInput = in.readLine()) != null) {
			System.out.println(userInput);
			String parts[] = userInput.split(":");
			return Integer.parseInt(parts[1]);
		}
		return 0;
	}
}
