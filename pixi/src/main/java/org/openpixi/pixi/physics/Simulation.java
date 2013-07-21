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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
import org.openpixi.pixi.physics.particles.Particle;
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
	public void step() throws FileNotFoundException {
                PrintWriter pw = new PrintWriter(new File("p.txt"));
                
		particlePush();
                
//                System.out.println(tstep);
                
                

		detector.run();
		collisionalgorithm.collide(detector.getOverlappedPairs(), f, mover.getSolver(), tstep);

		interpolation.interpolateToGrid(particles, grid, tstep);
                
////                pw.write("_______####################_________________\n");
//                for (int i = 0; i < grid.getNumCellsXTotal(); i++) {
//                    for(int j = 0; j < grid.getNumCellsYTotal(); j++){
//                        pw.write("1 = " + grid.getCell(i-2, j-2).getJx() + "\n");
//                        pw.write("2 = " + grid.getCell(i-2, j-2).getJy() + "\n");
//                        pw.write("3 = " + grid.getCell(i-2, j-2).getRho() + "\n");
//                        pw.write("4 = " + grid.getCell(i-2, j-2).getPhi() + "\n");
//                        pw.write("5 = " + grid.getCell(i-2, j-2).getEx() + "\n");
//                        pw.write("6 = " + grid.getCell(i-2, j-2).getEy() + "\n");
//                        pw.write("7 = " + grid.getCell(i-2, j-2).getBz() + "\n");
//                        pw.write("8 = " + grid.getCell(i-2, j-2).getBzo() + "\n\n");
//                        
//                    }
//                }
////                pw.write("_______####################_________________\n");
//                for (int i = 0; i < grid.getNumCellsXTotal(); i++) {
//                    for (int j = 0; j < grid.getNumCellsYTotal(); j++) {
//                        pw.write(grid.cells[i][j].getJx() + "\n");
//                    }
//                }
//                System.out.println("_______####################_________________\n");
//                for (int i = 0; i < grid.getNumCellsX(); i++) {
//                    for(int j = 0; j < grid.getNumCellsY(); j++){
//                        System.out.println("Jx = " + grid.getCell(i, j).getJx());
//                        System.out.println("Jy = " + grid.getCell(i, j).getJy());
//                    }
//                }
//                System.out.println("---############------------------------------\n");
//                
                grid.updateGrid(tstep);
		interpolation.interpolateToParticle(particles, grid);
                for(int i = 0; i < particles.size(); i++){
                    pw.write(particles.get(i).getX() + "\n");
                    pw.write(particles.get(i).getY() + "\n");
                    pw.write(particles.get(i).getRadius()+ "\n");
                    pw.write(particles.get(i).getVx() + "\n");
                    pw.write(particles.get(i).getVy() + "\n");
                    pw.write(particles.get(i).getAx() + "\n");
                    pw.write(particles.get(i).getAy() + "\n");
                    pw.write(particles.get(i).getMass() + "\n");
                    pw.write(particles.get(i).getCharge() + "\n");
                    pw.write(particles.get(i).getPrevX() + "\n");
                    pw.write(particles.get(i).getPrevY() + "\n");
                    pw.write(particles.get(i).getEx() + "\n");
                    pw.write(particles.get(i).getEy() + "\n");
                    pw.write(particles.get(i).getBz() + "\n");
                    pw.write(particles.get(i).getPrevPositionComponentForceX() + "\n");
                    pw.write(particles.get(i).getPrevPositionComponentForceY() + "\n");
                    pw.write(particles.get(i).getPrevTangentVelocityComponentOfForceX() + "\n");
                    pw.write(particles.get(i).getPrevTangentVelocityComponentOfForceY() + "\n");
                    pw.write(particles.get(i).getPrevNormalVelocityComponentOfForceX() + "\n");
                    pw.write(particles.get(i).getPrevNormalVelocityComponentOfForceY() + "\n");
                    pw.write(particles.get(i).getPrevBz() + "\n");
                    pw.write(particles.get(i).getPrevLinearDragCoefficient() + "\n");
                }
                pw.close();
                
	}


	/**
	 * Runs the entire simulation at once.
	 * (for non-interactive simulations)
	 */
	public void run() throws FileNotFoundException {
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
