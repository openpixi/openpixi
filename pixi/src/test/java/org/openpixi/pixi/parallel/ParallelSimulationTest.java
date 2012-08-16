package org.openpixi.pixi.parallel;

import junit.framework.TestCase;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.solver.Euler;
import org.openpixi.pixi.physics.util.ClassCopier;
import org.openpixi.pixi.physics.util.ResultsComparator;

/**
 * Tests the parallel (threaded) version of openpixi.
 * Multi-threaded and also single-threaded simulations and then compares the results.
 */
public class ParallelSimulationTest extends TestCase {

	public void testParallelSimulation() {
		Settings defaultSettings = new Settings();
		defaultSettings.setGridCellsX(100);
		defaultSettings.setGridCellsY(100);
		defaultSettings.setNumOfParticles(2000);
		defaultSettings.setIterations(10);
		defaultSettings.setParticleSolver(new Euler());

		Simulation singleThreadedSimulation = new Simulation(defaultSettings);

		Settings multiThreadedSettings = ClassCopier.copy(defaultSettings);
		multiThreadedSettings.setNumOfThreads(2);
		Simulation multiThreadedSimulation = new Simulation(multiThreadedSettings);

		singleThreadedSimulation.run();
		multiThreadedSimulation.run();

		ResultsComparator comparator = new ResultsComparator();
		comparator.compare(
				singleThreadedSimulation.particles, multiThreadedSimulation.particles,
				singleThreadedSimulation.grid, multiThreadedSimulation.grid);
	}
}
