package org.openpixi.pixi.parallel.particleaccess;

import org.openpixi.pixi.physics.Particle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Executes action upon particles in parallel using threads.
 *
 * Why do we use Callable interface instead of Runnable?
 * - Because we want to use the invokeAll() method of ExecutorService.
 * Why do we want to use the invokeAll() method and not the execute() or submit() method?
 * - Because execute() and submit() do not wait for the tasks to finish.
 */
public class ParallelParticleIterator implements ParticleIterator {

	/* These are exposed here for inner classes
	   since they can to be passed to them as method arguments */
	private ParticleAction action;
	private List<Particle> particles;

	private List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
	private ExecutorService threadExecutor;

	public ParallelParticleIterator(int numOfThreads, ExecutorService threadExecutor) {
		this.threadExecutor = threadExecutor;
		for (int i = 0; i < numOfThreads; ++i) {
			tasks.add(new Task(i, numOfThreads));
		}
	}

	public void execute(List<Particle> particles, ParticleAction action) {
		this.action = action;
		this.particles = particles;

		try {
			threadExecutor.invokeAll(tasks);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private class Task implements Callable<Object> {

		private int threadIdx;
		private int numOfThreads;

		private Task(int threadIdx, int numOfThreads) {
			this.threadIdx = threadIdx;
			this.numOfThreads = numOfThreads;
		}

		public Object call() throws Exception {
			for (int particleIdx = threadIdx; particleIdx < particles.size(); particleIdx += numOfThreads) {
				action.execute(particles.get(particleIdx));
			}
			return null;
		}
	}
}
