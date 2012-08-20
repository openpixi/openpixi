/*
 * OpenPixi - Open Particle-In-Cell (PIC) Simulator
 * Copyright (C) 2012  OpenPixi.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.collision.algorithms.CollisionAlgorithm;
import org.openpixi.pixi.physics.collision.detectors.Detector;
import org.openpixi.pixi.physics.fields.PoissonSolver;
import org.openpixi.pixi.physics.force.CombinedForce;
import org.openpixi.pixi.physics.force.SimpleGridForce;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.Interpolation;
import org.openpixi.pixi.physics.grid.LocalInterpolation;
import org.openpixi.pixi.physics.movement.ParticleMover;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.movement.boundary.SimpleParticleBoundaries;
import org.openpixi.pixi.physics.util.DoubleBox;

import java.util.ArrayList;
import java.util.List;

public class Simulation {

	/**Timestep*/
	public double tstep;
	/**Width of simulated area*/
	private double width;
	/**Height of simulated area*/
	private double  height;
	private double speedOfLight;

	/** Number of iterations in the non-interactive simulation. */
	private int iterations;

	/**Contains all Particle2D objects*/
	public ArrayList<Particle> particles;
	public CombinedForce f;
	private ParticleMover mover;
	/**Grid for dynamic field calculation*/
	public Grid grid;
	public Detector detector;
	public CollisionAlgorithm collisionalgorithm;
	public boolean collisionBoolean = false;

	/**
	 * We can turn on or off the effect of the grid on particles by
	 * adding or removing this force from the total force.
	 */
	private SimpleGridForce gridForce = new SimpleGridForce();
	private boolean usingGridForce = false;

	private ParticleGridInitializer particleGridInitializer = new ParticleGridInitializer();

	private Interpolation interpolation;

	/**solver for the electrostatic poisson equation*/
	private PoissonSolver poisolver;


	public Interpolation getInterpolation() {
		return interpolation;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public double getSpeedOfLight() {
		return speedOfLight;
	}

	public ParticleMover getParticleMover() {
		return mover;
	}


	/**
	 * Constructor for non distributed simulation.
	 */
	public Simulation(Settings settings) {
		tstep = settings.getTimeStep();
		width = settings.getSimulationWidth();
		height = settings.getSimulationHeight();
		speedOfLight = settings.getSpeedOfLight();
		iterations = settings.getIterations();

		// TODO make particles a generic list
		particles = (ArrayList<Particle>)settings.getParticles();
		f = settings.getForce();

		ParticleBoundaries particleBoundaries = new SimpleParticleBoundaries(
				new DoubleBox(0, width, 0, height),
				settings.getParticleBoundary());
		mover = new ParticleMover(
				settings.getParticleSolver(),
				particleBoundaries,
				settings.getParticleIterator());

		grid = new Grid(settings);
		if (settings.useGrid()) {
			turnGridForceOn();
		}
		else {
			turnGridForceOff();
		}

		poisolver = settings.getPoissonSolver();
		interpolation = new LocalInterpolation(
				settings.getInterpolator(), settings.getParticleIterator());
		particleGridInitializer.initialize(interpolation, poisolver, particles, grid);

		detector = settings.getCollisionDetector();
		collisionalgorithm = settings.getCollisionAlgorithm();

		prepareAllParticles();
	}


	/**
	 * Constructor for distributed simulation.
	 * Expects settings specific to the local node =>
	 * the simulation width and height as well as
	 * the number of cells in y and x direction must pertain to local simulation
	 * not to the global simulation.
	 * (No need to set poison solver and run ParticleGridInitializer as it was already run on the
	 * master node).
	 */
	public Simulation(Settings settings,
	                  Grid grid,
	                  List<Particle> particles,
	                  ParticleBoundaries particleBoundaries,
	                  Interpolation interpolation) {

		this.tstep = settings.getTimeStep();
		this.width = settings.getSimulationWidth();
		this.height = settings.getSimulationHeight();
		this.speedOfLight = settings.getSpeedOfLight();
		this.iterations = settings.getIterations();

		this.particles = (ArrayList<Particle>)particles;
		f = settings.getForce();

		mover = new ParticleMover(
				settings.getParticleSolver(),
				particleBoundaries,
				settings.getParticleIterator());

		this.grid = grid;
		if (settings.useGrid()) {
			turnGridForceOn();
		}
		else {
			turnGridForceOff();
		}

		this.interpolation = interpolation;

		detector = settings.getCollisionDetector();
		collisionalgorithm = settings.getCollisionAlgorithm();

		prepareAllParticles();
	}


	public void turnGridForceOn() {
		if (!usingGridForce) {
			f.add(gridForce);
			usingGridForce = true;
		}
	}


	public void turnGridForceOff() {
		if (usingGridForce) {
			f.remove(gridForce);
			usingGridForce = false;
		}
	}


	/**
	 * Runs the simulation in steps.
	 * (for interactive simulations)
	 */
	public void step() {
		particlePush();
		if(collisionBoolean) {
			detector.run();
			collisionalgorithm.collide(detector.getOverlappedPairs(), f, mover.getSolver(), tstep);
		}
		interpolation.interpolateToGrid(particles, grid, tstep);
		grid.updateGrid(tstep);
		interpolation.interpolateToParticle(particles, grid);
	}


	/**
	 * Runs the entire simulation at once.
	 * (for non-interactive simulations)
	 */
	public void run() {
		for (int i = 0; i < iterations; ++i) {
			step();
		}
	}


	public void particlePush() {
		mover.push(particles, f, tstep);
	}

	public void prepareAllParticles() {
		mover.prepare(particles, f, tstep);
	}

	public void completeAllParticles() {
		mover.complete(particles, f, tstep);
	}
}
