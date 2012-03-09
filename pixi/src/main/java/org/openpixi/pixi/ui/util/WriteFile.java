package org.openpixi.pixi.ui.util;

import java.io.*;

public class WriteFile {
	
	FileWriter fstream;
	BufferedWriter out;
	File file;
	
	public WriteFile() {
		
	}//filename + ".dat"
	
	public void writeFile(String filename, String filedirectory, String output) {
		try {
			file = new File(filedirectory + "\\" + filename + ".dat");
			fstream = new FileWriter(file, true);
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
