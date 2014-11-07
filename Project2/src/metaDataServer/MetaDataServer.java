package metaDataServer;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import utilities.UsefulMethods;

public class MetaDataServer {
	ServerSocket serverSock;
	int processNumber;
	
	public static void main(String[] args) {
		MetaDataServer mds = new MetaDataServer();
		mds.run();
	}

	public void run() {
		Properties MetadataServerPort = UsefulMethods.getUsefulMethodsInstance().getPropertiesFile("spec.properties");
		String portString = MetadataServerPort.getProperty("metadataport");//Integer.parseInt(args[1]);
		int port = Integer.parseInt(portString);
		System.out.println("Metadata Server port is " + port);
		try{
			serverSock = new ServerSocket(port);
			while(true) {
				Socket client = serverSock.accept();
				Thread t = new Thread(new MetadataHandler(client));
				t.start();				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
