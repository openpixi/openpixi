package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.collision.algorithms.CollisionAlgorithm;
import org.openpixi.pixi.physics.collision.detectors.Detector;
import org.openpixi.pixi.physics.fields.FieldSolver;
import org.openpixi.pixi.physics.fields.PoissonSolver;
import org.openpixi.pixi.physics.fields.PoissonSolverFFTPeriodic;
import org.openpixi.pixi.physics.force.CombinedForce;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.grid.GridBoundaryType;
import org.openpixi.pixi.physics.grid.Interpolator;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundary;
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
 * AND BEFORE ANY GETTER IS CALLED !!!
 *
 * TODO replace InitialConditions
 */
public class Settings {

	private double simulationWidth = 100;
	private double simulationHeight = 100;
	private double speedOfLight = 1;
	private double timeStep = 1;

	private Interpolator interpolator = new Interpolator();

	// Grid related settings

	private int gridCellsX = 10;
	private int gridCellsY = 10;

	private GridBoundaryType gridBoundary = GridBoundaryType.Hardwall;
	private FieldSolver gridSolver = new FieldSolver();
	private PoissonSolver poissonSolver = new PoissonSolverFFTPeriodic();

	// Particle related settings

	private int particleCount = 100;
	private double particleRadius = 1;
	private double particleMaxSpeed = speedOfLight;

	private ParticleBoundaryType particleBoundary = ParticleBoundaryType.Hardwall;
	private Detector collisionDetector = new Detector();
	private CollisionAlgorithm collisionResolver = new CollisionAlgorithm();
	private Solver particleSolver = new EmptySolver();
	private List<Force> forces = new ArrayList<Force>();

	// Batch version settings

	private int iterations = 100;

	// Distributed version settings

	private int numOfNodes;
	/** Width (in number of cells) of the circumference around the local simulation area
	    which needs to be shared in order to achieve correct interpolation. */
	private static final int SHARED_DATA_WIDTH = 1;

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

	public GridBoundaryType getGridBoundary() {
		return gridBoundary;
	}

	public ParticleBoundaryType getParticleBoundary() {
		return particleBoundary;
	}

	public int getSharedDataWidth() {
		return SHARED_DATA_WIDTH;
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
	 * Create random particles based on the given settings.
	 */
	public List<Particle> getParticles() {
		return InitialConditions.createRandomParticles(simulationWidth,  simulationHeight,
				particleMaxSpeed, particleCount, particleRadius);
	}

	//----------------------------------------------------------------------------------------------
	// SETTERS
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

	public void setParticleCount(int particleCount) {
		this.particleCount = particleCount;
	}

	public void setParticleRadius(double particleRadius) {
		this.particleRadius = particleRadius;
	}

	public void setParticleMaxSpeed(double particleMaxSpeed) {
		this.particleMaxSpeed = particleMaxSpeed;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public void setNumOfNodes(int numOfNodes) {
		this.numOfNodes = numOfNodes;
	}

	public void setGridBoundary(GridBoundaryType gridBoundary) {
		this.gridBoundary = gridBoundary;
	}

	public void setParticleBoundary(ParticleBoundaryType particleBoundary) {
		this.particleBoundary = particleBoundary;
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
