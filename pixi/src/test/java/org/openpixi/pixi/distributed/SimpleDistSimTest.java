package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Settings;

/**
 * Runs a single simple distributed simulation test with custom settings.
 * Intended mainly for debugging.
 */
public class SimpleDistSimTest {

	public static void main(String[] args) {
		Settings settings = new Settings();
		settings.setNumOfNodes(8);
		settings.setGridCellsX(32);
		settings.setGridCellsY(64);
		settings.setNumOfParticles(10);
		// If number of iterations is set to 0 we get a simple test
		// of problem distribution and results collection.
		settings.setIterations(100);

		IplServer.start();
		new EmulatedDistributedEnvironment(settings).runAtOnce();
		IplServer.end();
	}
}
