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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.openpixi.pixi.physics.fields.fieldgenerators.IFieldGenerator;
import org.openpixi.pixi.physics.fields.currentgenerators.ICurrentGenerator;
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
import org.openpixi.pixi.diagnostics.Diagnostics;

import java.util.ArrayList;

public class Simulation {

	/**
	 * Timestep
	 */
	public double tstep;

	/**
	 * Size of the simulation box
	 */
	private double[] simulationBoxSize;


	private int numberOfColors;
	private int numberOfDimensions;
    private double couplingConstant;
	private double speedOfLight;
	/**
	 * Number of iterations in the non-interactive simulation.
	 */
	private int iterations;

	/**
	 * Total number of steps simulated so far.
	 */
	public int totalSimulationSteps;

	/**
	 * Simulation running time so far.
	 */
	public double totalSimulationTime;

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

    /**
     * List of field generators which are applied when the simulation starts.
     */
    private ArrayList<IFieldGenerator>  fieldGenerators;
    
    /**
     * List of output file generators which are applied during the runtime of the simulation.
     */
    private ArrayList<Diagnostics>  diagnostics;

	/**
	 * List of external current generators which are applied during the whole runtime of the simulation.
	 */
	private ArrayList<ICurrentGenerator>  currentGenerators;

	public Interpolation getInterpolation() {
		return interpolation;
	}
	
	public int getIterations() {
		return iterations;
	}

	@Deprecated
	public double getWidth() {
		return simulationBoxSize[0];
	}

	@Deprecated
	public double getHeight() {
		return simulationBoxSize[1];
	}

	@Deprecated
	public double getDepth() {
		return simulationBoxSize[2];
	}

	public double[] getSimulationBoxSize() { return simulationBoxSize; }

	public double getSimulationBoxSize(int i) { return simulationBoxSize[i]; }

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
    public double getTimeStep() {
        return tstep;
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

		this.simulationBoxSize = new double[settings.getNumberOfDimensions()];
		for(int i = 0; i < settings.getNumberOfDimensions(); i++) {
			this.simulationBoxSize[i] = settings.getGridStep() * settings.getGridCells(i);
		}

		speedOfLight = settings.getSpeedOfLight();
		numberOfColors = settings.getNumberOfColors();
		numberOfDimensions = settings.getNumberOfDimensions();
        couplingConstant = settings.getCouplingConstant();

		iterations = settings.getIterations();

		totalSimulationTime = 0.0;
		totalSimulationSteps = 0;

		relativistic = settings.getRelativistic();

		// TODO make particles a generic list
		particles = (ArrayList<IParticle>) settings.getParticles();
		f = settings.getForce();

		diagnostics = settings.getDiagnostics();

		DoubleBox simulationBox = new DoubleBox(numberOfDimensions, new double[] {0, 0, 0},
				new double[] {this.getWidth(), this.getHeight(), this.getDepth()});
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

        // Cycle through field generators and apply field configurations to the Grid.
        fieldGenerators = settings.getFieldGenerators();
		for (IFieldGenerator f: fieldGenerators)
        {
            f.applyFieldConfiguration(this);
        }
		/*
			TODO After running through each field generator we should check if the intial state is consistent.
			(e.g. check if Gauss law is fulfilled.)
		 */

		// Copy current generators from Settings.
		currentGenerators = settings.getCurrentGenerators();
		// Generate external currents on the grid!!
		for (ICurrentGenerator c: currentGenerators) {
			c.initializeCurrent(this);
		}

		/**
		 * In order to read out the initial state without specifying the Unext(t = at/2) links by hand we calculate them
		 * according to the equations of motion from the electric fields at t = 0 and gauge links U(t = -at/2). We also
		 * compute both internal and external currents at t = -at/2 from the given particle velocities (specified also
		 * at t = -at/2) and determine new velocities at t = at/2.
		 */
		grid.updateLinks(tstep);

		interpolation.interpolateToParticle(particles, grid);

		interpolation.interpolateToGrid(particles, grid, tstep);

		//updateVelocities(); TODO: Write this method!!

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
	 * The algorithm goes as follows:
	 * 1) The link fields U(t - at/2) and Unext(t + at/2) are reassigned, such that U(t - at/2) can be overwritten
	 * with Unext(t + 3at/2).
	 * 2) Particle velocities are reassigned.
	 * 3) New currents at t = t + at/2 are generated from external ones and from new particle velocities
	 * at t = t + at/2.
	 * 4) Gauge links and electric fields are updated, Unext(t + 3at/2) and E(t + at) are dtermined from E(t),
	 * U(t + at/2) and J(t + at/2).
	 * 5) Particle positions at t = t + at are computed using their velocities at t = t + at/2.
	 * 6) Electric and magnetic field values are interpolated to particle positions.
	 * 7) Particle velocities at t + 3at/2 are determined using the interpolated fields at t = t + at.
	 * 8) Simulation time is increased by at.
	 * 9) Diagnostics routines are called in order to produce data output.
	 */
	public void step() throws FileNotFoundException,IOException {

		// Initialize and run diagnostics before first simulation step.
		if(totalSimulationSteps == 0) {
			for (Diagnostics d: diagnostics) {
				d.initialize(this);
			}
			runDiagnostics();
		}
		//Link and particle reassignment
		grid.storeFields();
		//reassignParticles(); TODO: Write this method!!

		//Generation of internal and external currents and charges
		grid.resetCurrent();
		grid.resetCharge();
		interpolation.interpolateToGrid(particles, grid, tstep);
		// Generate external currents on the grid!!
		for (ICurrentGenerator c: currentGenerators)
		{
			c.applyCurrent(this);
		}

		//Combined update of gauge links and fields
		grid.updateGrid(tstep);

		//Particle positions are updated using their velocities
		//updatePositions(); TODO: Write this method!!

		// Field values are interpolated to particle positions
		interpolation.interpolateToParticle(particles, grid);

		//Particle velocities are updated using the interpolated fields
		//updateVelocities(); TODO: Write this method!!

		// Step counter
		totalSimulationSteps++;
		totalSimulationTime =  totalSimulationSteps * tstep;

		//Output in text files
		runDiagnostics();
	}

	/**
	 * Whether the simulation should continue.
	 * @return
	 */
	public boolean continues() {
		return totalSimulationSteps <= iterations;
	}

	/**
	 * Runs the entire simulation at once. (for non-interactive simulations)
	 */
	public void run() throws FileNotFoundException,IOException {
		while (continues()) {
			step();
		}
	}
	
	public void runDiagnostics() throws IOException {

		for (int f = 0; f < diagnostics.size(); f++)
        {
			diagnostics.get(f).calculate(grid, particles, this.totalSimulationSteps);
        }
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