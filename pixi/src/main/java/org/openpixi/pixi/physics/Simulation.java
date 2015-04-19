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
import java.io.IOException;

import org.openpixi.pixi.physics.fields.PoissonSolver;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.force.CombinedForce;
import org.openpixi.pixi.physics.force.SimpleGridForce;
import org.openpixi.pixi.physics.force.relativistic.SimpleGridForceRelativistic;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.Interpolation;
import org.openpixi.pixi.physics.grid.LocalInterpolation;
import org.openpixi.pixi.physics.movement.ParticleMover;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaries;
import org.openpixi.pixi.physics.movement.boundary.SimpleParticleBoundaries;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.util.DoubleBox;

import java.util.ArrayList;
import java.util.List;

public class Simulation {

	/**
	 * Timestep
	 */
	public double tstep;
	/**
	 * Width of simulated area
	 */
	private double width;
	/**
	 * Height of simulated area
	 */
	private double height;
	/**
	 * Depth of simulated area
	 */
	private double depth;
	private double speedOfLight;
	private double eps0;
	private double mu0;
	/**
	 * Number of iterations in the non-interactive simulation.
	 */
	private int iterations;
	/**
	 * Total number of steps simulated so far.
	 */
	public int tottime;
	/**
	 * Total number of steps between spectral measurements.
	 */
	public int specstep;
	/**
	 * File path to output files.
	 */
	private String filePath;
	/**
	 * Contains all Particle2D objects
	 */
	public ArrayList<IParticle> particles;
	public CombinedForce f;
	private ParticleMover mover;
	/**
	 * Grid for dynamic field calculation
	 */
	public Grid grid;
	/**
	 * We can turn on or off the effect of the grid on particles by adding or
	 * removing this force from the total force.
	 */
	//private SimpleGridForce gridForce = new SimpleGridForce();
	private Force gridForce;
	private boolean usingGridForce = false;
	public boolean relativistic = false;
	private ParticleGridInitializer particleGridInitializer = new ParticleGridInitializer();
	private Interpolation interpolation;
	/**
	 * solver for the electrostatic poisson equation
	 */
	private PoissonSolver poisolver;

	public Interpolation getInterpolation() {
		return interpolation;
	}
	
	public int getIterations() {
		return iterations;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}
	
	public double getDepth() {
		return depth;
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
		depth = settings.getSimulationDepth();
		speedOfLight = settings.getSpeedOfLight();
		iterations = settings.getIterations();
		tottime = 0;
		specstep = settings.getSpectrumStep();
		filePath = settings.getFilePath();
		relativistic = settings.getRelativistic();
		eps0 = settings.getEps0();
		mu0 = settings.getMu0();

		// TODO make particles a generic list
		particles = (ArrayList<IParticle>) settings.getParticles();
		f = settings.getForce();

		ParticleBoundaries particleBoundaries = new SimpleParticleBoundaries(
				new DoubleBox(0, width, 0, height),								//Has to be changed!!!
				settings.getParticleBoundary());
		mover = new ParticleMover(
				settings.getParticleSolver(),
				particleBoundaries,
				settings.getParticleIterator());

		grid = new Grid(settings);
		if (settings.useGrid()) {
			turnGridForceOn();
		} else {
			turnGridForceOff();
		}

		poisolver = settings.getPoissonSolver();
		interpolation = new LocalInterpolation(
				settings.getInterpolator(), settings.getParticleIterator());
		particleGridInitializer.initialize(interpolation, poisolver, particles, grid);

		prepareAllParticles();
		
		clearFile();
	}

	/**
	 * Constructor for distributed simulation. Expects settings specific to the
	 * local node => the simulation width and height as well as the number of
	 * cells in y and x direction must pertain to local simulation not to the
	 * global simulation. (No need to set poison solver and run
	 * ParticleGridInitializer as it was already run on the master node).
	 */
	public Simulation(Settings settings,
			Grid grid,
			List<IParticle> particles,
			ParticleBoundaries particleBoundaries,
			Interpolation interpolation) {

		this.tstep = settings.getTimeStep();
		this.width = settings.getSimulationWidth();
		this.height = settings.getSimulationHeight();
		this.depth = settings.getSimulationDepth();
		this.speedOfLight = settings.getSpeedOfLight();
		this.iterations = settings.getIterations();
		this.tottime = 0;
		this.specstep = settings.getSpectrumStep();
		this.filePath = settings.getFilePath();
		this.relativistic = settings.getRelativistic();
		this.eps0 = settings.getEps0();
		this.mu0 = settings.getMu0();

		this.particles = (ArrayList<IParticle>) particles;
		f = settings.getForce();

		mover = new ParticleMover(
				settings.getParticleSolver(),
				particleBoundaries,
				settings.getParticleIterator());

		this.grid = grid;
		if (settings.useGrid()) {
			turnGridForceOn();
		} else {
			turnGridForceOff();
		}

		this.interpolation = interpolation;

		prepareAllParticles();
		
		clearFile();
	}

	public void turnGridForceOn() {
		if (!usingGridForce) {
			if(relativistic == true) {
				gridForce = new SimpleGridForceRelativistic(speedOfLight);
			} else {
				gridForce = new SimpleGridForce();
			}
			f.add(gridForce);
			usingGridForce = true;
		}
        if(!f.forces.contains(gridForce)){
            f.add(gridForce);
        }
	}

	public void turnGridForceOff() {
		if (usingGridForce) {
			f.remove(gridForce);
			usingGridForce = false;
		}
	}

	/**
	 * Runs the simulation in steps. (for interactive simulations)
	 */
	public void step() throws FileNotFoundException,IOException {

		interpolation.interpolateToParticle(particles, grid);
		particlePush();
		interpolation.interpolateToGrid(particles, grid, tstep);
		grid.updateGrid(tstep);

		tottime++;
	}

	/**
	 * Whether the simulation should continue.
	 * @return
	 */
	public boolean continues() {
		return tottime <= iterations;
	}

	/**
	 * Runs the entire simulation at once. (for non-interactive simulations)
	 */
	public void run() throws FileNotFoundException,IOException {
		while (continues()) {
			step();
		}
	}

	/**
	 * Checks if the files are already existent and deletes them.
	 */
	public void clearFile() {
		File particlesfile = getOutputFile("particles_seq.txt");
		boolean fileExists1 = particlesfile.exists();
		if(fileExists1 == true) {
			particlesfile.delete();
		}

		File gridfile = getOutputFile("cells_seq.txt");
		boolean fileExists2 = gridfile.exists();
		if(fileExists2 == true) {
			gridfile.delete();
		}
	}

	/**
	 * Get output file in correct subdirectory.
	 * Create subdirectories if necessary.
	 * @param filename
	 * @return file
	 */
	public File getOutputFile(String filename) {
		// Default output path is
		// 'output/' + filePath + '/' + filename
		File fullpath = new File("output");
		if(!fullpath.exists()) fullpath.mkdir();

		fullpath = new File(fullpath, filePath);
		if(!fullpath.exists()) fullpath.mkdir();

		return new File(fullpath, filename);
	}
	
	public void particlePush() {
		mover.push(particles, f, grid, tstep);
	}

	public void prepareAllParticles() {
		mover.prepare(particles, f, tstep);
	}

	public void completeAllParticles() {
		mover.complete(particles, f, tstep);
	}
}
