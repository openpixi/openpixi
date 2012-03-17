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
import org.openpixi.pixi.physics.collision.Algorithms.*;
import org.openpixi.pixi.physics.collision.detectors.*;
import org.openpixi.pixi.physics.fields.FieldSolver;
import org.openpixi.pixi.physics.fields.SimpleSolver;
import org.openpixi.pixi.physics.force.Force;
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
	public CurrentGrid currentGrid;
	public Collision collision;
	public Detector detector;
	public CollisionAlgorithm algorithm;

	public Simulation () {
	
		tstep = 1;
		width = 100;
		height = 100;

		/**Contains all Particle2D objects*/
		particles = new ArrayList<Particle2D>(0);
		f= new Force();
		boundary = new HardWallBoundary();
		
		psolver = new Boris();
		fsolver = new SimpleSolver();
		currentGrid = new CurrentGrid(this);
		detector = new Detector();
		algorithm = new TransformationMatrix();
		collision = new Collision(detector, algorithm);
		
		//collision = new ElasticCollisionSweepPrune();
	}
	
	public void addParticleList(ArrayList<Particle2D> plist)
	{
		particles = plist;
		detector.add(particles);
	}
	
	public void setSize(double width, double height) {
		this.width = width;
		this.height = height;
		this.boundary.setBoundaries(0, 0, width, height);
		this.currentGrid.setGrid(width, height);
	}

	public void step() {
		ParticleMover.particlePush(this);
		collision.check(particles, f, psolver, tstep);
		currentGrid.updateGrid(particles);
	}

}
