package org.openpixi.pixi.ui.util;

import java.io.*;

public class WriteFile {
	
	FileWriter fstream;
	BufferedWriter out;
	
	public WriteFile() {
		
	}
	
	public void writeFile(String filename, String output) {
		try {
			fstream = new FileWriter(filename + ".dat", true);
			out = new BufferedWriter(fstream);
			out.write(output);
			out.newLine();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
