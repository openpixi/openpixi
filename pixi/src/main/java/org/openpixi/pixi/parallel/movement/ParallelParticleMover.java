package org.openpixi.pixi.parallel.movement;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.movement.ParticleMover;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.solver.Solver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 *  Multi-threaded particle mover.
 *
 *  TODO create interactive version.
 *  This version is non-interactive.
 *  That means, it can not be used under a situation
 *  where the force or time step change during the simulation.
 *  In order to be able to create an interactive version one first needs to encapsulate
 *  the setting of force and time step in the simulation class, so that one can forward
 *  the request for change to the particle movement and further to the inner classes
 *  Push, Prepare and Complete.
 */
public class ParallelParticleMover extends ParticleMover {

	private Force force;
	private List<Particle> particles;
	private double timeStep;

	private ExecutorService threadsExecutor;
	private int numOfThreads;

	private List<ParticleAction> pushTasks = new ArrayList<ParticleAction>();
	private List<ParticleAction> prepareTasks = new ArrayList<ParticleAction>();
	private List<ParticleAction> completeTasks = new ArrayList<ParticleAction>();


	public ParallelParticleMover(
			Solver psolver, ParticleBoundaries boundaries,
			Force force, List<Particle> particles,
			ExecutorService threadsExecutor,
			double timeStep, int numOfThreads) {
		super(psolver, boundaries);

		this.force = force;
		this.particles = particles;
		this.threadsExecutor = threadsExecutor;
		this.timeStep = timeStep;
		this.numOfThreads = numOfThreads;

		for (int i = 0; i < numOfThreads; i++) {
			Push push = new Push(i);
			pushTasks.add(push);

			Prepare prepare = new Prepare(i);
			prepareTasks.add(prepare);

			Complete complete = new Complete(i);
			completeTasks.add(complete);
		}
	}


	@Override
	public void push(List<Particle> particles, Force force, double tstep) {
		try {
			threadsExecutor.invokeAll(pushTasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void prepare(List<Particle> particles, Force force, double tstep) {
		try {
			threadsExecutor.invokeAll(prepareTasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void complete(List<Particle> particles, Force force, double tstep) {
		try {
			threadsExecutor.invokeAll(completeTasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	//----------------------------------------------------------------------------------------------
	// Inner classes for push, prepare and complete implementing the Callable interface
	//----------------------------------------------------------------------------------------------

	/**
	 * Implements the assignment of different particles to different threads.
	 *
	 * Why do we use Callable interface instead of Runnable?
	 * Because we want to use the invokeAll() method of ExecutorService
	 * to push the particles in parallel.
	 *
	 * Why do we want to use the invokeAll() method and not the execute() or submit() method?
	 * Because execute() and submit() do not wait for the tasks to finish.
	 */
	private abstract class ParticleAction implements Callable<Object> {

		private int threadIdx;

		public ParticleAction(int threadIdx) {
			this.threadIdx = threadIdx;
		}

		public Object call() {
			for (int pIdx = threadIdx; pIdx < particles.size(); pIdx += numOfThreads) {
				particleAction(pIdx);
			}
			return null;
		}

		/**
		 * This is a template method and needs to be overriden by the children of this class.
		 * Performs a specific action on a particle with given index.
		 */
		public abstract void particleAction(int pIdx);
	}


	private class Push extends ParticleAction {
		public Push(int threadIdx) {
			super(threadIdx);
		}

		@Override
		public void particleAction(int pIdx) {
			solver.step(particles.get(pIdx), force, timeStep);
			boundaries.applyOnParticleCenter(particles.get(pIdx));
		}
	}


	private class Complete extends ParticleAction {
		public Complete(int threadIdx) {
			super(threadIdx);
		}

		@Override
		public void particleAction(int pIdx) {
			solver.complete(particles.get(pIdx), force, timeStep);
		}
	}


	private class Prepare extends ParticleAction {
		public Prepare(int threadIdx) {
			super(threadIdx);
		}

		@Override
		public void particleAction(int pIdx) {
			solver.prepare(particles.get(pIdx), force, timeStep);
		}
	}
}
