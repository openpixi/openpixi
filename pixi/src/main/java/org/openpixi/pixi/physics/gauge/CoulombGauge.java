package org.openpixi.pixi.physics.gauge;

import java.util.ArrayList;
import java.util.List;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.SU2Field;
import org.openpixi.pixi.physics.grid.YMField;

/**
 * Appy the Coulomb gauge transformation to a grid.
 */
public class CoulombGauge extends GaugeTransformation {

	CalculateDivergence calculateDivergence = new CalculateDivergence();
	InverseLaplaceOperator inverseLaplaceOperator = new InverseLaplaceOperator();

	private double[] fftArray;
	private DoubleFFTWrapper fft;

	/**
	 * Maximum number of interations.
	 */
	private int maxIteration = 100;

	/**
	 * Accuracy goal for the transformation.
	 */
	private double accuracyGoal = 1e-18;

	/**
	 * Remember a list of divergence.
	 */
	private List<Double> lastConvergence = null;

	public double getAccuracyGoal() {
		return accuracyGoal;
	}

	public Double[] getLastConvergence() {
		return lastConvergence.toArray(new Double[0]);
	}

	/**
	 * Constructor. Obtain size of required grid from other grid.
	 * @param grid Grid that should be duplicated in size.
	 */
	public CoulombGauge(Grid grid) {
		super(grid);
		fft = new DoubleFFTWrapper(grid.getNumCells());
		fftArray = new double[fft.getFFTArraySize()];
	}

	public void applyGaugeTransformation(Grid grid) {
		int iteration = 0;
		double divergenceSquaredSum = 0;
		lastConvergence = new ArrayList<Double>(maxIteration);
		while (iteration < maxIteration) {
			divergenceSquaredSum = iterateCoulombGauge(grid);
			lastConvergence.add(divergenceSquaredSum);
			iteration++;
			//System.out.println("Iteration " + iteration + " - Divergence U: " + divergenceSquaredSum);
			if (divergenceSquaredSum < accuracyGoal) {
				break;
			}
		}
		//System.out.println("Accuracy goal reached after " + iteration + " iterations.");
		if (divergenceSquaredSum >= accuracyGoal) {
			System.out.println("Warning: accuracy goal NOT reached within " + iteration + " iterations.");
		}
	}

	/**
	 * Performs a single iteration step.
	 * @return previous divergence
	 */
	private double iterateCoulombGauge(Grid grid) {
		double divergenceSquaredSum = 0;

		int colors = grid.getNumberOfColors();

		// New SU2Field array to store psi values
		SU2Field[] psi = new SU2Field[getG().length];
		for (int i = 0; i < getG().length; i++) {
			psi[i] = new SU2Field();
		}

		for (int color = 0; color < colors; color++) {
			// Calculate Divergence and put into fftArray
			calculateDivergence.setColorAndResetSum(color);
			grid.getCellIterator().execute(grid, calculateDivergence);
			divergenceSquaredSum += calculateDivergence.getDivergenceSquaredSum();

			// Solve Poisson's equation by applying the inverse Laplace operator
			// for discrete lattice derivatives in Fourier space:
			fft.complexForward(fftArray);
			grid.getCellIterator().execute(grid, inverseLaplaceOperator);
			fft.complexInverse(fftArray, true);

			// Add result to gauge transformation
			for (int i = 0; i < getG().length; i++) {
				// real part:
				int fftIndex = fft.getFFTArrayIndex(i);
				double value = fftArray[fftIndex];

				// Store values temporarily in SU2Matrix instead of SU2Field:
				//getG()[i].set(color + 1, value);

				psi[i].set(color, value);
			}
		}

		// Calculate g(x) = exp(i g psi^\dagger)
		for (int i = 0; i < getG().length; i++) {
			// psi is stored in g for convenience:
			//SU2Field psidagger = (SU2Field) getG()[i].adj().proj();

			// Field generators are antihermitian so multiply psi by -1 to get psidagger
			SU2Field psidagger = (SU2Field) psi[i].mult(-1);
			getG()[i] = psidagger.getLink();
		}

		/*
			Cycle through each cell and apply the gauge transformation
		 */
		super.applyGaugeTransformation(grid);

		return divergenceSquaredSum;
	}

	private class CalculateDivergence implements CellAction {
		private int color;
		private double divergenceSquaredSum;

		public void setColorAndResetSum(int color) {
			this.color = color;
			divergenceSquaredSum = 0;
		}

		public double getDivergenceSquaredSum() {
			return divergenceSquaredSum;
		}

		public void execute(Grid grid, int index) {
			double divergenceU = 0;
			for (int dir = 0; dir < grid.getNumberOfDimensions(); dir++) {
				/*
				 * U_i(x) - U_i(x-i)
				 */
				YMField U = grid.getU(index, dir).getAlgebraElement();
				YMField Ushifted = grid.getU(grid.shift(index, dir, -1), dir).getAlgebraElement();

				divergenceU += U.get(color) - Ushifted.get(color);
			}
			int fftIndex = fft.getFFTArrayIndex(index);
			fftArray[fftIndex] = divergenceU; // real part
			fftArray[fftIndex + 1] = 0; // imaginary part
			double divergenceUSquared = divergenceU * divergenceU;
			synchronized(this) {
				divergenceSquaredSum += divergenceUSquared;
			}
		}
	}

	private class InverseLaplaceOperator implements CellAction {
		public void execute(Grid grid, int index) {
			int[] coor = grid.getCellPos(index);
			int kx = coor[0];
			int ky = coor[1];
			int kz = coor[2];
			if (kx == 0 && ky == 0 && kz == 0) {
				// zero vector component does not contribute:
				fftArray[0] = 0; // real part
				fftArray[1] = 0; // imaginary part
			} else {
				// Calculate inverse Laplace operator on the lattice for discrete derivatives:
				double Nx = grid.getNumCells(0);
				double Ny = grid.getNumCells(1);
				double Nz = grid.getNumCells(2);

				double inverseLaplace = -0.5 / ((Math.cos(2 * Math.PI * kx / Nx)
						+ Math.cos(2 * Math.PI * ky / Ny)
						+ Math.cos(2 * Math.PI * kz / Nz) - 3.));

				int fftIndex = fft.getFFTArrayIndex(index);
				fftArray[fftIndex] *= inverseLaplace; // real part
				fftArray[fftIndex + 1] *= inverseLaplace; // imaginary part
			}
		}
	}

}
