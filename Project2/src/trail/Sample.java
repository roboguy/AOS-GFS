package trail;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Sample {
	static final String FILEPATH = "D:/UTD/4thSemester/AOS/project2/sample/sample.txt";

	public static void main(String[] args) {
		try {
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
}
