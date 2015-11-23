package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.ElementFactory;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.gauge.DoubleFFTWrapper;
import org.apache.commons.math3.special.Erf;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.security.acl.Group;

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

		// IR Regulator
		double m = 0.0;

		// UV Regulator
		double psqrMax = 4.0 * effTransversalDimensions / (as * as);
		double lambda = 1.0;

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
				double psqr = computeLatticeMomentumSquared(j);
				double x = psqr / psqrMax;
				double invLaplace;
				if(x <= lambda) {
					invLaplace = 1.0 / psqr;
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
		double gaugeNorm = g * as;
		int totalCells = s.grid.getTotalNumberOfCells();
		for (int i = 0; i < totalCells; i++) {
			int[] gridPos = s.grid.getCellPos(i);
			int[] transversalGridPos = GridFunctions.reduceGridPos(gridPos, direction);
			int longitudinalGridPos = gridPos[direction];
			double z = longitudinalGridPos * as - location;
			int transversalCellIndex = GridFunctions.getCellIndex(transversalGridPos, transversalNumCells);

			// Shape function (i.e. F(z,t)) at t = -dt/2 and t = dt /2.
			double s0 = integratedShapeFunction(z, - at / 2.0, orientation, longitudinalWidth);
			double s1 = integratedShapeFunction(z, + at / 2.0, orientation, longitudinalWidth);

			// Setup the gauge links at t = -dt/2 and t = dt/2
			GroupElement V0 = phi[transversalCellIndex].mult(- s0 * g).getLink();
			GroupElement V0next = phi[transversalCellIndex].mult(- s1 * g).getLink();

			// New method: Apply gauge transformation directly to gauge links without the use of a discretized derivative.
			for (int j = 0; j < numberOfDimensions; j++) {
				if (j != direction) {
					int transversalCellIndexShifted = GridFunctions.getCellIndex(
							GridFunctions.reduceGridPos(
									s.grid.getCellPos(s.grid.shift(i, j, 1))
									, direction),
							transversalNumCells);

					GroupElement V1 = phi[transversalCellIndexShifted].mult(- s0 * g).getLink();
					GroupElement V1next = phi[transversalCellIndexShifted].mult(- s1 * g).getLink();

					GroupElement U = s.grid.getU(i, j);
					GroupElement Unext = s.grid.getUnext(i, j);
					// U_x,i = V_x V_{x+i}^t
					s.grid.setU(i, j, V0.mult(U).mult(V1.adj()));
					s.grid.setUnext(i, j, V0next.mult(Unext).mult(V1next.adj()));

					// Also write to copy of the grid.
					gridCopy.setU(i, j, V0.mult(V1.adj()));
					gridCopy.setUnext(i, j, V0next.mult(V1next.adj()));
				}
			}
		}


		// Third step: Compute electric field from temporal plaquette
		for (int i = 0; i < totalCells; i++) {
			for (int j = 0; j < numberOfDimensions; j++) {
				s.grid.setE(i, j, s.grid.getEFromLinks(i, j));
				gridCopy.setE(i, j, gridCopy.getEFromLinks(i, j));
			}
		}

		// Compute gauss violation from grid copy
		gaussViolation = new AlgebraElement[s.grid.getTotalNumberOfCells()];
		for (int i = 0; i < gridCopy.getTotalNumberOfCells(); i++) {
			if(s.grid.isActive(i)) {
				gaussViolation[i] = gridCopy.getGaussConstraint(i);
			} else {
				gaussViolation[i] = factory.algebraZero();
			}
		}
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

	private double integratedShapeFunction(double z, double t, int o, double width) {

		double arg = (t - o*z)/(width*Math.sqrt(2));
		return  0.5 + 0.5*Erf.erf(arg);
	}

	private double computeLatticeMomentumSquared(int cellIndex) {
		int[] transversalGridPos = GridFunctions.getCellPos(cellIndex, effTransversalNumCells);

		double momentumSquared = 2.0 * effTransversalDimensions;
		for (int i = 0; i < effTransversalDimensions; i++) {
			momentumSquared -= 2.0 * Math.cos((2.0 * Math.PI * transversalGridPos[i]) / transversalNumCells[i]);
		}

		return momentumSquared / (as * as);
	}
}
