package org.openpixi.pixi.profile;

/**
 * Small helper for outputting the profiling information.
 */
public class Print {

	private static String timeInSec(long nanoTime) {
		return String.format("%7.3fs", nanoTime * 1e-9f);
	}

	private static  String timeInPercents(long partTime, long totalTime) {
		return String.format("%6.2f%%", partTime * 100 / (double)totalTime);
	}

	public static void totalTime(String timeName, long nanoTime) {
		System.out.println(String.format("%s ..... %s", timeInSec(nanoTime), timeName));
	}

	public static void partTime(String timeName, long partTime, long totalTime) {
		System.out.println(String.format(
				"%s\t%s ..... %s", timeInSec(partTime), timeInPercents(partTime, totalTime), timeName));
	}
}
