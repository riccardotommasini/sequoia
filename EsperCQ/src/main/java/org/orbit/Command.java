package org.orbit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Command {

	public static void executeCommand(String command, Logger logger ) {
		Runtime r = Runtime.getRuntime();
		Process process=null;
		BufferedReader reader = null;
		try {
			process = r.exec(command);
			reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				logger.lognl(line);
			}		 
			reader.close();
			process.waitFor();
		} catch (IOException e) {
			System.out.println("IO Exception on :"+command);
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Interrupted Exception on :"+command+", stop process.");
			if(!process.equals(null)) {
				process.destroy();
				if(!reader.equals(null)){	
					try {
						reader.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}	
	}




}
