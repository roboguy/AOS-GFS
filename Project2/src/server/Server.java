package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import utilities.UsefulMethods;

public class Server {
	ServerSocket serverSock;
	
	public static void main(String[] args) {
		Server server = new Server();
		int firstArg;
		if (args.length > 0) {
		    try {
		        firstArg = Integer.parseInt(args[0]);
		        server.run(firstArg);
		    } catch (NumberFormatException e) {
		        System.err.println("Argument" + args[0] + " must be an integer.");
		        System.exit(1);
		    }
		}
	}

	public void run(int serverNumber) {
		Thread th = new Thread(new ServerHeartBeat(serverNumber));
		th.start();
		
		Properties MetadataServerPort = UsefulMethods.getUsefulMethodsInstance().getPropertiesFile("spec.properties");
		String portString = MetadataServerPort.getProperty("server"+serverNumber+"port");//Integer.parseInt(args[1]);
		int port = Integer.parseInt(portString);
		System.out.println("Server"+serverNumber+" port is " + port);
		try{
			serverSock = new ServerSocket(port);
			while(true) {
				Socket client = serverSock.accept();
				Thread t = new Thread(new ServerHandler(client));
				t.start();				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
