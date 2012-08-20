package org.openpixi.pixi.distributed.utils;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.fields.YeeSolver;
import org.openpixi.pixi.physics.force.ConstantForce;
import org.openpixi.pixi.physics.grid.ChargeConservingAreaWeighting;
import org.openpixi.pixi.physics.grid.InterpolatorAlgorithm;
import org.openpixi.pixi.physics.solver.Boris;
import org.openpixi.pixi.physics.solver.Euler;
import org.openpixi.pixi.physics.solver.LeapFrogDamped;
import org.openpixi.pixi.physics.solver.relativistic.BorisRelativistic;
import org.openpixi.pixi.physics.solver.relativistic.SemiImplicitEulerRelativistic;
import org.openpixi.pixi.physics.util.ClassCopier;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines various settings for distributed simulation testing.
 * Modify this class if you want to extend the coverage of tested settings.
 */
public class VariousSettings {

	public static Map<String, Settings> getSettingsMap() {
		Map<String, Settings> variousTestSettings = new HashMap<String, Settings>();
		Settings defaultSettings = getDefaultSettings();

		Settings settings = ClassCopier.copy(defaultSettings);
		settings.setNumOfNodes(2);
		variousTestSettings.put("2 nodes - self communication", settings);

		settings = ClassCopier.copy(defaultSettings);
		settings.setParticleSolver(new Euler());
		variousTestSettings.put("Euler", settings);

		settings = ClassCopier.copy(defaultSettings);
		settings.setNumOfThreads(5);
		settings.setParticleSolver(new Euler());
		variousTestSettings.put("Threaded version", settings);

		// Fails probably because Boris remembers a lot of information about the force
		// in the particle. This information remembering probably causes problem when particles
		// are transferred from one node to the other.
		// TODO find solution
		settings = ClassCopier.copy(defaultSettings);
		settings.setParticleSolver(new Boris());
		variousTestSettings.put("Boris", settings);

		settings = ClassCopier.copy(defaultSettings);
		settings.setParticleSolver(new SemiImplicitEulerRelativistic(
				settings.getCellWidth() / settings.getTimeStep()));
		variousTestSettings.put("SemiImplicitEulerRelativistic", settings);

		settings = ClassCopier.copy(defaultSettings);
		settings.setParticleSolver(new LeapFrogDamped());
		variousTestSettings.put("LeapFrogDamped", settings);

		// Fails because in the distributed version we do additions upon particle's position
		// when it is transferred from one node to another. These additions are cause of the small
		// deviation from the non distributed simulation solution.
		// TODO find solution?
		settings = ClassCopier.copy(defaultSettings);
		settings.setIterations(5000);
		settings.setNumOfParticles(10);
		settings.setParticleSolver(new SemiImplicitEulerRelativistic(
				settings.getCellWidth() / settings.getTimeStep()));
		variousTestSettings.put("5000 iterations", settings);

		// TODO find solution
		settings = ClassCopier.copy(defaultSettings);
		settings.setInterpolator(new ChargeConservingAreaWeighting());
		variousTestSettings.put("ChargeConservingAreaWeighting", settings);

		settings = ClassCopier.copy(defaultSettings);
		settings.setInterpolator(new InterpolatorAlgorithm());
		variousTestSettings.put("BaseInterpolator", settings);

		// Fails because YeeSolver calculates the fields at the borders of the simulation area
		// differently then in the center, One needs to remake the YeeSolver in the same style
		// as SimpleFieldSolver.
		// TODO find solution
		settings = ClassCopier.copy(defaultSettings);
		settings.setGridSolver(new YeeSolver());
		variousTestSettings.put("YeeSolver", settings);

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

		return variousTestSettings;
	}


	public static Settings getDefaultSettings() {
		Settings settings = new Settings();
		settings.setNumOfNodes(16);
		settings.setGridCellsX(64);
		settings.setGridCellsY(128);
		settings.setSimulationWidth(10 * settings.getGridCellsX());
		settings.setSimulationHeight(10 * settings.getGridCellsY());
		settings.setNumOfParticles(100);
		settings.setIterations(100);
		// Ensures that the particles do not get too fast.
		settings.setParticleSolver(new BorisRelativistic(
				settings.getSimulationWidth() / settings.getTimeStep()));
		return settings;
	}
}
