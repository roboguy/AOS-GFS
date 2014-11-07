package server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;

import utilities.UsefulMethods;

public class ServerHeartBeat implements Runnable {
	
	int serverNumber = 0;
	PrintWriter out = null;
	public ServerHeartBeat(int serverNum) {
		this.serverNumber = serverNum;
	}

	@Override
	public void run() {
		Properties ServerPort = UsefulMethods.getUsefulMethodsInstance().getPropertiesFile("spec.properties");
		String serverName = ServerPort.getProperty("metadataserver");
		String portString = ServerPort.getProperty("metadataport");//Integer.parseInt(args[1]);
		int port = Integer.parseInt(portString.trim());
		
		String filePath = "/home/004/s/sm/smm130130/AOSproject2/FileSystem/server"+serverNumber;
		
		try {
			@SuppressWarnings("resource")
			
			Socket medatdataServer = new Socket(serverName, port);
			out = new PrintWriter(medatdataServer.getOutputStream(), true);
			
			while(true) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				File[] files = new File(filePath).listFiles();
				if(files.length > 0) {
					showFiles(files);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {			
			//medatdataServer.close();
		}
	}

	private void showFiles(File[] files) {
		for (File file : files) {
			if (file.isDirectory()) {
				showFiles(file.listFiles()); // Calls same method again.
			} else {
				String filename = file.getName();
				int fileLength = (int) file.length();
				long lastUpdated = file.lastModified();
				out.println("heartbeat:" + serverNumber + ":" + filename + ":"+ fileLength + ":" + lastUpdated);
				out.flush();
			}
		}
	}
}
