package org.openpixi.pixi.physics.fields;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.special.Erf;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.ElementFactory;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.currentgenerators.NewLorenzLCCurrent;
import org.openpixi.pixi.physics.gauge.DoubleFFTWrapper;
import org.openpixi.pixi.physics.util.GridFunctions;

public class NewLorenzLCPoissonSolver {

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

	public NewLorenzLCPoissonSolver(int direction, int orientation, double location, double longitudinalWidth, AlgebraElement[] transversalChargeDensity, int[] transversalNumCells) {
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
	}

	public void solve(Simulation s) {
		DoubleFFTWrapper fft = new DoubleFFTWrapper(transversalNumCells);

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
				fftArray[fft.getFFTArrayIndex(j)] /= psqr;
				fftArray[fft.getFFTArrayIndex(j) + 1] /= psqr;
			}
			fftArray[0] = 0.0;
			fftArray[1] = 0.0;

			// Transform back to position space.
			fft.complexInverse(fftArray, true);

			// Set transversal potential.
			for (int j = 0; j < totalTransversalCells; j++) {
				this.phi[j].set(i, fftArray[fft.getFFTArrayIndex(j)]);
			}
			System.out.println();
		}

		// Second step: compute links from transversal potential
		double gaugeNorm1 = g * as;
		double gaugeNorm2 = g * at;
		int totalCells = s.grid.getTotalNumberOfCells();
		for (int i = 0; i < totalCells; i++) {
			int[] gridPos = s.grid.getCellPos(i);
			int[] transversalGridPos = GridFunctions.reduceGridPos(gridPos, direction);
			int longitudinalGridPos = gridPos[direction];
			double z = longitudinalGridPos * as - location;
			int transversalCellIndex = GridFunctions.getCellIndex(transversalGridPos, transversalNumCells);

			// Shape function (i.e. F(z,t)) at t = -dt/2 and t = dt /2.

			//double s0 = shapeFunction(z, -at / 2.0, orientation, longitudinalWidth);
			//double s1 = shapeFunction(z, +at / 2.0, orientation, longitudinalWidth);
			double s0 = shapeFunction(z + as/2.0, -at / 2.0, orientation, longitudinalWidth);
			double s1 = shapeFunction(z + as/2.0, +at / 2.0, orientation, longitudinalWidth);
			double s2 = shapeFunction(z, 0.0, orientation, longitudinalWidth);
			double s3 = shapeFunction(z, at, orientation, longitudinalWidth);

			// Set temporal and spatial links.
			AlgebraElement currentPhi = phi[transversalCellIndex];
			GroupElement UZP = currentPhi.mult(orientation * s0 * gaugeNorm1).getLink(); // U_{x,i} ~ A_{x+i/2, i}
			GroupElement UZF = currentPhi.mult(orientation * s1 * gaugeNorm1).getLink(); // U_{x+0,i}
			GroupElement U0P = currentPhi.mult(-s2 * gaugeNorm2).getLink(); // U_{x,0}
			GroupElement U0F = currentPhi.mult(-s3 * gaugeNorm2).getLink(); // U_{x+0,0}

			s.grid.setU(i, direction, s.grid.getU(i, direction).mult(UZP));
			s.grid.setUnext(i, direction, s.grid.getUnext(i, direction).mult(UZF));
			s.grid.setU0(i, s.grid.getU0(i).mult(U0P));
			s.grid.setU0next(i, s.grid.getU0next(i).mult(U0F));
		}
	}

	private double shapeFunction(double z, double t, int o, double width) {
		Gaussian gauss = new Gaussian(0.0, width);
		return gauss.value(z - o * t);
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
