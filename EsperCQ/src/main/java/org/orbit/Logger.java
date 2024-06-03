package org.orbit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

	private FileWriter fw;
	private BufferedWriter bw;
	private boolean printDetails;
	
	public Logger(String file, boolean printAllDetails) throws IOException {		
		init(file);
		printDetails=printAllDetails;
	}
	
	public Logger(String file) throws IOException {
		init(file);
		printDetails=true;
	}
	
	private void init(String file) throws IOException {
		File statFile = new File(file);
		if (!statFile.exists()) {
			statFile.createNewFile();
		}
		fw = new FileWriter(statFile.getAbsoluteFile());
		bw = new BufferedWriter(fw);
	}

	public void log(String string) throws IOException {
		bw.write(string);
	}
	
	public void lognl(String string) throws IOException {
		bw.write(string);
		bw.newLine();
	}
	
	public void lognlIfPrintDetailsRequired(String string) throws IOException {
		if(printDetails) {
			lognl(string);
		}
	}
	
	public void flushLogger() throws IOException {
		bw.flush();
	}
	
	public void closeLogger() throws IOException {
		bw.close();
	}
	
	

}
