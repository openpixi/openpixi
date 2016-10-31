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
import org.openpixi.pixi.physics.initial.IInitialCondition;
import org.openpixi.pixi.physics.movement.ParticleMover;
import org.openpixi.pixi.physics.movement.boundary.AbsorbingParticleBoundaryConditions;
import org.openpixi.pixi.physics.movement.boundary.IParticleBoundaryConditions;
import org.openpixi.pixi.physics.movement.boundary.PeriodicParticleBoundaryConditions;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.physics.util.PerformanceTimer;

import java.util.ArrayList;

public class Simulation {


	/**
	 * Type of the simulation (pure temporal/lorenz Yang-Mills, CGC, boost-invariant CGC, temporal CPIC, ..)
	 */
	private SimulationType simulationType;

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
	 * Number of threads.
     */
	public int numberOfThreads;

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

	private PerformanceTimer timer;


	public SimulationType getSimulationType() {
		return simulationType;
	}

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

		simulationType = settings.getSimulationType();
		tstep = settings.getTimeStep();

		this.simulationBoxSize = new double[settings.getNumberOfDimensions()];
		for(int i = 0; i < settings.getNumberOfDimensions(); i++) {
			this.simulationBoxSize[i] = settings.getGridStep(i) * settings.getGridCells(i);
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

		IParticleBoundaryConditions particleBoundaryConditions;
		switch (settings.getBoundaryType())
		{
			case Periodic:
				particleBoundaryConditions = new PeriodicParticleBoundaryConditions(this);
				break;
			case Absorbing:
				particleBoundaryConditions = new AbsorbingParticleBoundaryConditions(this);
				break;
			default:
				particleBoundaryConditions = new PeriodicParticleBoundaryConditions(this);
				break;
		}

		mover = new ParticleMover(
				settings.getParticleSolver(),
				particleBoundaryConditions,
				settings.getParticleIterator());

		numberOfThreads = settings.getNumOfThreads();

		grid = new Grid(settings);
		if (settings.useGrid()) {
			turnGridForceOn();
		} else {
			turnGridForceOff();
		}
		grid.setSimulationSteps(totalSimulationSteps);

		// Regions
		if(settings.isEvaluationRegionEnabled()) {
			grid.setEvaluationRegion(settings.getEvaluationRegionPoint1(), settings.getEvaluationRegionPoint2());
		}

		if(settings.isActiveRegionEnabled()) {
			grid.setActiveRegion(settings.getActiveRegionPoint1(), settings.getActiveRegionPoint2());
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
		// Initialize external currents on the grid
		for (ICurrentGenerator c: currentGenerators) {
			c.initializeCurrent(this, currentGenerators.size());
		}

		for(IInitialCondition ic : settings.getInitialConditions()) {
			ic.applyInitialCondition(this);
		}

		initialize();

		timer = new PerformanceTimer();
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
	 * Initialization step.
	 * 1) Update links from U(-dt/2) to U(dt/2) using E(0)
	 * 2) Interpolate rho(0) using particle positions x(0) and charges Q(0)
	 * 3) Update velocities from v(-dt/2) using E(0), U(-dt/2), U(dt/2) to v(dt/2)
	 * 4) Update velocities from x(0) to x(dt) using v(dt/2)
	 * 5) Interpolate fields to particles at x(0)
	 * 6) Update charges Q(0) to Q(dt) using parallel transport from last step.
	 * 7) Interpolate current j(dt/2) using x(0), x(dt), v(dt/2) and Q(0), Q(dt).
	 * 9) Apply external currents and charge densities to rho(0), j(dt/2).
	 *
	 */
	public void initialize() {
		/*
		 * In order to read out the initial state without specifying the Unext(t = at/2) links by hand we calculate them
		 * according to the equations of motion from the electric fields at t = 0 and gauge links U(t = -at/2).
		 * We also compute both internal and external currents at t = -at/2 from the given particle velocities
		 * (specified also at t = -at/2) and determine new velocities at t = at/2.
		 */
		grid.updateLinks(tstep);

		// Interpolate charge density
		grid.resetCharge();
		interpolation.interpolateChargedensity(particles, grid);

		// Update particle velocities
		//updateVelocities();

		// Update particle positions and charges (without reassigning values)
		mover.updatePositions(particles, f, grid, tstep);

		// Interpolate fields to particles
		interpolation.interpolateToParticle(particles, grid);

		// Update charges
		mover.updateCharges(particles, f, grid, tstep);

		// Interpolate currents
		grid.resetCurrent();
		interpolation.interpolateToGrid(particles, grid);

		// Generate external currents on the grid
		for (ICurrentGenerator c: currentGenerators)
		{
			c.applyCurrent(this);
		}
	}

	/**
	 * Runs the simulation in steps. (for interactive simulations)
	 * The algorithm goes as follows:
	 * 1) Initialize and run diagnostics if first simulation step, i.e. t == 0.
	 * 2) Increase simulation time variable from t to t+dt.
	 * 3) Reassign particle positions, charges and gauge links.
	 *    Particle position and charge and now refer to quantities at t+dt.
	 *    U refers to U(t+dt/2), Unext to U(t+3d/2).
	 * 4) Compute E(t+dt) from E(t), U(t+dt/2) and j(t+dt/2).
	 * 5) Compute U(t+3dt/2) using E(t+dt) and U(t+dt/2).
	 * 6) Interpolate charge density rho(t+dt) using particle position x(t+dt) and charge Q(t+dt).
	 * 7) Update particle velocities v(t+dt/2) using E(t+dt), and U(t+dt/2), U(t+3dt/2) to v(t+3dt/2).
	 * 8) Update particle positions x(t+dt) using particle velocity from last step to x(t+2dt).
	 * 9) Interpolate fields (E, parallel transport) to particle positions x(t+dt) [and x(t+2dt) in case of parallel transport].
	 * 10) Update particle charges Q(t+dt) using parallel transport from last step (applies to non-abelian simulations) to Q(t+2dt).
	 * 11) Interpolate current j(t+3dt/2) using particle positions [x(t+dt) and x(t+2dt)] velocities [v(t+3dt/2)] charges [Q(t+dt) and Q(t+2dt)].
	 * 12) Apply external currents and charge densities to j(t+3dt/2) and rho(t+dt).
	 * 13) Run diagnostics at t+dt.
	 */
	public void step() throws IOException {

		// 1) Initialize and run diagnostics before first simulation step.
		if(totalSimulationSteps == 0) {
			for (int i = 0; i< diagnostics.size(); i++) {	//Attention! Size of the diagnostics may change during the initialization!!
				diagnostics.get(i).initialize(this);
			}
			runDiagnostics();
		}

		// 2) Step counter
		totalSimulationSteps++;
		totalSimulationTime =  totalSimulationSteps * tstep;
		grid.setSimulationSteps(totalSimulationSteps);

		// 3) Reassign particle charges, positions and gauge links
		mover.reassign(particles);
		grid.storeFields();

		timer.reset();
		// 4) Compute electric fields from links and currents
		// 5) Update links
		grid.updateGrid(tstep);
		timer.lap("EOM");

		// 6) Interpolate charge density
		grid.resetCharge();
		interpolation.interpolateChargedensity(particles, grid);
		timer.lap("CIN");

		// 7) Update particle velocities
		//updateVelocities();

		// 8) Update particle positions
		mover.updatePositions(particles, f, grid, tstep);
		timer.lap("PUP");

		// 9) Interpolate fields to particles
		interpolation.interpolateToParticle(particles, grid);
		timer.lap("PIN");

		// 10) Update particle charges
		mover.updateCharges(particles, f, grid, tstep);
		timer.lap("CUP");

		// 11) Interpolate currents
		grid.resetCurrent();
		interpolation.interpolateToGrid(particles, grid);
		timer.lap("JIN");

		// 12) Generate external currents on the grid
		for (ICurrentGenerator c: currentGenerators)
		{
			c.applyCurrent(this);
		}

		// 13) Run diagnostics.

		timer.reset();
		runDiagnostics();
		timer.lap("DIA");

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
	public void run() throws IOException {
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

	/**
	 * Return list of diagnostics objects.
	 * @return list of diagnostics objects
	 */
	public ArrayList<Diagnostics> getDiagnosticsList() {
		return diagnostics;
	}

	/*
	Not used right now.

	public void prepareAllParticles() {
		mover.prepare(particles, f, tstep);
	}

	public void completeAllParticles() {
		mover.complete(particles, f, tstep);
	}
	*/
}