package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.ElementFactory;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.currentgenerators.NewLCCurrent;
import org.openpixi.pixi.physics.gauge.DoubleFFTWrapper;

/**
 * Created by David on 01.09.2015.
 */
public class NewLCPoissonSolver {

	private int direction;
	private int orientation;
	private int surfaceIndex;
	private double longitudinalWidth;

	private AlgebraElement[] transversalChargeDensity;
	private AlgebraElement[] phi;
	private int[] transversalNumCells;

	private int totalTransversalCells;
	private int transversalDimensions;
	private double as;

	private ElementFactory factory;

	public NewLCPoissonSolver(int direction, int orientation, int surfaceIndex, double longitudinalWidth, AlgebraElement[] transversalChargeDensity, int[] transversalNumCells) {
		this.direction = direction;
		this.orientation = orientation;
		this.surfaceIndex = surfaceIndex;
		this.longitudinalWidth = longitudinalWidth;
		this.transversalChargeDensity = transversalChargeDensity;
		this.transversalNumCells = transversalNumCells;
		this.transversalDimensions = transversalNumCells.length;
	}

	public void initialize(Simulation s) {
		factory = s.grid.getElementFactory();
		as = s.grid.getLatticeSpacing();

		totalTransversalCells = NewLCCurrent.getTotalNumberOfCells(transversalNumCells);
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
			for (int j = 0; j < totalTransversalCells; j++) {
				double psqr = computeLatticeMomentumSquared(j);
				fftArray[fft.getFFTArrayIndex(j)] /= psqr;
				fftArray[fft.getFFTArrayIndex(j) + 1] /= psqr;
			}
			// Transform back to position space.
			fft.complexInverse(fftArray, true);

			// Set transversal potential.
			for (int j = 0; j < totalTransversalCells; j++) {
				this.phi[j].set(i, fftArray[fft.getFFTArrayIndex(j)]);
			}
		}

		// Second step: compute links and electric fields from transversal potential
	}

	private double computeLatticeMomentumSquared(int cellIndex) {
		int[] transversalGridPos = NewLCCurrent.getCellPos(cellIndex, transversalNumCells);

		double momentumSquared = 2.0 * transversalDimensions;
		for (int i = 0; i < transversalDimensions; i++) {
			momentumSquared -= Math.cos((2.0 * Math.PI * transversalGridPos[i]) / transversalNumCells[i]);
		}

		return momentumSquared / (as * as);
	}
}
