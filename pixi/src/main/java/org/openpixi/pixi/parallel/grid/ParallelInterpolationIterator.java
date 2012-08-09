package org.openpixi.pixi.parallel.grid;

import org.openpixi.pixi.parallel.ThreadWork;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.InterpolationIterator;
import org.openpixi.pixi.physics.grid.Interpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Uses threads when iterating the particles for interpolation.
 */
public class ParallelInterpolationIterator extends InterpolationIterator {

	private List<Particle> particles;
	private Grid grid;
	private double timeStep;

	private ExecutorService threadsExecutor;

	private List<ThreadWork> toGridTasks = new ArrayList<ThreadWork>();
	private List<ThreadWork> toParticleTasks = new ArrayList<ThreadWork>();
	private List<ThreadWork> chargeDensityTasks = new ArrayList<ThreadWork>();



	public ParallelInterpolationIterator(
			Interpolator interpolator, ExecutorService threadsExecutor, int numOfThreads) {
		super(interpolator);

		this.threadsExecutor = threadsExecutor;

		for (int i = 0; i < numOfThreads; ++i) {
			toGridTasks.add(new ToGrid(i, numOfThreads, 0));
			toParticleTasks.add(new ToParticle(i, numOfThreads, 0));
			chargeDensityTasks.add(new ChargeDensity(i, numOfThreads, 0));
		}
	}


	@Override
	public void interpolateToGrid(List<Particle> particles, Grid grid, double tstep) {
		// TODO reset current
		setFields(particles, grid, tstep);
		ThreadWork.setNumOfItems(particles.size(), toGridTasks);
		try {
			threadsExecutor.invokeAll(toGridTasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	@Override
	public void interpolateToParticle(List<Particle> particles, Grid grid) {
		setFields(particles, grid, 0);
		ThreadWork.setNumOfItems(particles.size(), toParticleTasks);
		try {
			threadsExecutor.invokeAll(toParticleTasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	@Override
	public void interpolateChargedensity(List<Particle> particles, Grid grid) {
		// TODO reset charge
		setFields(particles, grid, 0);
		ThreadWork.setNumOfItems(particles.size(), chargeDensityTasks);
		try {
			threadsExecutor.invokeAll(chargeDensityTasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	private void setFields(List<Particle> particles, Grid grid, double timeStep) {
		this.particles = particles;
		this.grid = grid;
		this.timeStep = timeStep;
	}


	//----------------------------------------------------------------------------------------------
	// Inner classes for interpolateToGrid, interpolateToParticle and interpolateChargeDensity
	//----------------------------------------------------------------------------------------------


	private class ToGrid extends ThreadWork {
		public ToGrid(int threadIdx, int numOfThreads, int numOfItems) {
			super(threadIdx, numOfThreads, numOfItems);
		}

		@Override
		public void doWork(int item) {
			interpolator.interpolateToGrid(particles.get(item), grid, timeStep);
		}
	}


	private class ToParticle extends ThreadWork {
		public ToParticle(int threadIdx, int numOfThreads, int numOfItems) {
			super(threadIdx, numOfThreads, numOfItems);
		}

		@Override
		public void doWork(int item) {
			interpolator.interpolateToParticle(particles.get(item), grid);
		}
	}


	private class ChargeDensity extends ThreadWork {
		public ChargeDensity(int threadIdx, int numOfThreads, int numOfItems) {
			super(threadIdx, numOfThreads, numOfItems);
		}

		@Override
		public void doWork(int item) {
			interpolator.interpolateChargedensity(particles.get(item), grid);
		}
	}

}
