package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Settings;

import java.util.Map;

/**
 * Runs multiple tests of the distributed simulation.
 * The distribution is emulated by threads.
 */
public class ComplexDistSimTest {

	public static void main(String[] args) throws Exception {
		Map<String, Settings> settingsMap = VariousSettings.getSettingsMap();
		IplServer.start();
		for (String testName: settingsMap.keySet()) {
			System.out.println("Running test " + testName);
			new EmulatedDistributedEnvironment(settingsMap.get(testName)).runAtOnce();
			System.out.println();
		}
		IplServer.end();
	}



}
