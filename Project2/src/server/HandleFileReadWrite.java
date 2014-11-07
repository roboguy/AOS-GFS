package server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class HandleFileReadWrite {

	public void createAndWriteToFile(String filename,String serverNumber, String message) {
		String[] chunk = filename.split("\\.");
		String chunkName = chunk[0]+"-1";
		try {
			int serverNum = Integer.parseInt(serverNumber);

			FileWriter fileWritter = new FileWriter("/home/004/s/sm/smm130130/AOSproject2/FileSystem/server"+serverNum+"/"+chunkName, true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			bufferWritter.write(message);
			bufferWritter.flush();
			bufferWritter.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readFromFile(String chunkName, String ServerNumber, int seekposition, int bytesToRead) {
		try {
			System.out.println(new String(readFromFile("/home/004/s/sm/smm130130/AOSproject2/FileSystem/server"+ServerNumber+"/"+chunkName, seekposition, bytesToRead)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static byte[] readFromFile(String filePath, int position, int size)
			throws IOException {

		RandomAccessFile file = new RandomAccessFile(filePath, "r");
		file.seek(position);
		byte[] bytes = new byte[size];
		file.read(bytes);
		file.close();
		return bytes;
	}
}
