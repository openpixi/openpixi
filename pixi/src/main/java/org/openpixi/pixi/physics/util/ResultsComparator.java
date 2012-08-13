package org.openpixi.pixi.physics.util;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;

import java.util.List;

/**
 * Compares the results of two simulations.
 * In case of failure throws ComparisonFailedException.
 */
public class ResultsComparator {

	private static final Double TOLERANCE = 1e-10;

	private static final int NO_STEP_TRACKING = -1;
   	private int stepNo = NO_STEP_TRACKING;


	public ResultsComparator() {

	}


	/**
	 * In case of failure outputs also the step number in which the failure occurred.
	 */
	public ResultsComparator(int stepNo) {
		this.stepNo = stepNo;
	}


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
		if (Math.abs(p1.getX() - p2.getX()) > tolerance) {
			return false;
		}
		if (Math.abs(p1.getY() - p2.getY()) > tolerance) {
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

		/*
		 * We only compare the cells up till interpolation radius
		 * (ie. we do not compare the last row and column of the cells).
		 * The last row and column would be different in periodic boundaries because
		 * in the non distributed simulation these rows and cells point inside of the grid,
		 * whereas in distributed simulation they are not exchanged since they are not needed
		 * for any interpolation.
		 */
		int differences = 0;
		for (int x = -Grid.INTERPOLATION_RADIUS;
		     x < expectedGrid.getNumCellsX() + Grid.INTERPOLATION_RADIUS; ++x) {
			for (int y = -Grid.INTERPOLATION_RADIUS;
			     y < expectedGrid.getNumCellsY() + Grid.INTERPOLATION_RADIUS; ++y) {

				Cell expectedCell = expectedGrid.getCell(x,y);
				Cell actualCell = actualGrid.getCell(x,y);
				if (!compareCells(expectedCell, actualCell, tolerance)) {
					++differences;
					System.out.println(" -> differences in cell: " + x + "," + y);
				}
			}
		}

		if (differences > 0) {
			fail(String.format("%d cells were different!", differences));
		}
	}


	private boolean compareCells(Cell cellA, Cell cellB, double tolerance) {
		boolean ok = true;
		double difference = Math.abs(cellA.getEx() - cellB.getEx());
		if (difference > tolerance) {
			printSingleMemberDifference("ex", difference);
			ok = false;
		}
		difference = Math.abs(cellA.getEy() - cellB.getEy());
		if (difference > tolerance) {
			printSingleMemberDifference("ey", difference);
			ok = false;
		}
		difference = Math.abs(cellA.getBz() - cellB.getBz());
		if (difference > tolerance) {
			printSingleMemberDifference("bz", difference);
			ok = false;
		}
		difference = Math.abs(cellA.getExo() - cellB.getExo());
		if (difference > tolerance) {
			printSingleMemberDifference("exo", difference);
			ok = false;
		}
		difference = Math.abs(cellA.getEyo() - cellB.getEyo());
		if (difference > tolerance) {
			printSingleMemberDifference("eyo", difference);
			ok = false;
		}
		difference = Math.abs(cellA.getBzo() - cellB.getBzo());
		if (difference > tolerance) {
			printSingleMemberDifference("bzo", difference);
			ok = false;
		}
		difference = Math.abs(cellA.getJx() - cellB.getJx());
		if (difference > tolerance) {
			printSingleMemberDifference("jx", difference);
			ok = false;
		}
		Math.abs(cellA.getJy() - cellB.getJy());
		if (difference > tolerance) {
			printSingleMemberDifference("jy", difference);
			ok = false;
		}
		return ok;
	}


	private void printSingleMemberDifference(String member, double difference) {
		System.out.print(String.format("%s (%.6f) ", member,  difference));
	}


	private void fail(String msg) {
		StringBuilder finalMsg = new StringBuilder(msg);
		if (stepNo != NO_STEP_TRACKING) {
			finalMsg.append(" STEP NUMBER: " + stepNo);
		}
		finalMsg.append(", COMPARISON FAILED !!! ");
		throw new ComparisonFailedException(finalMsg.toString());
	}


	/**
	 * Used in debugging for fields comparison.
	 */
	private void printFields(Grid grid) {
		for (int y = -Grid.EXTRA_CELLS_BEFORE_GRID;
		     y < grid.getNumCellsY() + Grid.EXTRA_CELLS_AFTER_GRID; ++y) {
			for (int x = -Grid.EXTRA_CELLS_BEFORE_GRID;
			     x < grid.getNumCellsX() + Grid.EXTRA_CELLS_AFTER_GRID; ++x) {

				System.out.print(String.format("[%9.6f,%9.6f] ",
						grid.getCell(x, y).getEx(), grid.getCell(x, y).getEy()));
			}
			System.out.println();
		}
	}


	/**
	 * Used in debugging for currents comparison.
	 */
	private void printCurrents(Grid grid) {
		for (int y = -Grid.EXTRA_CELLS_BEFORE_GRID;
		     y < grid.getNumCellsY() + Grid.EXTRA_CELLS_AFTER_GRID; ++y) {
			for (int x = -Grid.EXTRA_CELLS_BEFORE_GRID;
			     x < grid.getNumCellsX() + Grid.EXTRA_CELLS_AFTER_GRID; ++x) {

				System.out.print(String.format("[%.6f,%.6f] ",
						grid.getCell(x, y).getJx(), grid.getCell(x, y).getJy()));
			}
			System.out.println();
		}
	}
}
