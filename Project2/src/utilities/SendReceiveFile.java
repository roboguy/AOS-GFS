package utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class SendReceiveFile {
	
	public void sendFile() throws IOException {
		
		ServerSocket serverSocket = new ServerSocket(15123); 
		Socket socket = serverSocket.accept(); 
		System.out.println("Accepted connection : " + socket);
		File transferFile = new File ("Document.doc");
		byte [] bytearray = new byte [(int)transferFile.length()]; 
		FileInputStream fin = new FileInputStream(transferFile); 
		BufferedInputStream bin = new BufferedInputStream(fin); 
		bin.read(bytearray,0,bytearray.length); 
		OutputStream os = socket.getOutputStream(); 
		System.out.println("Sending Files..."); 
		os.write(bytearray,0,bytearray.length); 
		os.flush(); 
		socket.close(); 
		System.out.println("File transfer complete");
	}
	
	public void receiveFile() throws IOException {
		int filesize=1022386;
		int bytesRead;
		int currentTot = 0;
		Socket socket = new Socket("127.0.0.1",15123);
		byte [] bytearray = new byte [filesize];
		InputStream is = socket.getInputStream();
		FileOutputStream fos = new FileOutputStream("copy.doc", true);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		bytesRead = is.read(bytearray,0,bytearray.length);
		currentTot = bytesRead;
		do { 
			bytesRead = is.read(bytearray, currentTot, (bytearray.length-currentTot)); 
			if(bytesRead >= 0) currentTot += bytesRead;
			} while(bytesRead > -1);
		bos.write(bytearray, 0 , currentTot); 
		bos.flush();
		bos.close(); 
		socket.close(); 
	}
}
