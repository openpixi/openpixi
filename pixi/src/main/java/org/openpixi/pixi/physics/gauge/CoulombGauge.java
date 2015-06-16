package org.openpixi.pixi.physics.gauge;

import java.util.ArrayList;
import java.util.List;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.SU2Field;
import org.openpixi.pixi.physics.grid.YMField;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_3D;

public class CoulombGauge extends GaugeTransformation {

	CalculateDivergence calculateDivergence = new CalculateDivergence();
	InverseLaplaceOperator inverseLaplaceOperator = new InverseLaplaceOperator();

	private double[][][] fftArray;
	private DoubleFFT_3D fft;

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

		if(grid.getNumberOfDimensions() != 3) {
			System.out.println("Coulomb gauge transformation currently only available for 3 dimensions!");
		}
		// JTransform requires twice as many rows in the last column,
		// for real and imaginary parts
		fftArray = new double[grid.getNumCells(0)][grid.getNumCells(1)][2 * grid.getNumCells(2)];

		fft = new DoubleFFT_3D(grid.getNumCells(0), grid.getNumCells(1), grid.getNumCells(2));
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
				int[] coor = grid.getCellPos(i);

				// real part:
				double value = fftArray[coor[0]][coor[1]][2 * coor[2]];

				// Store values temporarily in SU2Matrix instead of SU2Field:
				getG()[i].set(color + 1, value);
			}
		}

		// Calculate g(x) = exp(i g psi^\dagger)
		for (int i = 0; i < getG().length; i++) {
			// psi is stored in g for convenience:
			SU2Field psidagger = (SU2Field) getG()[i].adj().proj();
			getG()[i] = psidagger.getLinkExact();
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

		public void execute(Grid grid, int[] coor) {
			double divergenceU = 0;
			for (int dir = 0; dir < grid.getNumberOfDimensions(); dir++) {
				/*
				 * U_i(x) - U_i(x-i)
				 */
				YMField U = grid.getU(coor, dir).getAlgebraElement();
				YMField Ushifted = grid.getU(grid.shift(coor, dir, -1), dir).getAlgebraElement();

				divergenceU += U.get(color) - Ushifted.get(color);
			}
			fftArray[coor[0]][coor[1]][2 * coor[2]] = divergenceU; // real part
			fftArray[coor[0]][coor[1]][2 * coor[2] + 1] = 0; // imaginary part
			double divergenceUSquared = divergenceU * divergenceU;
			synchronized(this) {
				divergenceSquaredSum += divergenceUSquared;
			}
		}
	}

	private class InverseLaplaceOperator implements CellAction {
		public void execute(Grid grid, int[] coor) {
			int kx = coor[0];
			int ky = coor[1];
			int kz = coor[2];
			if (kx == 0 && ky == 0 && kz == 0) {
				// zero vector component does not contribute:
				fftArray[0][0][0] = 0; // real part
				fftArray[0][0][1] = 0; // imaginary part
			} else {
				// Calculate inverse Laplace operator on the lattice for discrete derivatives:
				double Nx = grid.getNumCells(0);
				double Ny = grid.getNumCells(1);
				double Nz = grid.getNumCells(2);

				double inverseLaplace = -0.5 / ((Math.cos(2 * Math.PI * kx / Nx)
						+ Math.cos(2 * Math.PI * ky / Ny)
						+ Math.cos(2 * Math.PI * kz / Nz) - 3.));

				// Real part:
				fftArray[coor[0]][coor[1]][2 * coor[2]] *= inverseLaplace;

				// Imaginary part:
				fftArray[coor[0]][coor[1]][2 * coor[2] + 1] *= inverseLaplace;
			}
		}
	}

}
