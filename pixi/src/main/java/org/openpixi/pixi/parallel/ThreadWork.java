package org.openpixi.pixi.parallel;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Implements the distribution of work to threads.
 * For example, in particle mover assigns multiple particles to one thread.
 *
 * Why do we use Callable interface instead of Runnable?
 * - Because we want to use the invokeAll() method of ExecutorService
 *   to push the particles in parallel.
 * Why do we want to use the invokeAll() method and not the execute() or submit() method?
 * - Because execute() and submit() do not wait for the tasks to finish.
 */
public abstract class ThreadWork implements Callable<Object> {

	private int threadIdx;
	private int numOfThreads;
	private int numOfItems;

	public ThreadWork(int threadIdx, int numOfThreads, int numOfItems) {
		this.threadIdx = threadIdx;
		this.numOfThreads = numOfThreads;
		this.numOfItems = numOfItems;
	}

	public void setNumOfItems(int numOfItems) {
		this.numOfItems = numOfItems;
	}

	public Object call() {
		for (int item = threadIdx; item < numOfItems; item += numOfThreads) {
			doWork(item);
		}
		return null;
	}

	/**
	 * This is a template method and needs to be overriden by the children of this class.
	 * Performs a specific action on a particle with given index.
	 */
	public abstract void doWork(int item);


	public static void setNumOfItems(int numOfParticles, List<ThreadWork> workList) {
		for (ThreadWork threadWork: workList) {
			threadWork.setNumOfItems(numOfParticles);
		}
	}
}
