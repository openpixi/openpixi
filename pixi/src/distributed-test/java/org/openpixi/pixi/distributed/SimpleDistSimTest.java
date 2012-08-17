package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.utils.EmulatedDistributedEnvironment;
import org.openpixi.pixi.distributed.utils.IplServer;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;

/**
 * Runs a single simple distributed simulation test with custom settings.
 * Intended mainly for debugging.
 */
public class SimpleDistSimTest {

	public static void main(String[] args) {
		Settings settings = new Settings();
		settings.setNumOfNodes(2);
		settings.setNumOfThreads(2);
		settings.setGridCellsX(8);
		settings.setGridCellsY(8);
		settings.setSimulationWidth(10 * settings.getGridCellsX());
		settings.setSimulationHeight(10 * settings.getGridCellsY());
		settings.setNumOfParticles(100);
		// If number of iterations is set to 0 we get a simple test
		// of problem distribution and results collection (we must use runAtOnce() method of
		// the EmulatedDistributedEnvironment).
		settings.setIterations(100);

		IplServer.start();
		new EmulatedDistributedEnvironment(settings).runInSteps();
		IplServer.end();
		System.out.println("PASSED ..... simple distributed simulation test");
	}


	/**
	 * Used for debugging purposes when there is a need of a specific particle(s).
	 */
	public static void createParticles(Settings settings) {
		Particle p1 = new Particle();
		p1.setX(75);
		p1.setY(35);
		p1.setVx(2);
		p1.setVy(0);
		p1.setRadius(1);
		p1.setMass(1);
		p1.setCharge(0.1);
		settings.addParticle(p1);
	}
}
