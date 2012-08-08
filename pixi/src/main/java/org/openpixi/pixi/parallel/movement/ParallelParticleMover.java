package org.openpixi.pixi.parallel.movement;

import org.openpixi.pixi.parallel.ThreadWork;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.movement.ParticleMover;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.solver.Solver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 *  Multi-threaded particle mover.
 */
public class ParallelParticleMover extends ParticleMover {

	private Force force;
	private List<Particle> particles;
	private double timeStep;

	private ExecutorService threadsExecutor;

	private List<ThreadWork> pushTasks = new ArrayList<ThreadWork>();
	private List<ThreadWork> prepareTasks = new ArrayList<ThreadWork>();
	private List<ThreadWork> completeTasks = new ArrayList<ThreadWork>();


	public ParallelParticleMover(
			Solver psolver, ParticleBoundaries boundaries,
			ExecutorService threadsExecutor,
			int numOfThreads) {
		super(psolver, boundaries);

		this.threadsExecutor = threadsExecutor;

		for (int i = 0; i < numOfThreads; i++) {
			Push push = new Push(i, numOfThreads, 0);
			pushTasks.add(push);

			Prepare prepare = new Prepare(i, numOfThreads, 0);
			prepareTasks.add(prepare);

			Complete complete = new Complete(i, numOfThreads, 0);
			completeTasks.add(complete);
		}
	}


	@Override
	public void push(List<Particle> particles, Force force, double tstep) {
		try {

			// The particles, force and time step can change and thus, are set in each iteration
			setFields(particles, force, tstep);
			ThreadWork.setNumOfItems(particles.size(), pushTasks);
			threadsExecutor.invokeAll(pushTasks);

		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	@Override
	public void prepare(List<Particle> particles, Force force, double tstep) {
		try {

			setFields(particles, force, tstep);
			ThreadWork.setNumOfItems(particles.size(), prepareTasks);
			threadsExecutor.invokeAll(prepareTasks);

		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	@Override
	public void complete(List<Particle> particles, Force force, double tstep) {
		try {

			setFields(particles, force, tstep);
			ThreadWork.setNumOfItems(particles.size(), completeTasks);
			threadsExecutor.invokeAll(completeTasks);

		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	private void setFields(List<Particle> particles, Force force, double tstep) {
		this.particles = particles;
		this.force = force;
		this.timeStep = tstep;
	}


	//----------------------------------------------------------------------------------------------
	// Inner classes for push, prepare and complete implementing the Callable interface
	//----------------------------------------------------------------------------------------------


	private class Push extends ThreadWork {
		public Push(int threadIdx, int numOfThreads, int numOfItems) {
			super(threadIdx, numOfThreads, numOfItems);
		}

		@Override
		public void doWork(int pIdx) {
			solver.step(particles.get(pIdx), force, timeStep);
			boundaries.applyOnParticleCenter(particles.get(pIdx));
		}
	}


	private class Complete extends ThreadWork {
		public Complete(int threadIdx, int numOfThreads, int numOfItems) {
			super(threadIdx, numOfThreads, numOfItems);
		}

		@Override
		public void doWork(int pIdx) {
			solver.complete(particles.get(pIdx), force, timeStep);
		}
	}


	private class Prepare extends ThreadWork {
		public Prepare(int threadIdx, int numOfThreads, int numOfItems) {
			super(threadIdx, numOfThreads, numOfItems);
		}

		@Override
		public void doWork(int pIdx) {
			solver.prepare(particles.get(pIdx), force, timeStep);
		}
	}
}
