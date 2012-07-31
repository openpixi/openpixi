package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;

import java.util.List;

/**
 * Compares the results of two simulations.
 */
public class SimulationComparator {

	private static final Double TOLERANCE = 1e-10;


	public void compare(
			List<Particle> expectedParticles, List<Particle> actualParticles,
			Grid expectedGrid, Grid actualGrid) {

		compareParticleLists(expectedParticles, actualParticles, TOLERANCE);
		compareGrids(expectedGrid, actualGrid, TOLERANCE);
	}


	private void compareParticleLists(
			List<Particle> expectedParticles, List<Particle> actualParticles, double tolerance) {

		if (expectedParticles.size() < actualParticles.size()) {
			fail("There are more actual particles than expected!");
		}
		if (expectedParticles.size() > actualParticles.size()) {
			fail("There are less actual particles than expected!");
		}

		for (Particle p: expectedParticles) {
			if (!findParticle(p, actualParticles, tolerance)) {
				fail("Could not find particle " + p + " in the list of actual particles!");
			}
		}
	}


	private boolean findParticle(
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
	private boolean compareParticles(Particle p1, Particle p2, Double tolerance) {
		if (Math.abs(p1.getX()-p2.getX()) > tolerance) {
			return false;
		}
		if (Math.abs(p1.getY()-p2.getY()) > tolerance) {
			return false;
		}
		return true;
	}


	private void compareGrids(Grid expectedGrid, Grid actualGrid, double tolerance) {
		if (expectedGrid.getNumCellsX() < actualGrid.getNumCellsX()) {
			fail("Actual grid is larger in X direction!");
		}
		if (expectedGrid.getNumCellsX() > actualGrid.getNumCellsX()) {
			fail("Actual grid is smaller in X direction!");
		}
		if (expectedGrid.getNumCellsY() < actualGrid.getNumCellsY()) {
			fail("Actual grid is larger in Y direction!");
		}
		if (expectedGrid.getNumCellsY() > actualGrid.getNumCellsY()) {
			fail("Actual grid is smaller in Y direction!");
		}

		for (int x = -Grid.EXTRA_CELLS_BEFORE_GRID;
		     x < expectedGrid.getNumCellsX() + Grid.EXTRA_CELLS_AFTER_GRID; ++x) {
			for (int y = -Grid.EXTRA_CELLS_BEFORE_GRID;
			     y < expectedGrid.getNumCellsY() + Grid.EXTRA_CELLS_AFTER_GRID; ++y) {

				Cell expectedCell = expectedGrid.getCell(x,y);
				Cell actualCell = actualGrid.getCell(x,y);
				if (!compareCells(expectedCell, actualCell, tolerance)) {
					fail("Cells at [" + x + "," + y + "] are not equal! " +
							"Expected: " + expectedCell + " Actual: " + actualCell);
				}
			}
		}

	}


	/**
	 * Compares just the electric and magnetic fields.
	 */
	private boolean compareCells(Cell cellA, Cell cellB, double tolerance) {
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
