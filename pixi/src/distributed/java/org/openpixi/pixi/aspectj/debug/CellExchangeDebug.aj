package org.openpixi.pixi.aspectj.debug;

import org.aspectj.lang.annotation.AdviceName;
import org.openpixi.pixi.distributed.Worker;
import org.openpixi.pixi.physics.grid.Grid;

/**
 * Prints out the ids of the cells on each node before and after the cell exchange.
 * With the printed ids one can verify whether the ghost and border cells were well matched.
 * The id's should not change across the iterations.
 * The id is cell's x and y index in string format.
 */
public privileged aspect CellExchangeDebug extends DistributedSimulationDebug {

	/** Assures that one thread completes the printing of debugging information
	 * before other thread starts. */
	private static Object lock = new Object();

	pointcut distInterpolateToParticle(Grid grid):
		execution(* *..DistributedInterpolationIterator.interpolateToParticle(
			*..List<*..Particle>, Grid))
		&& args(.., grid);

	@AdviceName("printCellIDsBeforeExchange")
	before(Worker w, Grid g):  distInterpolateToParticle(g) && underWorkerStep(w) {
		synchronized (lock) {
			System.out.println("\nNode " + w.workerID);
			System.out.println("cell ids before exchange:");
			printCellIDs(g);
		}
	}

	@AdviceName("printCellIDsAfterExchange")
	before(Worker w, Grid g):  distInterpolateToParticle(g) && underWorkerStep(w) {
		synchronized (lock) {
			System.out.println("\nNode " + w.workerID);
			System.out.println("cell ids after exchange:");
			printCellIDs(g);
		}
	}


	private void printCellIDs(Grid grid) {
		int xmin = -Grid.EXTRA_CELLS_BEFORE_GRID;
		int ymin = -Grid.EXTRA_CELLS_BEFORE_GRID;
		int xmax = grid.getNumCellsX() + Grid.EXTRA_CELLS_AFTER_GRID - 1;
		int ymax = grid.getNumCellsY() + Grid.EXTRA_CELLS_AFTER_GRID - 1;

		for (int y = ymin; y <= ymax; ++y) {
			for (int x = xmin; x <= xmax; ++x) {
				System.out.print(String.format("%s ", grid.getCell(x, y).id));
			}
			System.out.println();
		}
	}
}
