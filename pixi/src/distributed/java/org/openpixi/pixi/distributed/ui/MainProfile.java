package org.openpixi.pixi.distributed.ui;

import org.openpixi.pixi.distributed.Node;
import org.openpixi.pixi.distributed.profile.DistributedProfileInfo;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.solver.Boris;
import org.openpixi.pixi.profile.ProfileInfo;

/**
 * Based on the command line arguments runs either distributed or local simulation.
 * Afterwards displays profiling information.
 */
public class MainProfile {

	public static void main(String[] args) {

		// TODO replace with more powerfull settings class (command line arguments support)
		int numOfNodes = 0;
		String iplServer = null;
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals("-numOfNodes")) {
				++i;
				numOfNodes = Integer.parseInt(args[i]);
			}
			else if (args[i].equals("-iplServer")) {
				++i;
				iplServer = args[i];
			}
		}

		assert numOfNodes > 0: "Invalid number of nodes!";

		Settings settings = new Settings();
		settings.setNumOfThreads(1);
		settings.setGridCellsX(16);
		settings.setGridCellsY(16);
		settings.setSimulationWidth(10 * settings.getGridCellsX());
		settings.setSimulationHeight(10 * settings.getGridCellsY());
		settings.setNumOfParticles(10000);
		settings.setIterations(1000);
		settings.setParticleSolver(new Boris());
		settings.setNumOfNodes(numOfNodes);
		settings.setIplServer(iplServer);

		if (numOfNodes == 1) {
			Simulation simulation = new Simulation(settings);
			simulation.run();
			ProfileInfo.printProfileInfo();
		}
		else if (numOfNodes > 1) {
			assert iplServer != null;
			Node node = new Node(settings);
			node.run();
			DistributedProfileInfo.printProfileInfo();
		}

		settings.terminateThreads();
	}
}
