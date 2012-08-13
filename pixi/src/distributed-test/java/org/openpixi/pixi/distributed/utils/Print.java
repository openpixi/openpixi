package org.openpixi.pixi.distributed.utils;

import java.util.Map;

/**
 * Small helper for printing out information about passed and failed tests when running tests
 * under various different settings.
 */
public class Print {
	public static void testResults(Map<String, Boolean> resultsMap) {
		System.out.println();
		for (String testName: resultsMap.keySet()) {
			String result = null;
			if (resultsMap.get(testName)) {
				result = "PASS";
			}
			else {
				result = "FAIL";
			}
			System.out.println(result + " ..... " + testName);
		}
	}
}
