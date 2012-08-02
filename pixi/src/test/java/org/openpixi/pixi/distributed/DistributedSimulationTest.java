package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.solver.Euler;

/**
 * Tests the distributed version of openpixi against the non distributed version.
 */
public class DistributedSimulationTest {

	public static void main(String[] args) throws Exception {
		Settings settings = new Settings();
		settings.setNumOfNodes(4);
		settings.setGridCellsX(16);
		settings.setGridCellsY(16);
		settings.setSimulationWidth(160);
		settings.setSimulationHeight(160);
		settings.setNumOfParticles(10);
		settings.setIterations(100);
		settings.setParticleSolver(new Euler());

		new SimulatedDistributedEnvironment(settings).runInSteps();

		System.out.println("OK");
	}


	private static void createParticles(Settings settings) {
		Particle p1 = new Particle();
		p1.setX(1);
		p1.setY(1);
		p1.setVx(1);
		p1.setVy(1);
		p1.setRadius(1);
		p1.setMass(1);
		p1.setCharge(0.1);
		settings.addParticle(p1);
	}
}
