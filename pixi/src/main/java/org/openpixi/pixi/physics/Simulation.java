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
import org.openpixi.pixi.physics.boundary.*;
import org.openpixi.pixi.physics.collision.*;
import org.openpixi.pixi.physics.collision.algorithms.CollisionAlgorithm;
import org.openpixi.pixi.physics.collision.detectors.Detector;
import org.openpixi.pixi.physics.fields.*;
import org.openpixi.pixi.physics.force.*;
import org.openpixi.pixi.physics.grid.*;
import org.openpixi.pixi.physics.solver.*;

public class Simulation {
	
	/**Timestep*/
	public double tstep;
	/**Width of simulated area*/
	public double width;
	/**Height of simulated area*/
	public double  height;
	/**Speed of light*/
	public double c;

	/**Contains all Particle2D objects*/
	public ArrayList<Particle2D> particles;
	public CombinedForce f;
	public Boundary boundary;
	/**Solver for the particle equations of motion*/
	public Solver psolver;
	/**Grid for dynamic field calculation*/
	public Grid grid;
	public Detector detector;
	public CollisionAlgorithm collisionalgorithm;
	public boolean collisionBoolean = false;

	/**Creates a basic simulation and initializes all 
	 * necessary variables. All solvers are set to their
	 * empty versions.
	 * This constructor should be called from a factory class
	 */
	Simulation () {
	
		tstep = 0;
		width = 0;
		height = 0;
		
		particles = new ArrayList<Particle2D>(0);
		f = new CombinedForce();		
		
		psolver = new EmptySolver();
		grid = new Grid(this);
		boundary = new Boundary(this);
		detector = new Detector();
		collisionalgorithm = new CollisionAlgorithm();

	}
	
	public void step() {
		particlePush();
		if(collisionBoolean) {
			detector.run();
			collisionalgorithm.collide(detector.getOverlappedPairs(), f, psolver, tstep);
		}
		grid.updateGrid(particles);
	}
	
	public void setSize(double width, double height) {
		this.width = width;
		this.height = height;
		this.boundary.setBoundaries(0, 0, width, height);
		this.grid.setGrid(width, height);
	}
	
	public void particlePush() {
		for (Particle2D p : particles) {
			psolver.step(p, f, tstep);
			boundary.check(p, f, psolver, tstep);
		}		
	}
	
	public void prepareAllParticles() {
		for (Particle2D p : particles) {
			psolver.prepare(p, f, tstep);
		}
	}

	public void completeAllParticles() {
		for (Particle2D p : particles) {
			psolver.complete(p, f, tstep);
		}
	}

}
