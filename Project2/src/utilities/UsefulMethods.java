package utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;

public class UsefulMethods {
	
	private static volatile UsefulMethods instance = null;
	
	private UsefulMethods() {
		
	}

	public static UsefulMethods getUsefulMethodsInstance() {
		synchronized (UsefulMethods.class) {
			// Double check
			if (instance == null) {
				System.out.println("UsefulMethods : I am being created");
				instance = new UsefulMethods();
			}
		}
		return instance;
	}
	
	public Properties getPropertiesFile(String filename) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("resource/"+filename+"");
			prop.load(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}

	
	public int randomServer() {
		Random r = new Random();
		int Low = 1;
		int High = 3; // This should be 15
		int randomNum = r.nextInt((High - Low) + 1) + Low;
		return randomNum;
	}
	
	public String getTime() {
		final Calendar cal = Calendar.getInstance();
    	final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    	return sdf.format(cal.getTime());
	}
}
