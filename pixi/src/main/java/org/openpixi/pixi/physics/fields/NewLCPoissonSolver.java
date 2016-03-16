package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.ElementFactory;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.gauge.DoubleFFTWrapper;
import org.apache.commons.math3.special.Erf;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.GridFunctions;

public class NewLCPoissonSolver {

	private int direction;
	private int orientation;
	private double location;
	private double longitudinalWidth;

	private AlgebraElement[] transversalChargeDensity;
	private AlgebraElement[] phi;
	private int[] transversalNumCells;
	private int[] effTransversalNumCells;

	private int numberOfDimensions;
	private int totalTransversalCells;
	private int transversalDimensions;
	private int effTransversalDimensions;
	private double as;
	private double at;
	private double g;

	public double infraredCoefficient = 0.0;
	public double lowPassCoefficient = 1.0;

	private ElementFactory factory;
	private Simulation s;

	private AlgebraElement[] gaussViolation;
	private Grid gridCopy;

	public NewLCPoissonSolver(int direction, int orientation, double location, double longitudinalWidth, AlgebraElement[] transversalChargeDensity, int[] transversalNumCells) {
		this.direction = direction;
		this.orientation = orientation;
		this.location = location;
		this.longitudinalWidth = longitudinalWidth;
		this.transversalChargeDensity = transversalChargeDensity;
		this.transversalNumCells = transversalNumCells;
		this.transversalDimensions = transversalNumCells.length;
		this.effTransversalNumCells = GridFunctions.getEffectiveNumCells(transversalNumCells);
		this.effTransversalDimensions = GridFunctions.getEffectiveNumberOfDimensions(transversalNumCells);
	}

	public void initialize(Simulation s) {
		factory = s.grid.getElementFactory();
		as = s.grid.getLatticeSpacing();
		at = s.getTimeStep();
		g = s.getCouplingConstant();
		numberOfDimensions = s.getNumberOfDimensions();

		totalTransversalCells = GridFunctions.getTotalNumberOfCells(transversalNumCells);
		phi = new AlgebraElement[totalTransversalCells];
		for (int i = 0; i < totalTransversalCells; i++) {
			phi[i] = factory.algebraZero();
		}
		this.s = s;

		// Create a copy of the grid.
		gridCopy = new Grid(s.grid);
		gridCopy.createGrid();
	}

	public void solve(Simulation s) {
		DoubleFFTWrapper fft = new DoubleFFTWrapper(transversalNumCells);

		// UV Regulator (in lattice energy units)
		double lambdaSquared = lowPassCoefficient * lowPassCoefficient;

		// IR Regulator (in lattice energy units)
		double mSquared = infraredCoefficient * infraredCoefficient;

		// First step: compute transversal potential phi
		for (int i = 0; i < factory.numberOfComponents; i++) {
			// Initialize array for FFT and fill it with charge density of component i.
			double[] fftArray = new double[fft.getFFTArraySize()];
			for (int j = 0; j < totalTransversalCells; j++) {
				fftArray[fft.getFFTArrayIndex(j)] = transversalChargeDensity[j].get(i);
			}
			// Transform charge density to momentum space.
			fft.complexForward(fftArray);
			// Solve Poisson equation in momentum space.
			for (int j = 1; j < totalTransversalCells; j++) {
				double pEffSquared = computeEffectiveLatticeMomentumSquared(j);
				double pSquare = computeLatticeMomentumSquared(j);
				double invLaplace;
				// Implement as momentum cutoff
				if(pSquare <= lambdaSquared) {
					invLaplace = 1.0 / (pEffSquared + mSquared);
				} else {
					invLaplace = 0.0;
				}
				fftArray[fft.getFFTArrayIndex(j)] *= invLaplace;
				fftArray[fft.getFFTArrayIndex(j) + 1] *= invLaplace;
			}
			fftArray[0] = 0.0;
			fftArray[1] = 0.0;

			// Transform back to position space.
			fft.complexInverse(fftArray, true);

			// Set transversal potential.
			for (int j = 0; j < totalTransversalCells; j++) {
				this.phi[j].set(i, fftArray[fft.getFFTArrayIndex(j)]);
			}
		}

		// Second step: compute links from transversal potential
		GaugeLinkSetter gaugeLinkSetter = new GaugeLinkSetter();
		gaugeLinkSetter.initialize(s.grid, gridCopy, phi);
		s.grid.getCellIterator().execute(s.grid, gaugeLinkSetter);

		// Third step: Compute electric field from temporal plaquette
		ElectricFieldSetter electricFieldSetter = new ElectricFieldSetter();
		s.grid.getCellIterator().execute(s.grid, electricFieldSetter);
		gridCopy.getCellIterator().execute(gridCopy, electricFieldSetter);

		// Compute gauss violation from grid copy
		GaussViolationCalculation gvCalculation = new GaussViolationCalculation();
		gvCalculation.reset(gridCopy);
		gridCopy.getCellIterator().execute(gridCopy, gvCalculation);
		gaussViolation = gvCalculation.getResult();

		// Remove reference to gridCopy (?)
		this.gridCopy = null;

	}

	public AlgebraElement getGaussConstraint(int i) {
		return gaussViolation[i];
	}

	public GroupElement getV(int index, double t) {
		int[] gridPos = s.grid.getCellPos(index);
		int[] transversalGridPos = GridFunctions.reduceGridPos(gridPos, direction);
		int transversalCellIndex = GridFunctions.getCellIndex(transversalGridPos, transversalNumCells);
		int longitudinalGridPos = gridPos[direction];
		return getV(longitudinalGridPos, transversalCellIndex, t);
	}

	public GroupElement getV(int longitudinalIndex, int transversalIndex, double t) {
		double z = longitudinalIndex * as - location;
		double shape = integratedShapeFunction(z, t, orientation, longitudinalWidth);
		return phi[transversalIndex].mult(- shape * g).getLink();
	}

	public GroupElement getV(double longitudinalPosition, int transversalIndex, double t) {
		double z = longitudinalPosition - location;
		double shape = integratedShapeFunction(z, t, orientation, longitudinalWidth);
		return phi[transversalIndex].mult(- shape * g).getLink();
	}

	public GroupElement getV(int transversalIndex) {
		return phi[transversalIndex].mult(- g).getLink();
	}

	public GroupElement getU(int transversalIndex, int direction) {
		int shiftedIndex = GridFunctions.shift(transversalIndex, direction, 1 , transversalNumCells);
		return getV(transversalIndex).mult(getV(shiftedIndex).adj());
	}

	private double integratedShapeFunction(double z, double t, int o, double width) {

		double arg = (t - o*z)/(width*Math.sqrt(2));
		return  0.5 + 0.5*Erf.erf(arg);
	}

	private double computeEffectiveLatticeMomentumSquared(int cellIndex) {
		int[] transversalGridPos = GridFunctions.getCellPos(cellIndex, effTransversalNumCells);

		double momentumSquared = 2.0 * effTransversalDimensions;
		for (int i = 0; i < effTransversalDimensions; i++) {
			momentumSquared -= 2.0 * Math.cos((2.0 * Math.PI * transversalGridPos[i]) / transversalNumCells[i]);
		}

		return momentumSquared / (as * as);
	}

	private double computeLatticeMomentumSquared(int cellIndex) {
		int[] transversalGridPos = GridFunctions.getCellPos(cellIndex, effTransversalNumCells);
		double twopi = 2.0 * Math.PI;

		double momentumSquared = 0.0;
		for (int i = 0; i < effTransversalDimensions; i++) {
			double momentumComponent;
			int n = transversalNumCells[i];
			if(transversalGridPos[i] < n / 2) {
				momentumComponent = twopi * transversalGridPos[i] / (as * n);
			} else {
				momentumComponent = twopi * (n - transversalGridPos[i]) / (as * n);
			}

			momentumSquared += momentumComponent * momentumComponent;
		}

		return momentumSquared;
	}

	// Classes for multithreaded operations

	/**
	 * This class sets all the gauge links according to the solution of the Poisson equation.
	 */
	private class GaugeLinkSetter implements CellAction {
		private Grid grid;
		private Grid gridCopy;
		private AlgebraElement[] phi;

		public void initialize(Grid grid, Grid gridCopy, AlgebraElement[] phi) {
			this.grid = grid;
			this.gridCopy = gridCopy;
			this.phi = phi;
		}

		public void execute(Grid gr, int index) {
			int[] gridPos = this.grid.getCellPos(index);
			int[] transversalGridPos = GridFunctions.reduceGridPos(gridPos, direction);
			int longitudinalGridPos = gridPos[direction];
			double z = longitudinalGridPos * as - location;
			int transversalCellIndex = GridFunctions.getCellIndex(transversalGridPos, transversalNumCells);

			// Shape function (i.e. F(z,t)) at t = -dt/2 and t = dt /2.
			double s0 = g * integratedShapeFunction(z, - at / 2.0, orientation, longitudinalWidth);
			double s1 = g * integratedShapeFunction(z, + at / 2.0, orientation, longitudinalWidth);

			// Setup the gauge links at t = -dt/2 and t = dt/2
			GroupElement V0 = this.phi[transversalCellIndex].mult(- s0).getLink();
			GroupElement V0next = this.phi[transversalCellIndex].mult(- s1).getLink();

			// New method: Apply gauge transformation directly to gauge links without the use of a discretized derivative.
			for (int j = 0; j < numberOfDimensions; j++) {
				if (j != direction) {
					int transversalCellIndexShifted = GridFunctions.getCellIndex(
							GridFunctions.reduceGridPos(
									this.grid.getCellPos(s.grid.shift(index, j, 1))
									, direction),
							transversalNumCells);

					GroupElement V1 = this.phi[transversalCellIndexShifted].mult(- s0).getLink();
					GroupElement V1next = this.phi[transversalCellIndexShifted].mult(- s1).getLink();

					GroupElement U = this.grid.getU(index, j);
					GroupElement Unext = this.grid.getUnext(index, j);
					// U_x,i = V_x V_{x+i}^t
					this.grid.setU(index, j, V0.mult(U).mult(V1.adj()));
					this.grid.setUnext(index, j, V0next.mult(Unext).mult(V1next.adj()));

					// Also write to copy of the grid.
					this.gridCopy.setU(index, j, V0.mult(V1.adj()));
					this.gridCopy.setUnext(index, j, V0next.mult(V1next.adj()));
				}
			}
		}
	}

	/**
	 * Computes and stores the Gauss law violation at every lattice site.
	 */
	private class GaussViolationCalculation implements CellAction {
		public AlgebraElement[] gaussViolation;
		private ElementFactory factory;


		public void reset(Grid grid) {
			this.gaussViolation = new AlgebraElement[grid.getTotalNumberOfCells()];
			this.factory = grid.getElementFactory();
		}

		public void execute(Grid grid, int index) {
			if(grid.isActive(index)) {
				this.gaussViolation[index] = grid.getGaussConstraint(index);
			} else {
				this.gaussViolation[index] = this.factory.algebraZero();
			}
		}

		public AlgebraElement[] getResult() {
			return gaussViolation;
		}
	}

	/**
	 * This class computes the electric fields from the gauge links (U and Unext).
	 */
	private class ElectricFieldSetter implements CellAction {
		public void execute(Grid grid, int index) {
			for (int j = 0; j < numberOfDimensions; j++) {
				grid.setE(index, j, grid.getEFromLinks(index, j));
			}
		}
	}
}
