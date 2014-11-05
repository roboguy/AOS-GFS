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
		Socket client = SetUpNetworking("metadataserver", "metadataport");
		PrintWriter out = new PrintWriter(client.getOutputStream(), true);
		BufferedReader in =new BufferedReader(new InputStreamReader(client.getInputStream()));
		out.println("create"+":"+filename);
		
		int serverNumber = readResponse(client, in);
		System.out.println("Server response is:"+ serverNumber);
		client.close();out.close();in.close();
		// user serverNumber to send files to particular file
		Socket client1 = SetUpNetworking("server"+serverNumber, "server"+serverNumber+"port");
		// Send a message about read, write or append and then the message 
		// But how to make the server know about the particular opearation
		PrintWriter out1 = new PrintWriter(client1.getOutputStream(), true);
		out1.print("write:"+message);
		client1.close();out1.close();
	}

	private void appendToFile(String filename) {
		// consult m-server append to the end of the file		
	}

	private void readFromFile(String filename) {
		//consult meta data server and read
	}
	
	private Socket SetUpNetworking(String server, String serverport) throws IOException {
		
		Properties ServerPort = UsefulMethods.getUsefulMethodsInstance().getPropertiesFile("spec.properties");
		
		String serverName = ServerPort.getProperty(server);
		String portString = ServerPort.getProperty(serverport);//Integer.parseInt(args[1]);
		int port = Integer.parseInt(portString.trim());
		System.out.println("Connecting to "+serverName+".... with port ......"+port);
		
		Socket client = null;
		
		try {
			client = new Socket(serverName, port);			
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			//client.close();
		}
		return client;
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
