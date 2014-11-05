package server;

import java.util.Properties;

import utilities.UsefulMethods;

public class StartServers {
	public static void main(String[] args) {
		UsefulMethods usefulmthds = UsefulMethods.getUsefulMethodsInstance();
		Properties prop =usefulmthds.getPropertiesFile("spec.properties");
		String numServers = prop.getProperty("numofservers");
		int servers = Integer.parseInt(numServers);
		for(int i=1; i<= servers; i++) {
			//Thread t = new Thread(new Server(i));
			//t.start();
		}
	}
}
