package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the distribution and collection of results without running the simulation.
 * Since it setups network connection it is not implemented as a JUnit test.
 */
public class DistributeAndCollectTest {

	private static final Double TOLERANCE = 1e-10;

	public static void main(String[] args) throws Exception {
		Settings settings = new Settings();
		settings.setNumOfNodes(16);
		settings.setGridCellsX(32);
		settings.setGridCellsY(64);

		Thread server = startIplServer();
		setRequiredSystemProperties();

		List<Thread> threads = new ArrayList<Thread>();
		List<Node> nodes = new ArrayList<Node>();

		// Start all the nodes
		for (int i = 0; i < settings.getNumOfNodes(); i++) {
			Node node = new Node(settings);
			Thread thread = new Thread(node);

			thread.start();

			threads.add(thread);
			nodes.add(node);
		}

		// Wait for all the nodes
		for (Thread node: threads) {
			node.join();
		}
		// Stop the server
		server.interrupt();

		// Compare the result at master
		for (Node n: nodes) {
			if (n.isMaster()) {
				compareInitialAndFinalState(n.getMaster());
				System.out.println("OK (data was successfully distributed and collected)");
			}
		}
	}


	private static void setRequiredSystemProperties() {
		System.setProperty("ibis.server.address", "localhost");
		System.setProperty("ibis.pool.name", "test");
	}


	private static Thread startIplServer() {
		Thread server = new Thread(new Runnable() {
			public void run() {
				ibis.ipl.server.Server.main(new String[] {"--events"});
			}
		});
		server.start();
		return server;
	}


	private static void compareInitialAndFinalState(Master master) {
		compareParticleLists(master.getInitialParticles(), master.getFinalParticles(), TOLERANCE);
		compareGrids(master.getInitialGrid(), master.getFinalGrid(), TOLERANCE);
	}


	private static void compareParticleLists(
			List<Particle> initialParticles, List<Particle> finalParticles, double tolerance) {

		if (initialParticles.size() < finalParticles.size()) {
		    fail("New particles arrived back to master!");
	    }
		if (initialParticles.size() > finalParticles.size()) {
			fail("Too few particles arrived back to master!");
		}

		for (Particle p: initialParticles) {
			if (!findParticle(p, finalParticles, tolerance)) {
				fail("Could not find particle " + p + " in the list of final particles!");
			}
		}
	}


	private static boolean findParticle(
			Particle p, List<Particle> particles, Double tolerance) {
		boolean retval = false;
		for (Particle p2: particles) {
			if (compareParticles(p, p2, tolerance)) {
				return true;
			}
		}
		return retval;
	}


	/**
	 * Compares just the position.
	 */
	private static boolean compareParticles(Particle p1, Particle p2, Double tolerance) {
		if (Math.abs(p1.getX()-p2.getX()) > tolerance) {
			return false;
		}
		if (Math.abs(p1.getY()-p2.getY()) > tolerance) {
			return false;
		}
		return true;
	}


	private static void compareGrids(Grid initialGrid, Grid finalGrid, double tolerance) {
		if (initialGrid.getNumCellsX() < finalGrid.getNumCellsX()) {
			fail("Initial grid is smaller in X direction!");
		}
		if (initialGrid.getNumCellsX() > finalGrid.getNumCellsX()) {
			fail("Initial grid is larger in X direction!");
		}
		if (initialGrid.getNumCellsY() < finalGrid.getNumCellsY()) {
			fail("Initial grid is smaller in Y direction!");
		}
		if (initialGrid.getNumCellsY() > finalGrid.getNumCellsY()) {
			fail("Initial grid is larger in Y direction!");
		}

		for (int x = -Grid.EXTRA_CELLS_BEFORE_GRID;
		     x < initialGrid.getNumCellsX() + Grid.EXTRA_CELLS_AFTER_GRID; ++x) {
			for (int y = -Grid.EXTRA_CELLS_BEFORE_GRID;
			     y < initialGrid.getNumCellsY() + Grid.EXTRA_CELLS_AFTER_GRID; ++y) {

				Cell initialCell = initialGrid.getCell(x,y);
				Cell finalCell = finalGrid.getCell(x,y);
				if (!compareCells(initialCell, finalCell, tolerance)) {
					fail("Cells are not equal! " +
							"Initial: " + initialCell + " Final: " + finalCell);
				}
			}
		}

	}


	/**
	 * Compares just the electric and magnetic fields.
	 */
	private static boolean compareCells(Cell cellA, Cell cellB, double tolerance) {
		if (Math.abs(cellA.Ex - cellB.Ex) > tolerance) {
			return false;
		}
		if (Math.abs(cellA.Ey - cellB.Ey) > tolerance) {
			return false;
		}
		if (Math.abs(cellA.Bz - cellB.Bz) > tolerance) {
			return false;
		}
		return true;
	}


	private static void fail(String msg) {
		System.out.println(msg);
		System.exit(1);
	}
}
