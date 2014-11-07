package trail;

import java.io.File;

public class Sample2 {
	public static void main(String... args) {
	    File[] files = new File("D:/UTD/Personal/Algorithms/Project2/src/server").listFiles();
	    System.out.println(files.length);
	    showFiles(files);
	}

	public static void showFiles(File[] files) {
	    for (File file : files) {
	        if (file.isDirectory()) {
	            System.out.println("Directory: " + file.getName());
	            showFiles(file.listFiles()); // Calls same method again.
	        } else {
	        	String fileName[] = file.getName().split("\\.");
	            System.out.println("File: " + file.getName() + "File Size is : " + fileName[0]);
	        } 
	    }
	}
}
