package org.openpixi.pixi.parallel.cellaccess;

import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.IntBox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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
	int numOfThreads;

	private List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
	private ExecutorService threadExecutor;


	public ParallelCellIterator(int numOfThreads, ExecutorService threadExecutor) {
		this.threadExecutor = threadExecutor;
		this.numOfThreads = numOfThreads;
		for (int i = 0; i < numOfThreads; ++i) {
			tasks.add(new Task(i, numOfThreads));
		}
	}


	public void execute(Grid grid, CellAction action) {
		this.grid = grid;
		this.action = action;
		try {
			List<Future<Object>> futures = threadExecutor.invokeAll(tasks);
			for (Future<Object> f : futures) {
				// Retrieving the result throws possible exceptions
				f.get();
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			// Throw exceptions that happened in a thread
			throw new RuntimeException(e);
		}
	}

	public CellIterator copy(){
		ParallelCellIterator copy = new ParallelCellIterator(this.numOfThreads, this.threadExecutor);
		copy.dimensions = dimensions.copy();
		copy.numOfCells = this.numOfCells;

		return copy;
	}

	@Override
	public void setNormalMode(int[] numCells) {
		super.setNormalMode(numCells);
		numOfCells = dimensions.getNumCells();
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
				action.execute(grid, cellIdx);
			}
			return null;
		}
	}
}
