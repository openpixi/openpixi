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

import java.util.ArrayList;

import org.openpixi.pixi.physics.collision.algorithms.CollisionAlgorithm;
import org.openpixi.pixi.physics.collision.detectors.Detector;
import org.openpixi.pixi.physics.force.CombinedForce;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.GridFactory;
import org.openpixi.pixi.physics.movement.BoundingBox;
import org.openpixi.pixi.physics.movement.LocalParticleMover;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaryType;
import org.openpixi.pixi.physics.solver.EmptySolver;

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

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
		resize(width, height);
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
		resize(width, height);
	}

	/**Creates a basic simulation and initializes all
	 * necessary variables. All solvers are set to their
	 * empty versions.
	 * This constructor should be called from a factory class
	 */
	Simulation () {

		tstep = 0;
		width = 100;
		height = 100;

		particles = new ArrayList<Particle>(0);
		f = new CombinedForce();

		mover = new LocalParticleMover(
				new EmptySolver(),
				new BoundingBox(0, width, 0, height),
				ParticleBoundaryType.Periodic);
		grid = GridFactory.createSimpleGrid(this, 10, 10, width, height);
		detector = new Detector();
		collisionalgorithm = new CollisionAlgorithm();

	}

	/**
	 * When the simulation is resized we also need to resize:
	 * - particle boundaries
	 * - grid -> TODO
	 */
	public void resize(double width, double height) {
		this.width = width;
		this.height = height;
		mover.resizeBoundaries(new BoundingBox(0,width,0,height));
	}

	public void step() {
		particlePush();
		if(collisionBoolean) {
			detector.run();
			collisionalgorithm.collide(detector.getOverlappedPairs(), f, mover.psolver, tstep);
		}
		grid.updateGrid(particles, tstep);
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
