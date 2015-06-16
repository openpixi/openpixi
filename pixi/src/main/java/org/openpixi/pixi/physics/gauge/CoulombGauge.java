package org.openpixi.pixi.physics.gauge;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.LinkMatrix;
import org.openpixi.pixi.physics.grid.SU2Field;
import org.openpixi.pixi.physics.grid.SU2Matrix;
import org.openpixi.pixi.physics.grid.YMField;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_3D;

public class CoulombGauge {

	CalculateDivergence calculateDivergence = new CalculateDivergence();
	InverseLaplaceOperator inverseLaplaceOperator = new InverseLaplaceOperator();

	GaugeTransformation transformation;

	private double[][][] fftArray;
	private DoubleFFT_3D fft;

	/**
	 * Constructor. Obtain size of required grid from other grid.
	 * @param grid Grid that should be duplicated in size.
	 */
	public CoulombGauge(Grid grid) {
		transformation = new GaugeTransformation(grid);

		if(grid.getNumberOfDimensions() != 3) {
			System.out.println("Coulomb gauge transformation currently only available for 3 dimensions!");
		}
		// JTransform requires twice as many rows in the last column,
		// for real and imaginary parts
		fftArray = new double[grid.getNumCells(0)][grid.getNumCells(1)][2 * grid.getNumCells(2)];

		fft = new DoubleFFT_3D(grid.getNumCells(0), grid.getNumCells(1), grid.getNumCells(2));
	}

	public void fixGauge(Grid grid) {
		transformation.copyGrid(grid);

		// TODO: Iterate until required accuracy is reached.
		for (int j = 0; j < 1; j++) {
			double divergenceSquaredSum = iterateCoulombGauge();
			System.out.println("Iteration " + j + " - Divergence U: " + divergenceSquaredSum);
		}
	}

	/**
	 * Performs a single iteration step.
	 * @return previous divergence
	 */
	private double iterateCoulombGauge() {
		double divergenceSquaredSum = 0;

		int colors = transformation.gaugedGrid.getNumberOfColors();
		for (int color = 0; color < colors; color++) {
			// Calculate Divergence and put into fftArray
			calculateDivergence.setColorAndResetSum(color);
			transformation.gaugedGrid.getCellIterator().execute(transformation.gaugedGrid, calculateDivergence);
			divergenceSquaredSum += calculateDivergence.getDivergenceSquaredSum();

			// Solve Poisson's equation by applying the inverse Laplace operator
			// for discrete lattice derivatives in Fourier space:
			fft.complexForward(fftArray);
			transformation.gaugedGrid.getCellIterator().execute(transformation.gaugedGrid, inverseLaplaceOperator);
			fft.complexInverse(fftArray, true);

			// Add result to gauge transformation
			for (int i = 0; i < transformation.g.length; i++) {
				int[] coor = transformation.gaugedGrid.getCellPos(i);

				// real part:
				double value = fftArray[coor[0]][coor[1]][2 * coor[2]];

				// Store values temporarily in SU2Matrix instead of SU2Field:
				transformation.g[i].set(color + 1, value);
			}
		}

		// Calculate g(x) = exp(i g psi^\dagger)
		for (int i = 0; i < transformation.g.length; i++) {
			// psi is stored in g for convenience:
			SU2Field psidagger = (SU2Field) transformation.g[i].adj().proj();
			transformation.g[i] = psidagger.getLinkExact();
		}

		/*
			Cycle through each cell and apply the gauge transformation
		 */
		transformation.applyGaugeTransformation();

		return divergenceSquaredSum;
	}

	public Grid getGaugedGrid() {
		return transformation.gaugedGrid;
	}

	public LinkMatrix[] getGaugeTransformation() {
		return transformation.g;
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
