package trail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Sample {
	static final String FILEPATH = "D:/UTD/4thSemester/AOS/project2/sample/sample.txt";

	public static void main(String[] args) {
		try {
			appendFile();
			System.out.println(new String(readFromFile(FILEPATH, 8000, 30)));
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
	
	private static void appendFile() {
		String storedFile = "D:/personal/Fall/AOS/Project/file2-2";
		
		File file = new File(storedFile);
		long fileLength = file.length();
		System.out.println("after append file: "+fileLength);
		if(fileLength < 8192) {
			Character c = null;
			FileWriter writer;
			try {
				writer = new FileWriter(storedFile, true);
				while(file.length() == 8192) {
					writer.write(c);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("after append file: "+file.length());
	}
}
