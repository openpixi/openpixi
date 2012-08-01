package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.solver.Euler;

/**
 * Tests the distributed version of openpixi against the non distributed version.
 */
public class DistributedSimulationTest {

	public static void main(String[] args) throws Exception {
		Settings settings = new Settings();
		settings.setNumOfNodes(2);
		settings.setGridCellsX(16);
		settings.setGridCellsY(16);
		settings.setNumOfParticles(1);
		settings.setIterations(10);
		settings.setParticleSolver(new Euler());

		new SimulatedDistributedEnvironment(settings).runInSteps();

		System.out.println("OK");
	}
}
