package org.openpixi.pixi.ui.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class FileIO {

	static public String readFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		reader.close();

		return stringBuilder.toString();
	}

	static public void writeFile(File file, String string) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		byte[] contentInBytes = string.getBytes();
		out.write(contentInBytes);
		out.close();
	}

}
