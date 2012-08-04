package org.openpixi.pixi.aspectj.profile;

import org.openpixi.pixi.distributed.Node;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;

/**
 * Based on the command line arguments runs either distributed or local simulation.
 */
public class ProfileRunner {

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
		settings.setGridCellsX(128);
		settings.setGridCellsY(128);
		settings.setSimulationWidth(10 * settings.getGridCellsX());
		settings.setSimulationHeight(10 * settings.getGridCellsY());
		settings.setNumOfParticles(100);
		settings.setIterations(100);
		settings.setNumOfNodes(numOfNodes);
		settings.setIplServer(iplServer);

		if (numOfNodes == 1) {
			Simulation simulation = new Simulation(settings);
			simulation.run();
		}
		else if (numOfNodes > 1) {
			assert iplServer != null;
			Node node = new Node(settings);
			node.run();
		}
	}
}
