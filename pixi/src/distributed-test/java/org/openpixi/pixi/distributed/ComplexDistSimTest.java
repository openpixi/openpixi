package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.utils.EmulatedDistributedEnvironment;
import org.openpixi.pixi.distributed.utils.IplServer;
import org.openpixi.pixi.distributed.utils.Print;
import org.openpixi.pixi.distributed.utils.VariousSettings;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.util.ComparisonFailedException;

import java.util.HashMap;
import java.util.Map;

/**
 * Runs multiple tests of the distributed simulation.
 * The distribution is emulated by threads.
 */
public class ComplexDistSimTest {

	/**
	 * If the results of distributed and non-distributed simulations differ,
	 * the test still continues to test other settings.
	 * However, if there is a normal exception (other than ComparisonFailedException),
	 * we abort the testing immediately.
	 */
	public static void main(String[] args) throws Exception {

		Map<String, Settings> settingsMap = VariousSettings.getSettingsMap();
		Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

		IplServer.start();
		for (String testName: settingsMap.keySet()) {
			System.out.println("Running test " + testName);
			try {

				new EmulatedDistributedEnvironment(settingsMap.get(testName)).runAtOnce();
				resultsMap.put(testName, true);

			}
			catch (ComparisonFailedException e) {
				System.out.println(e.getMessage());
				resultsMap.put(testName, false);
			}
			catch (Exception e) {
				IplServer.end();
				throw new RuntimeException(e);
			}
			System.out.println();
		}
		Print.testResults(resultsMap);
		IplServer.end();
	}



}
