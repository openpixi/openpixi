package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Settings;

/**
 * Tests the distribution and collection of results without running the simulation.
 * Since it setups network connection it is not implemented as a JUnit test.
 */
public class DistributeAndCollectTest {

	public static void main(String[] args) throws Exception {
		Settings settings = new Settings();
		settings.setNumOfNodes(8);
		settings.setGridCellsX(32);
		settings.setGridCellsY(64);
		settings.setNumOfParticles(10);
		// Do not run the simulation in this test
		settings.setIterations(0);

		IplServer.start();
		new EmulatedDistributedEnvironment(settings).runAtOnce();
		IplServer.end();

		System.out.println("OK");
	}
}
