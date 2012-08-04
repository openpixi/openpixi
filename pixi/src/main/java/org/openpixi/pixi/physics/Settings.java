package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.collision.algorithms.CollisionAlgorithm;
import org.openpixi.pixi.physics.collision.detectors.Detector;
import org.openpixi.pixi.physics.fields.FieldSolver;
import org.openpixi.pixi.physics.fields.PoissonSolver;
import org.openpixi.pixi.physics.fields.PoissonSolverFFTPeriodic;
import org.openpixi.pixi.physics.force.CombinedForce;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.grid.CloudInCell;
import org.openpixi.pixi.physics.grid.GridBoundaryType;
import org.openpixi.pixi.physics.grid.Interpolator;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaryType;
import org.openpixi.pixi.physics.solver.EmptySolver;
import org.openpixi.pixi.physics.solver.Solver;

import java.util.ArrayList;
import java.util.List;

/**
 * Specifies default values of simulation parameters.
 * The default values can be overridden with a settings file input.
 * The default values can be overridden with a command line input.
 * The default values can be overridden programatically.
 *
 * NOTICE ON USAGE:
 * To assure that the class is used in the intended way
 * THE SETTERS SHOULD BE CALLED RIGHT AFTER A PARAMETERLESS CONSTRUCTOR
 * AND BEFORE ANY OF THE MORE COMPLEX GETTERS IS CALLED !!!
 */
public class Settings {

	//----------------------------------------------------------------------------------------------
	// DEFAULT VALUES
	//----------------------------------------------------------------------------------------------

	private double simulationWidth = 100;
	private double simulationHeight = 100;
	private double speedOfLight = 1;
	private double timeStep = 1;

	private GeneralBoundaryType boundaryType = GeneralBoundaryType.Periodic;

	private Interpolator interpolator = new CloudInCell();

	// Grid related settings

	private int gridCellsX = 10;
	private int gridCellsY = 10;

	private FieldSolver gridSolver = new FieldSolver();
	private PoissonSolver poissonSolver = new PoissonSolverFFTPeriodic();

	private boolean useGrid = true;

	// Particle related settings

	private int numOfParticles = 100;
	private double particleRadius = 1;
	private double particleMaxSpeed = speedOfLight;

	private List<Particle> particles = new ArrayList<Particle>();

	private Detector collisionDetector = new Detector();
	private CollisionAlgorithm collisionResolver = new CollisionAlgorithm();
	private Solver particleSolver = new EmptySolver();
	private List<Force> forces = new ArrayList<Force>();

	// Batch version settings

	private int iterations = 100;

	// Distributed version settings

	private int numOfNodes = 1;
	private String iplServer = "localhost";
	private String iplPool = "openpixi";

	//----------------------------------------------------------------------------------------------
	// SIMPLE GETTERS
	//----------------------------------------------------------------------------------------------

	public double getSimulationWidth() {
		return simulationWidth;
	}

	public double getSimulationHeight() {
		return simulationHeight;
	}

	public int getGridCellsX() {
		return gridCellsX;
	}

	public int getGridCellsY() {
		return gridCellsY;
	}

	public double getSpeedOfLight() {
		return speedOfLight;
	}

	public double getTimeStep() {
		return timeStep;
	}

	public Detector getCollisionDetector() {
		return collisionDetector;
	}

	public CollisionAlgorithm getCollisionAlgorithm() {
		return collisionResolver;
	}

	public FieldSolver getGridSolver() {
		return gridSolver;
	}

	public PoissonSolver getPoissonSolver() {
		return poissonSolver;
	}

	public Solver getParticleSolver() {
		return particleSolver;
	}

	public Interpolator getInterpolator() {
		return interpolator;
	}

	public int getNumOfNodes() {
		return numOfNodes;
	}

	public GeneralBoundaryType getBoundaryType() {
		return boundaryType;
	}

	public int getIterations() {
		return iterations;
	}

	public String getIplServer() {
		return iplServer;
	}

	public boolean useGrid() {
		return useGrid;
	}

	public String getIplPool() {
		return iplPool;
	}

	//----------------------------------------------------------------------------------------------
	// MORE COMPLEX GETTERS / BUILDERS
	//----------------------------------------------------------------------------------------------

	public double getCellWidth() {
		return simulationWidth / gridCellsX;
	}

	public double getCellHeight() {
		return simulationHeight / gridCellsY;
	}

	/**
	 * Build the combined force for simulation.
	 */
	public CombinedForce getForce() {
		CombinedForce combinedForce = new CombinedForce();
		for (Force f: forces) {
			combinedForce.add(f);
		}
		return combinedForce;
	}

	/**
	 * If no particles are specified creates random particles.
	 */
	public List<Particle> getParticles() {
		if (particles.size() != 0) {
			return particles;
		}
		else {
			this.particles = InitialConditions.createRandomParticles(
					simulationWidth,  simulationHeight,
					particleMaxSpeed, numOfParticles, particleRadius);
			return this.particles;
		}
	}

	public GridBoundaryType getGridBoundary() {
		switch (boundaryType) {
			case Periodic:
				return GridBoundaryType.Periodic;
			case Hardwall:
				return GridBoundaryType.Hardwall;
			default:
				return GridBoundaryType.Hardwall;
		}
	}

	public ParticleBoundaryType getParticleBoundary() {
		switch (boundaryType) {
			case Periodic:
				return ParticleBoundaryType.Periodic;
			case Hardwall:
				return ParticleBoundaryType.Hardwall;
			default:
				return ParticleBoundaryType.Hardwall;
		}
	}

	//----------------------------------------------------------------------------------------------
	// SETTERS (Overwrite default values programatically)
	//----------------------------------------------------------------------------------------------

	public void setSimulationWidth(double simulationWidth) {
		this.simulationWidth = simulationWidth;
	}

	public void setSimulationHeight(double simulationHeight) {
		this.simulationHeight = simulationHeight;
	}

	public void setGridCellsX(int gridCellsX) {
		this.gridCellsX = gridCellsX;
	}

	public void setGridCellsY(int gridCellsY) {
		this.gridCellsY = gridCellsY;
	}

	public void setSpeedOfLight(double speedOfLight) {
		this.speedOfLight = speedOfLight;
	}

	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
	}

	public void setCollisionDetector(Detector collisionDetector) {
		this.collisionDetector = collisionDetector;
	}

	public void setCollisionResolver(CollisionAlgorithm collisionResolver) {
		this.collisionResolver = collisionResolver;
	}

	public void setGridSolver(FieldSolver gridSolver) {
		this.gridSolver = gridSolver;
	}

	public void setPoissonSolver(PoissonSolver poissonSolver) {
		this.poissonSolver = poissonSolver;
	}

	public void setParticleSolver(Solver particleSolver) {
		this.particleSolver = particleSolver;
	}

	public void setInterpolator(Interpolator interpolator) {
		this.interpolator = interpolator;
	}

	public void setForces(List<Force> forces) {
		this.forces = forces;
	}

	public void addForce(Force force) {
		this.forces.add(force);
	}

	public void setNumOfParticles(int numOfParticles) {
		this.numOfParticles = numOfParticles;
	}

	public void setParticleRadius(double particleRadius) {
		this.particleRadius = particleRadius;
	}

	public void setParticleMaxSpeed(double particleMaxSpeed) {
		this.particleMaxSpeed = particleMaxSpeed;
	}

	public void addParticle(Particle p) {
		particles.add(p);
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public void setNumOfNodes(int numOfNodes) {
		this.numOfNodes = numOfNodes;
	}

	public void setBoundary(GeneralBoundaryType boundaryType) {
		this.boundaryType = boundaryType;
	}

	public void setIplServer(String iplServer) {
		this.iplServer = iplServer;
	}

	public void useGrid(boolean useGrid) {
		this.useGrid = useGrid;
	}

	public void setIplPool(String iplPool) {
		this.iplPool = iplPool;
	}

	//----------------------------------------------------------------------------------------------
	// CONSTRUCTORS
	//----------------------------------------------------------------------------------------------

	public Settings() {
	}

	/**
	 * Overwrites default values with file input.
	 */
	public Settings(String fileName) {
		this();
		// Parse file
		throw new UnsupportedOperationException();
	}

	/**
	 * Overwrites default values with command line input.
	 */
	public Settings(String[] cmdLine) {
		throw new UnsupportedOperationException();
	}
}
