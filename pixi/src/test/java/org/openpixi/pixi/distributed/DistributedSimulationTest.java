package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;

/**
 * Tests the distributed version of openpixi against the non distributed version.
 */
public class DistributedSimulationTest {

	public static void main(String[] args) throws Exception {
		Settings settings = new Settings();
		settings.setNumOfNodes(2);
		settings.setGridCellsX(16);
		settings.setGridCellsY(16);
		settings.setNumOfParticles(10);
		settings.setIterations(1000);

		SimulatedDistributedEnvironment distributedEnv =
				new SimulatedDistributedEnvironment(settings);
		Simulation simulation = new Simulation(settings);

		// Run distributed simulation
		distributedEnv.run();

		// Run non-distributed simulation
		for (int i = 0; i < settings.getIterations(); ++i) {
			simulation.step();
		}

		// Compare results
		SimulationComparator comparator = new SimulationComparator();
		Master master = distributedEnv.getMaster();
		comparator.compare(
				simulation.particles, master.getFinalParticles(),
				simulation.grid, master.getFinalGrid());

		System.out.println("OK");
	}
}
