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

	/**Contains all Particle2D objects*/
	public ArrayList<Particle2D> particles;
	public Force  f;
	public Boundary boundary;
	
	public Solver psolver;
	public FieldSolver fsolver;
	public Grid grid;
	public Collision collision;
	//public Detector detector;
	//public CollisionAlgorithm algorithm;
	public boolean collisionBoolean = false;

	public Simulation (int swidth, int sheight, int pcount, double pradius) {
	
		tstep = 1;
		width = swidth;
		height = sheight;
		
		particles = new ArrayList<Particle2D>(0);
		f = new CombinedForce();		
		InitialConditions.initRandomParticles(this, pcount, pradius);
		
		psolver = new Boris();
		fsolver = new YeeSolver();
		grid = new Grid(this);
		//detector = new Detector();
		//algorithm = new CollisionAlgorithm();
		collision = new Collision();
		
		//collision = new ElasticCollisionSweepPrune();
		
		//should be placed at the beginning but that is impossible because of dependencies on boundaries and Grid
		setSize(width, height);
		
	}
	
	public void setSize(double width, double height) {
		this.width = width;
		this.height = height;
		this.boundary.setBoundaries(0, 0, width, height);
		this.grid.setGrid(width, height);
	}

	public void step() {
		ParticleMover.particlePush(this);
		if(collisionBoolean) {
			collision.check(particles, f, psolver, tstep);
		}
		grid.updateGrid(particles);
	}

}
