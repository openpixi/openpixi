package org.openpixi.pixi.parallel.cellaccess;

import org.openpixi.pixi.physics.grid.Grid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Executes action upon cells in parallel using threads.
 * Can iterate also through extra cells based on the boolean parameter
 * includeExtraCells in constructor.
 */
public class ParallelCellIterator extends CellIterator {

	/* These are exposed here for inner classes
	   since they can not be passed to them as method arguments. */
	private Grid grid;
	private CellAction action;
	int numOfCells;

	private List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
	private ExecutorService threadExecutor;


	public ParallelCellIterator(int numOfThreads, ExecutorService threadExecutor) {
		this.threadExecutor = threadExecutor;
		for (int i = 0; i < numOfThreads; ++i) {
			tasks.add(new Task(i, numOfThreads));
		}
	}


	public void execute(Grid grid, CellAction action) {
		this.grid = grid;
		this.action = action;
		try {
			threadExecutor.invokeAll(tasks);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public void setNormalMode(int numCellsX, int numCellsY) {
		super.setNormalMode(numCellsX, numCellsY);
		numOfCells = dimensions.xsize() * dimensions.ysize();
	}


	@Override
	public void setExtraCellsMode(int numCellsX, int numCellsY) {
		super.setExtraCellsMode(numCellsX, numCellsY);
		numOfCells = dimensions.xsize() * dimensions.ysize();
	}


	private class Task implements Callable<Object> {

		private int threadIdx;
		private int numOfThreads;

		private Task(int threadIdx, int numOfThreads) {
			this.threadIdx = threadIdx;
			this.numOfThreads = numOfThreads;
		}

		public Object call() throws Exception {
			for (int cellIdx = threadIdx; cellIdx < numOfCells; cellIdx += numOfThreads) {
				int x = (cellIdx / dimensions.ysize()) + dimensions.xmin();
				int y = (cellIdx % dimensions.ysize()) + dimensions.ymin();
				action.execute(grid, x, y);
			}
			return null;
		}
	}
}
