package utilities;

import java.io.*;
import java.util.Properties;

public class DivideFiles {
    int sizeOfFiles = 0;// 1MB
    int randomServer = 0;
	UsefulMethods um;
	Properties prop;
	String filepath;
	
	public DivideFiles(String filePath) throws IOException {
		filepath = filePath;
		sizeOfFiles = 8 * 1024;
		um = UsefulMethods.getUsefulMethodsInstance();
		prop = um.getPropertiesFile("spec.properties");
	}
	
	public void devideFile(String filename, String filepath) throws IOException {
		
		BufferedInputStream bis = null;
		FileOutputStream out = null;
        String name = filename;
		System.out.println("name of the file got : "+name);
        int partCounter = 1;
        int tmp = 0;
		try {
			bis = new BufferedInputStream(new FileInputStream(filepath));
			byte[] buffer = new byte[sizeOfFiles];
	        while ((tmp = bis.read(buffer)) > 0) {
	    		randomServer = um.randomServer();
	            File newFile=new File("/home/004/s/sm/smm130130/AOSproject2/server/Server"+randomServer+"/"+name+""+partCounter++);
	            newFile.createNewFile();
	            out = new FileOutputStream(newFile);
	            out.write(buffer,0,tmp);
	        }
		} finally {
			if(bis != null) {
				bis.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}
	
	public void showFiles(File[] files) throws IOException {
		
	    for (File file : files) {
	        if (file.isDirectory()) {
	            System.out.println("Directory: " + file.getName());
	            showFiles(file.listFiles()); // Calls same method again.
	        } else {
	        	String filename[] = file.getName().split("\\.");
	        	int len = (int) file.length();
	            System.out.println("File: " + file.getName() + "File Size is : " + file.length());
	            if(len > 8192) {
	            	devideFile(filename[0], file.getAbsolutePath());
	            }
	        }
	    }
	}
}
