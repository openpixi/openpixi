package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.force.ConstantForce;
import org.openpixi.pixi.physics.grid.ChargeConservingAreaWeighting;
import org.openpixi.pixi.physics.grid.Interpolator;
import org.openpixi.pixi.physics.solver.Boris;
import org.openpixi.pixi.physics.solver.Euler;
import org.openpixi.pixi.physics.solver.relativistic.SemiImplicitEulerRelativistic;
import org.openpixi.pixi.physics.util.ClassCopier;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests the distributed version of openpixi against the non distributed version
 * under various but not all settings.
 */
public class DistributedSimulationTest {

	public static void main(String[] args) throws Exception {

		Map<String, Settings> variousTestSettings = new HashMap<String, Settings>();
		Settings defaultSettings = getDefaultSettings();

		Settings settings = ClassCopier.copy(defaultSettings);
		variousTestSettings.put("Euler", settings);

		settings = ClassCopier.copy(defaultSettings);
		settings.setParticleSolver(new Boris());
		variousTestSettings.put("Boris", settings);

		settings = ClassCopier.copy(defaultSettings);
		settings.setParticleSolver(
				new SemiImplicitEulerRelativistic(settings.getSpeedOfLight()));
		variousTestSettings.put("SemiImplicitEulerRelativistic", settings);

		settings = ClassCopier.copy(defaultSettings);
		settings.setInterpolator(new ChargeConservingAreaWeighting());
		variousTestSettings.put("ChargeConservingAreaWeighting", settings);

		settings = ClassCopier.copy(defaultSettings);
		settings.setInterpolator(new Interpolator());
		variousTestSettings.put("BaseInterpolator", settings);

		// Fails because YeeSolver relies too much on gridCellsX and gridCellsY which differ
		// in the distributed and local simulations.
		// TODO find solution
//		settings = ClassCopier.copy(defaultSettings);
//		settings.setGridSolver(new YeeSolver());
//		variousTestSettings.put("YeeSolver", settings);

		// Fails because SpringForce uses particle's absolute y position to calculate the force.
		// Since the y position in local and distributed simulation differs,
		// it also has a different effect.
		// TODO find solution
//		settings = ClassCopier.copy(defaultSettings);
//		settings.addForce(new SpringForce());
//		variousTestSettings.put("SpringForce", settings);

		settings = ClassCopier.copy(defaultSettings);
		ConstantForce constantForce = new ConstantForce();
		constantForce.bz = -1;
		settings.addForce(constantForce);
		variousTestSettings.put("MagneticForce", settings);

		IplServer.start();
		for (String testName: variousTestSettings.keySet()) {
			System.out.println("Running test " + testName);

			new SimulatedDistributedEnvironment(variousTestSettings.get(testName)).runInSteps();

			System.out.println("OK");
			System.out.println();
		}
		IplServer.end();
	}


	private static Settings getDefaultSettings() {
		Settings settings = new Settings();
		settings.setNumOfNodes(8);
		settings.setGridCellsX(64);
		settings.setGridCellsY(128);
		settings.setSimulationWidth(160);
		settings.setSimulationHeight(160);
		settings.setNumOfParticles(10);
		settings.setIterations(100);
		settings.setParticleSolver(new Euler());
		return settings;
	}


	/**
	 * Used for debugging purposes when there is a need of a specific particle(s).
	 */
	private static void createParticles(Settings settings) {
		Particle p1 = new Particle();
		p1.setX(10);
		p1.setY(75);
		p1.setVx(1);
		p1.setVy(0);
		p1.setRadius(1);
		p1.setMass(1);
		p1.setCharge(0.1);
		settings.addParticle(p1);
	}
}
