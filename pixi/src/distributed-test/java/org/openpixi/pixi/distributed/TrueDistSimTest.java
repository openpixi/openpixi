package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.utils.Print;
import org.openpixi.pixi.distributed.utils.VariousSettings;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.util.ComparisonFailedException;
import org.openpixi.pixi.physics.util.ResultsComparator;

import java.util.HashMap;
import java.util.Map;

/**
 * Runs a true distributed simulation test under various settings.
 * True means that the class needs to be started by multiple jvm's or multiple computers.
 * Requires the ipl server to be running.
 */
public class TrueDistSimTest {

	public static void main(String[] args) throws Exception {

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

		assert numOfNodes > 1: "Invalid number of nodes!";
		assert iplServer != null: "Ipl server was not specified!";

		Map<String, Settings> settingsMap = VariousSettings.getSettingsMap();
		Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();
		for (String testName: settingsMap.keySet()) {
			Settings stt = settingsMap.get(testName);
			stt.setNumOfNodes(numOfNodes);
			stt.setIplServer(iplServer);

			Node n = new Node(stt);
			n.run();
			try {
				compareResult(stt, n, testName);
				resultsMap.put(testName, true);
			} catch (ComparisonFailedException e) {
				System.out.println(e.getMessage());
				resultsMap.put(testName, false);
			}
		}

		Print.testResults(resultsMap);
	}


	private static void compareResult(Settings stt, Node n, String testName) {
		if (n.isMaster()) {
			System.out.println("Comparing result of " + testName + " test");

			Simulation localSimulation = new Simulation(stt);
			localSimulation.run();

			Master master = n.getMaster();
			ResultsComparator comparator = new ResultsComparator();
			comparator.compare(
					localSimulation.particles, master.getFinalParticles(),
					localSimulation.grid, master.getFinalGrid());
		}
	}
}
