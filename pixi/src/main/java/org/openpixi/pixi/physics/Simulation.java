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
import org.openpixi.pixi.physics.grid.Interpolator;
import org.openpixi.pixi.physics.movement.LocalParticleMover;
import org.openpixi.pixi.physics.util.DoubleBox;

import java.util.ArrayList;

public class Simulation {

	/**Timestep*/
	public double tstep;
	/**Width of simulated area*/
	private double width;
	/**Height of simulated area*/
	private double  height;
	/**Speed of light*/
	public double c;

	/**Contains all Particle2D objects*/
	public ArrayList<Particle> particles;
	public CombinedForce f;
	public LocalParticleMover mover;
	/**Grid for dynamic field calculation*/
	public Grid grid;
	public Detector detector;
	public CollisionAlgorithm collisionalgorithm;
	public boolean collisionBoolean = false;

	private ParticleGridInitializer particleGridInitializer = new ParticleGridInitializer();

	/**interpolation algorithm for current, charge density and force calculation*/
	private Interpolator interpolator;

	/**solver for the electrostatic poisson equation*/
	private PoissonSolver poisolver;

	public void setInterpolator(Interpolator interpolator) {
		this.interpolator = interpolator;
	}

	public Interpolator getInterpolator() {
		return interpolator;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
		particleGridInitializer.initialize(interpolator, poisolver, particles, grid);
	}


	public Simulation(Settings settings) {
		tstep = settings.getTimeStep();
		width = settings.getSimulationWidth();
		height = settings.getSimulationHeight();

		// TODO make particles a generic list
		particles = (ArrayList<Particle>)settings.getParticles();
		f = settings.getForce();

		mover = new LocalParticleMover(
				settings.getParticleSolver(),
				new DoubleBox(0, width, 0, height),
				settings.getParticleBoundary());

		SimpleGridForce gridForce = new SimpleGridForce();
		f.add(gridForce);
		poisolver = settings.getPoissonSolver();
		grid = new Grid(settings);
		interpolator = settings.getInterpolator();

		particleGridInitializer.initialize(interpolator, poisolver, particles, grid);

		detector = settings.getCollisionDetector();
		collisionalgorithm = settings.getCollisionAlgorithm();
	}


	public void step() {
		particlePush();
		if(collisionBoolean) {
			detector.run();
			collisionalgorithm.collide(detector.getOverlappedPairs(), f, mover.psolver, tstep);
		}
		interpolator.interpolateToGrid(particles, grid, tstep);
		grid.updateGrid(tstep);
		interpolator.interpolateToParticle(particles, grid);
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
