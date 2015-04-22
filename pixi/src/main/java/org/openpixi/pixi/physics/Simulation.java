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
import org.openpixi.pixi.physics.movement.boundary.IParticleBoundaryConditions;
import org.openpixi.pixi.physics.movement.boundary.PeriodicParticleBoundaryConditions;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.util.DoubleBox;

import java.util.ArrayList;

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


	private int numberOfColors;
	private int numberOfDimensions;
    private double couplingConstant;

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
	public int getNumberOfColors() {
		return numberOfColors;
	}
	public int getNumberOfDimensions() {
		return numberOfDimensions;
	}
    public double getCouplingConstant() {
        return couplingConstant;
    }

    public ParticleMover getParticleMover()
    {
        return  mover;
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
		numberOfColors = settings.getNumberOfColors();
		numberOfDimensions = settings.getNumberOfDimensions();
        couplingConstant = settings.getCouplingConstant();

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

		DoubleBox simulationBox = new DoubleBox(numberOfDimensions, new double[] {0, 0, 0}, new double[] {width, height, depth});
		IParticleBoundaryConditions particleBoundaryConditions;
		switch (settings.getBoundaryType())
		{
			case Periodic:
				particleBoundaryConditions = new PeriodicParticleBoundaryConditions(simulationBox, numberOfDimensions);
				break;
			default:
				particleBoundaryConditions = new PeriodicParticleBoundaryConditions(simulationBox, numberOfDimensions);
				break;

		}

		mover = new ParticleMover(
				settings.getParticleSolver(),
				particleBoundaryConditions,
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

	public void turnGridForceOn() {
		if (!usingGridForce) {
			if(relativistic == true) {
				gridForce = new SimpleGridForceRelativistic(this);
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
