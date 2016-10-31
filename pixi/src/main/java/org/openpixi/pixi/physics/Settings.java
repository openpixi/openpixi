package org.openpixi.pixi.physics;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.parallel.cellaccess.*;
import org.openpixi.pixi.parallel.particleaccess.*;
import org.openpixi.pixi.physics.fields.*;
import org.openpixi.pixi.physics.fields.fieldgenerators.IFieldGenerator;
import org.openpixi.pixi.physics.fields.currentgenerators.ICurrentGenerator;
import org.openpixi.pixi.physics.force.*;
import org.openpixi.pixi.physics.grid.*;
import org.openpixi.pixi.physics.initial.IInitialCondition;
import org.openpixi.pixi.physics.particles.*;
import org.openpixi.pixi.physics.movement.solver.*;
import org.openpixi.pixi.ui.util.yaml.YamlPanels;

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
	private int    numberOfColors = 2;
	private int    numberOfDimensions = 3;
	private double couplingConstant = 1.0;
	private double timeStep = 0.1;
	private double gridStep = 1;
	private double[] gridSteps;
	private boolean useUnevenGrid = false;
	private double tMax = 1000;
	private GeneralBoundaryType boundaryType = GeneralBoundaryType.Periodic;
	private InterpolatorAlgorithm interpolator = new EmptyInterpolator();

	// Grid related settings
	private int[] gridCells;
	private double[] simulationWidth;
	private FieldSolver fieldSolver = new FieldSolver();
	private PoissonSolver poissonSolver = new EmptyPoissonSolver();
	private boolean useGrid = true;
	private boolean relativistic = true;
	// Regions
	private boolean evaluationRegionEnabled = false;
	private int[] evaluationRegionPoint1;
	private int[] evaluationRegionPoint2;
	private boolean activeRegionEnabled = false;
	private int[] activeRegionPoint1;
	private int[] activeRegionPoint2;

	// Particle related settings
	private int numOfParticles = 0;

	private SimulationType simulationType = SimulationType.TemporalYangMills;
	private List<IParticle> particles = new ArrayList<IParticle>();
	private ParticleSolver particleSolver = new EmptyParticleSolver();
	private List<Force> forces = new ArrayList<Force>();


	// FieldGenerator related settings
	private ArrayList<IFieldGenerator> fieldGenerators = new ArrayList<IFieldGenerator>();

	// CurrentGenerator related settings
	private ArrayList<ICurrentGenerator> currentGenerators = new ArrayList<ICurrentGenerator>();

	// Initial conditions (new, replaces field and current generators)
	private ArrayList<IInitialCondition> initialConditions = new ArrayList<IInitialCondition>();

	// Diagnostics related settings
	/**
	 * Used to mark output files
	 */
	private String runid = "default-run";
	private ArrayList<Diagnostics> diagnostics = new ArrayList<Diagnostics>();
	// Batch version settings
	private int iterations = (int) Math.ceil(tMax/timeStep);
	// Parallel (threaded) version settings
	private int numOfThreads = 1;
	/* The creation and start of the new threads is expensive. Therefore, in the parallel
	 * simulation we use ExecutorService which is maintaining a fixed number of threads running
	 * all the time and assigns work to the threads on the fly according to demand. */
	private ExecutorService threadsExecutor;

	// Panel management
	private YamlPanels yamlPanels;

	//----------------------------------------------------------------------------------------------
	// SIMPLE GETTERS
	//----------------------------------------------------------------------------------------------
	public SimulationType getSimulationType() {
		return this.simulationType;
	}
	
	public double getSimulationWidth(int i) {
		return simulationWidth[i];
	}
	
	public int getGridCells(int i)
	{
		return gridCells[i];
	}

	public int[] getGridCells() {
		return gridCells;
	}

	public double getGridStep() {
		if(useUnevenGrid) {
			throw new RuntimeException("Use getGridStep(int direction).");
		}
		return gridStep;
	}

	public double getGridStep(int i) {
		if(useUnevenGrid) {
			return gridSteps[i];
		} else {
			return gridStep;
		}
	}

	public boolean useUnevenGrid() {
		return useUnevenGrid;
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

	public double getCouplingConstant() {
		return couplingConstant;
	}

	public double getTimeStep() {
		return timeStep;
	}

	public FieldSolver getFieldSolver() {
		/*
		 * For the distributed tests to pass we need to create new grid solver so that the two
		 * simulation instances do not share the cell iterator!
		 */
		return fieldSolver.clone();
	}

	public PoissonSolver getPoissonSolver() {
		return poissonSolver;
	}

	public ParticleSolver getParticleSolver() {
		return particleSolver;
	}

	public InterpolatorAlgorithm getInterpolator() {
		return interpolator;
	}


	public ArrayList<Diagnostics> getDiagnostics() {
		ArrayList<Diagnostics> diagnosticsCopy = new ArrayList<Diagnostics>();
		for (Diagnostics d : diagnostics) {
			diagnosticsCopy.add(d);
		}
		return diagnosticsCopy;
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

	public int getNumOfThreads() {
		return numOfThreads;
	}

	public ArrayList<IFieldGenerator> getFieldGenerators() {
		return this.fieldGenerators;
	}

	public ArrayList<ICurrentGenerator> getCurrentGenerators()
	{
		return this.currentGenerators;
	}

	public ArrayList<IInitialCondition> getInitialConditions()
	{
		return this.initialConditions;
	}

	public YamlPanels getYamlPanels() {
		return yamlPanels;
	}

	public boolean isEvaluationRegionEnabled() {
		return evaluationRegionEnabled;
	}

	public int[] getEvaluationRegionPoint1() {
		return evaluationRegionPoint1;
	}

	public int[] getEvaluationRegionPoint2() {
		return evaluationRegionPoint2;
	}

	public boolean isActiveRegionEnabled() {
		return activeRegionEnabled;
	}

	public int[] getActiveRegionPoint1() {
		return activeRegionPoint1;
	}

	public int[] getActiveRegionPoint2() {
		return activeRegionPoint2;
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
	public void setSimulationType(SimulationType simulationType) {
		this.simulationType = simulationType;
		applySimulationTypeSetting();
	}

	/**
	 * Set dimension and number of grid cells in each direction.
	 * @param gridCells
	 */
	public void setGridCells(int[] gridCells) {
		setNumberOfDimensions(gridCells.length);
		for (int i=0; i<gridCells.length; i++) {
			setGridCells(i, gridCells[i]);
		}
	}

	public void setGridCells(int i, int num) {
		gridCells[i] = num;
		simulationWidth[i] = gridStep * num;
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
		gridSteps = new double[numberOfDimensions];
	}

	public void setCouplingConstant(double g) {
		this.couplingConstant = g;
	}

	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
		this.iterations = (int) Math.ceil(tMax / timeStep);
	}

	public void setTMax(double TMax) {
		this.tMax = TMax;
		this.iterations = (int) Math.ceil(TMax / timeStep);
	}

	public void setGridStep(double gridstep) {
		this.gridStep = gridstep;
	}

	public void setGridStep(int i, double gridstep) {
		this.useUnevenGrid = true;
		this.gridSteps[i] = gridstep;
	}

	public void setRelativistic(boolean rel) {
		this.relativistic = rel;
	}

	public void setFieldSolver(FieldSolver fieldSolver) {
		this.fieldSolver = fieldSolver;
	}

	public void setPoissonSolver(PoissonSolver poissonSolver) {
		this.poissonSolver = poissonSolver;
	}

	public void setParticleSolver(ParticleSolver particleSolver) {
		this.particleSolver = particleSolver;
	}

	public void setInterpolator(InterpolatorAlgorithm interpolator) {
		this.interpolator = interpolator;
	}

	public void setDiagnostics(ArrayList<Diagnostics> diagnostics) {
		this.diagnostics = diagnostics;
	}
	
	public void addDiagnostics(Diagnostics newDiagnostic) {
		this.diagnostics.add(newDiagnostic);
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

	public void setFieldGenerators(ArrayList<IFieldGenerator> fieldGenerators) {
		this.fieldGenerators = fieldGenerators;
	}

	public void addFieldGenerator(IFieldGenerator generator)
	{
		this.fieldGenerators.add(generator);
	}

	public void addCurrentGenerator(ICurrentGenerator generator) {
		this.currentGenerators.add(generator);
	}

	public void setCurrentGenerators(ArrayList<ICurrentGenerator> currentGenerators)
	{
		this.currentGenerators = currentGenerators;
	}

	public void addInitialConditions(IInitialCondition initialCondition) {
		this.initialConditions.add(initialCondition);
	}

	public void setYamlPanels(YamlPanels yamlPanels) {
		this.yamlPanels = yamlPanels;
	}

	public void setEvaluationRegionEnabled(boolean value) {
		this.evaluationRegionEnabled = value;
	}

	public void setEvaluationRegionPoint1(int[] point) {
		this.evaluationRegionPoint1 = point;
	}

	public void setEvaluationRegionPoint2(int[] point) {
		this.evaluationRegionPoint2 = point;
	}

	public void setActiveRegionEnabled(boolean value) {
		this.activeRegionEnabled = value;
	}

	public void setActiveRegionPoint1(int[] point) {
		this.activeRegionPoint1 = point;
	}

	public void setActiveRegionPoint2(int[] point) {
		this.activeRegionPoint2 = point;
	}


	//----------------------------------------------------------------------------------------------
	// VARIOUS
	//----------------------------------------------------------------------------------------------
	public Settings() {
		simulationWidth = new double[numberOfDimensions];
		for (int i = 0; i < numberOfDimensions; i++) {
			simulationWidth[i] = 1;
		}

		gridCells = new int[numberOfDimensions];
		for (int i = 0; i < numberOfDimensions; i++) {
			gridCells[i] = 1;
		}

		gridSteps = new double[numberOfDimensions];
		for (int i = 0; i < numberOfDimensions; i++) {
			gridSteps[i] = 1.0;
		}

		applySimulationTypeSetting();
	}

	/**
	 * Sets up the components of the simulation (Grid, FieldSolver, ParticleSolver, Interpolation, ..)
	 * according to SimulationType.
	 */
	private void applySimulationTypeSetting() {
		switch(simulationType) {
			case TemporalYangMills:
				setBoundary(GeneralBoundaryType.Periodic);
				setFieldSolver(new FastTYMSolver());
				setParticleSolver(new EmptyParticleSolver());
				setInterpolator(new EmptyInterpolator());
				break;
			case TemporalCGC:
				setBoundary(GeneralBoundaryType.Absorbing);
				setFieldSolver(new FastTYMSolver());
				setParticleSolver(new CGCParticleSolver());
				setInterpolator(new CGCParticleInterpolation());
				break;
			case TemporalCGCNGP:
				setBoundary(GeneralBoundaryType.Absorbing);
				setFieldSolver(new FastTYMSolver());
				setParticleSolver(new CGCParticleSolver());
				setInterpolator(new CGCParticleInterpolationNGP());
				break;
			case TemporalOptimizedCGCNGP:
				setBoundary(GeneralBoundaryType.Absorbing);
				setFieldSolver(new FastTYMSolver());
				setParticleSolver(new CGCSuperParticleSolver());
				setInterpolator(new CGCSuperParticleInterpolationNGP());
				break;
			case BoostInvariantCGC:
				break;
		}
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
