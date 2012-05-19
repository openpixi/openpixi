package org.openpixi.pixi.ui.util;

import java.io.*;

public class WriteFile {
	
	FileWriter fstream;
	BufferedWriter out;
	File file;
	
	public WriteFile() {
	}
	
	public WriteFile(String filename, String filedirectory) {
		try {
		file = new File(filedirectory + "\\" + filename + ".dat");
		fstream = new FileWriter(file, true);
		out = new BufferedWriter(fstream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeLine(String output) {
		try{
			out.write(output);
			out.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeFstream() {
		try{
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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
