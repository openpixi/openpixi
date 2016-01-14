package org.openpixi.pixi.diagnostics;

import java.io.File;

public class FileFunctions {

	/**
	 * Returns a File instance for a given path. Directories are created if they do not exist yet.
	 *
	 * @param path String with path or filename
	 * @return     File instance
	 */
	public static File getFile(String path) {
		File file = new File(path);
		File folder = file.getParentFile();
		if(!folder.exists()) {
			boolean result = folder.mkdirs();
			if(!result) {
				System.out.println("FileFunctions: Error creating path for file " + path);
			}
		}
		return file;
	}

	/**
	 * Clear a file if it already exists.
	 *
	 * @param path String with path or filename
	 */
	public static void clearFile(String path) {
		File file = new File(path);
		if(file.exists()) {
			boolean result = file.delete();
			if(!result) {
				System.out.println("FileFunctions: Error deleting file " + path);
			}
		}
	}

}
