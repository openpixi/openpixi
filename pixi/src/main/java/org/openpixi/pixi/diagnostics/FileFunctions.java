package org.openpixi.pixi.diagnostics;

import java.io.File;
import java.text.DecimalFormat;

public class FileFunctions {

	private static DecimalFormat decimalFormat = new DecimalFormat("0.################E0");

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


	/**
	 * Converts a 1D double array to a tab-separated values (TSV) string.
	 * @param array	1D double array
	 * @return	String with the values of the array as TSV.
	 */
	public static String generateTSVString(double[] array) {
		StringBuilder outputStringBuilder = new StringBuilder();
		DecimalFormat formatter = new DecimalFormat("0.################E0");
		for (int i = 0; i < array.length; i++) {
			outputStringBuilder.append(formatter.format(array[i]));
			if(i < array.length - 1) {
				outputStringBuilder.append("\t");
			}
		}
		return outputStringBuilder.toString();
	}

	/**
	 * Convert a double to a String in scientific notation.
	 * @param v input double
	 * @return  formatted string
	 */
	public static String format(double v) {
		return decimalFormat.format(v);
	}

	/**
	 * Convert a float to a String in scientific notation.
	 * @param v input float
	 * @return  formatted string
	 */
	public static String format(float v) {
		return decimalFormat.format(v);
	}
}
