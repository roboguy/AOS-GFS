package utilities;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class StartProgram {
	public static void main(String[] args) throws IOException {
		

		UsefulMethods um = UsefulMethods.getUsefulMethodsInstance();
		Properties prop = um.getPropertiesFile("spec.properties");
		
		String filePath = prop.getProperty("filesystem");
		File[] files = new File(filePath).listFiles();
		DivideFiles df = new DivideFiles(filePath);
		df.showFiles(files);
	}
}
