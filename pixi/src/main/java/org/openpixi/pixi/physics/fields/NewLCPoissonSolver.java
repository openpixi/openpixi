package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.ElementFactory;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.gauge.DoubleFFTWrapper;
import org.apache.commons.math3.special.Erf;
import org.openpixi.pixi.physics.util.GridFunctions;

/**
 * Created by David on 01.09.2015.
 */
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
	}

	public void solve(Simulation s) {
		DoubleFFTWrapper fft = new DoubleFFTWrapper(transversalNumCells);
		// TODO: fix correct setting of Vs
		// TODO: fix shape function (wrong orientation?)

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
			double s0 = - integratedShapeFunction(z, - at / 2.0, orientation, longitudinalWidth);
			double s1 = - integratedShapeFunction(z, + at / 2.0, orientation, longitudinalWidth);

			// Setup the gauge links at t = -dt/2 and t = dt/2
			GroupElement V0 = phi[transversalCellIndex].mult(s0 * g).getLink();
			GroupElement V0next = phi[transversalCellIndex].mult(s1 * g).getLink();
			for (int j = 0; j < numberOfDimensions; j++) {
				if(j != direction) {
					int transversalCellIndexShifted = GridFunctions.getCellIndex(
							GridFunctions.reduceGridPos(
									s.grid.getCellPos(s.grid.shift(i, j, 1))
									, direction),
							transversalNumCells);
					GroupElement V1 = phi[transversalCellIndexShifted].mult(s0 * g).getLink();
					GroupElement V1next = phi[transversalCellIndexShifted].mult(s1 * g).getLink();

					/*
					Equation from the CGC initial condition notes to find the gauge field:
					    A_\mu^a t^a = i/g V (\partial_\mu V)^\dagger.
					In lattice units we replace A_\mu^a by g*as*A_\mu^a. Therefore
					    A_\mu^a t^a = i as V (\partial_\mu V)^\dagger.
					 */

					AlgebraElement A = V0.mult(
							(V1.sub(V0)).mult(1.0/as).adj()
					).proj().mult(as);
					AlgebraElement Anext = V0next.mult(
							(V1next.sub(V0next)).mult(1.0/as).adj()
					).proj().mult(as);

					// "Add" the gauge field by multiplying links.
					s.grid.setU(i, j,
							s.grid.getU(i, j).mult(A.getLink())
					);
					s.grid.setUnext(i, j,
							s.grid.getUnext(i, j).mult(Anext.getLink()));
				}
			}
		}

		// Third step: Compute electric field from temporal plaquette
		for (int i = 0; i < totalCells; i++) {
			for (int j = 0; j < numberOfDimensions; j++) {
				if(j != direction) {
					s.grid.setE(i, j, s.grid.getEFromLinks(i, j));
				}
			}
		}
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
