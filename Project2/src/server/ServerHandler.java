package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerHandler implements Runnable {
	
	Socket sock;
	BufferedReader reader;
	HandleFileReadWrite hfrw = new HandleFileReadWrite();
	
	public ServerHandler(Socket client) throws IOException {
		sock = client;
		InputStreamReader in = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(in);
	}

	@Override
	public void run() {
		String msg = null;
		try {
			while((msg = reader.readLine()) != null) {
				String parts[] = msg.split(":");
				System.out.println("Received on the server side : "+parts[0]);
				if(parts[0].equalsIgnoreCase("write")) {
					hfrw.createAndWriteToFile(parts[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
