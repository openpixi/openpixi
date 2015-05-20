package org.openpixi.pixi.physics;

import org.openpixi.pixi.diagnostics.methods.Diagnostics;
import org.openpixi.pixi.parallel.cellaccess.*;
import org.openpixi.pixi.parallel.particleaccess.*;
import org.openpixi.pixi.physics.fields.*;
import org.openpixi.pixi.physics.fields.FieldGenerators.IFieldGenerator;
import org.openpixi.pixi.physics.force.*;
import org.openpixi.pixi.physics.grid.*;
import org.openpixi.pixi.physics.particles.*;
import org.openpixi.pixi.physics.solver.*;
import org.openpixi.pixi.physics.solver.relativistic.LeapFrogRelativistic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Specifies default values of simulation parameters. The default values can be
 * overridden with a settings file input. The default values can be overridden
 * with a command line input. The default values can be overridden
 * programatically.
 *
 * NOTICE ON USAGE: To assure that the class is used in the intended way THE
 * SETTERS SHOULD BE CALLED RIGHT AFTER A PARAMETERLESS CONSTRUCTOR AND BEFORE
 * ANY OF THE MORE COMPLEX GETTERS IS CALLED !!!
 *
 * In the tests of the distributed version we pass the same settings class to
 * all the different threads which simulate the distributed behaviour.
 * Consequently, the different simultaneously running simulations do have the
 * same particle solver, interpolator etc. and this is very dangerous. TODO
 * ensure that all the getters retrieve new objects
 */
public class Settings {

	//----------------------------------------------------------------------------------------------
	// DEFAULT VALUES
	//----------------------------------------------------------------------------------------------
	private double speedOfLight = 1;
	private int    numberOfColors = 1;
	private int	   numberOfDimensions = 3;
    private double couplingConstant = 1.0;
	private double timeStep = 0.1;
	private double gridStep = 1;
	private double tMax = 1000;
	private int spectrumStep = 300;
	private String filePath = "default";
	private GeneralBoundaryType boundaryType = GeneralBoundaryType.Periodic;
	private InterpolatorAlgorithm interpolator = new EmptyInterpolator();

	// Grid related settings
	private int[] gridCells;
	private double[] simulationWidth;
	private FieldSolver gridSolver = new GeneralYangMillsSolver();//new FieldSolver();
	private PoissonSolver poissonSolver = new EmptyPoissonSolver();
	private boolean useGrid = true;
	private boolean relativistic = true;

	// Particle related settings
	private int numOfParticles = 0;

	private int simulationType = 0;
	private int writeToFile = 0;
	private List<IParticle> particles = new ArrayList<IParticle>();
	private Solver particleSolver = new EmptyParticleSolver();
	private List<Force> forces = new ArrayList<Force>();


	// FieldGenerator related settings
	private ArrayList<IFieldGenerator> fieldGenerators = new ArrayList<IFieldGenerator>();

	// Diagnostics related settings
	/**
	 * Used to mark output files
	 */
	private String runid = "default-run";
	private List<Diagnostics> diagnostics = new ArrayList<Diagnostics>();
	// Batch version settings
	private int iterations = (int) Math.ceil(tMax/timeStep);
	// Parallel (threaded) version settings
	private int numOfThreads = 1;
	/* The creation and start of the new threads is expensive. Therefore, in the parallel
	 * simulation we use ExecutorService which is maintaining a fixed number of threads running
	 * all the time and assigns work to the threads on the fly according to demand. */
	private ExecutorService threadsExecutor;

	//----------------------------------------------------------------------------------------------
	// SIMPLE GETTERS
	//----------------------------------------------------------------------------------------------
	public int getSimulationType() {
		return this.simulationType;
	}

	public int getWriteToFile() {
		return this.writeToFile;
	}
	
	public double getSimulationWidth(int i) {
		return simulationWidth[i];
	}

	public double getSimulationWidth() {
		return getSimulationWidth(0);
	}

	public double getSimulationHeight() {
		return getSimulationWidth(1);
	}
	
	public double getSimulationDepth() {
		return getSimulationWidth(2);
	}
	
	public int getGridCells(int i)
	{
		return gridCells[i];
	}

	public int getGridCellsX() {
		return getGridCells(0);
	}

	public int getGridCellsY() {
		return getGridCells(1);
	}
	
	public int getGridCellsZ() {
		return getGridCells(2);
	}
	
	public double getGridStep() {
		return gridStep;
	}
	
	public int getSpectrumStep() {
		return spectrumStep;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public boolean getRelativistic() {
		return relativistic;
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

    public double getCouplingConstant()
    {
        return couplingConstant;
    }

	public double getTimeStep() {
		return timeStep;
	}

	public FieldSolver getGridSolver() {
		/*
		 * For the distributed tests to pass we need to create new grid solver so that the two
		 * simulation instances do not share the cell iterator!
		 */
		return gridSolver.clone();
	}

	public PoissonSolver getPoissonSolver() {
		return poissonSolver;
	}

	public Solver getParticleSolver() {
		return particleSolver;
	}

	public InterpolatorAlgorithm getInterpolator() {
		return interpolator;
	}


	public List<Diagnostics> getDiagnostics() {
		return diagnostics;
	}

	public GeneralBoundaryType getBoundaryType() {
		return boundaryType;
	}

	public int getIterations() {
		return iterations;
	}

	public boolean useGrid() {
		return useGrid;
	}

    public ArrayList<IFieldGenerator> getFieldGenerators()
    {
        return this.fieldGenerators;
    }

	//----------------------------------------------------------------------------------------------
	// MORE COMPLEX GETTERS / BUILDERS
	//----------------------------------------------------------------------------------------------

	/**
	 * Build the combined force for simulation.
	 */
	public CombinedForce getForce() {
		CombinedForce combinedForce = new CombinedForce();
		for (Force f : forces) {
			combinedForce.add(f);
		}
		return combinedForce;
	}

	/**
	 * If no particles are specified creates random particles.
	 *
	 * !!! IMPORTANT !!! Always returns deep copy of the actual particle list!
	 */
	public List<IParticle> getParticles() {
		return cloneParticles();
	}

	private List<IParticle> cloneParticles() {
		List<IParticle> copy = new ArrayList<IParticle>();
		for (IParticle p : particles) {
			copy.add(p.copy());
		}
		return copy;
	}

	public ParticleIterator getParticleIterator() {
		if (numOfThreads == 1) {
			return new SequentialParticleIterator();
		} else if (numOfThreads > 1) {
			return new ParallelParticleIterator(numOfThreads, getThreadsExecutor());
		} else {
			throw new RuntimeException("Invalid number of threads: " + numOfThreads);
		}
	}

	public CellIterator getCellIterator() {
		if (numOfThreads == 1) {
			return new SequentialCellIterator();
		} else if (numOfThreads > 1) {
			return new ParallelCellIterator(numOfThreads, getThreadsExecutor());
		} else {
			throw new RuntimeException("Invalid number of threads: " + numOfThreads);
		}
	}

	/**
	 * Create threads executor on the fly according to demand.
	 */
	private ExecutorService getThreadsExecutor() {
		if (threadsExecutor == null) {
			threadsExecutor = Executors.newFixedThreadPool(numOfThreads);
		}
		return threadsExecutor;
	}

	//----------------------------------------------------------------------------------------------
	// SETTERS (Overwrite default values programatically)
	//----------------------------------------------------------------------------------------------
	public void setSimulationType(int simulationType) {
		this.simulationType = simulationType;
	}

	public void setWriteToFile(int writeTo) {
		this.writeToFile = writeTo;
	}

	public void setGridCells(int i, int num) {
		gridCells[i] = num;
		simulationWidth[i] = gridStep*num;
	}
	
	public void setGridCellsX(int gridCellsX) 
	{
		setGridCells(0, gridCellsX);
	}

	public void setGridCellsY(int gridCellsY) {
		setGridCells(1, gridCellsY);
	}
	
	public void setGridCellsZ(int gridCellsZ) {
		setGridCells(2, gridCellsZ);
	}

	public void setSpeedOfLight(double speedOfLight) {
        this.speedOfLight = speedOfLight;
	}

	public void setNumberOfColors(int numberOfColors)
	{
		this.numberOfColors = numberOfColors;
	}

	public void setNumberOfDimensions(int numberOfDimensions)
	{
		this.numberOfDimensions = numberOfDimensions;
		gridCells = new int[numberOfDimensions];
		simulationWidth = new double[numberOfDimensions];
	}

    public void setCouplingConstant(double g)
    {
        this.couplingConstant = g;
    }

	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
		this.iterations = (int) Math.ceil(tMax/timeStep);
	}
	
	public void setTMax(double TMax) {
		this.tMax = TMax;
		this.iterations = (int) Math.ceil(TMax/timeStep);
	}
	
	public void setGridStep(double gridstep) {
		this.gridStep = gridstep;
	}
	
	public void setSpectrumStep(int spectrumstep) {
		this.spectrumStep = spectrumstep;
	}

	public void setFilePath(String filepath) {
		this.filePath = filepath;
	}

	public void setRelativistic(boolean rel) {
		this.relativistic = rel;
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

	public void setInterpolator(InterpolatorAlgorithm interpolator) {
		this.interpolator = interpolator;
	}

	public void setDiagnostics(List<Diagnostics> diagnostics) {
		this.diagnostics = diagnostics;
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

	public void setParticleList(List<IParticle> particles) {
		this.numOfParticles = particles.size();
		this.particles = particles;
	}

	public void addParticle(IParticle p) {
		particles.add(p);
	}

	/**
	 * @deprecated Use setTimeStep() and setTMax() instead.
	 */
	@Deprecated
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public void setBoundary(GeneralBoundaryType boundaryType) {
		this.boundaryType = boundaryType;
	}

	public void useGrid(boolean useGrid) {
		this.useGrid = useGrid;
	}

	public void setNumOfThreads(int numOfThreads) {
		this.numOfThreads = numOfThreads;
	}

    public void setFieldGenerators(ArrayList<IFieldGenerator> fieldGenerators)
    {
        this.fieldGenerators = fieldGenerators;
    }

    public void addFieldGenerator(IFieldGenerator generator)
    {
        this.fieldGenerators.add(generator);
    }

	//----------------------------------------------------------------------------------------------
	// VARIOUS
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

	/**
	 * Has to be called every time numOfThreads is set to a value higher than 1!
	 * Terminates the threads used by executor service. Is idempotent (can be
	 * called multiple times without side-effects).
	 */
	public void terminateThreads() {
		if (threadsExecutor != null) {
			threadsExecutor.shutdown();
			threadsExecutor = null;
		}
	}
}
