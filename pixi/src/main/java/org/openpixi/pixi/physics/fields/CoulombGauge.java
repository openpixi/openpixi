package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.LinkMatrix;
import org.openpixi.pixi.physics.grid.SU2Field;
import org.openpixi.pixi.physics.grid.SU2Matrix;
import org.openpixi.pixi.physics.grid.YMField;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_3D;

public class CoulombGauge {

	GaugeTransform gaugeTransform = new GaugeTransform();
	CalculateDivergence calculateDivergence = new CalculateDivergence();
	InverseLaplaceOperator inverseLaplaceOperator = new InverseLaplaceOperator();

	Grid gaugedGrid;

	/** Gauge transformation */
	private LinkMatrix[] g;

	private double[][][] fftArray;
	private DoubleFFT_3D fft;

	/**
	 * Constructor. Obtain size of required grid from other grid.
	 * @param grid Grid that should be duplicated in size.
	 */
	public CoulombGauge (Grid grid) {
		gaugedGrid = new Grid(grid);

		if(grid.getNumberOfDimensions() != 3) {
			System.out.println("Coulomb gauge transformation currently only available for 3 dimensions!");
		}
		// JTransform requires twice as many rows in the last column,
		// for real and imaginary parts
		fftArray = new double[grid.getNumCells(0)][grid.getNumCells(1)][2 * grid.getNumCells(2)];

		fft = new DoubleFFT_3D(grid.getNumCells(0), grid.getNumCells(1), grid.getNumCells(2));
	}

	public void fixGauge(Grid grid) {

		// Copy grid
		int numberOfCells = grid.getNumberOfCells();

		int colors = grid.getNumberOfColors();
		if (colors != 2) {System.out.println("Coulomb gauge for SU(" + colors + ") not defined.\n");
			return;
		}

		g = new SU2Matrix[numberOfCells];

		// Reset the gauge transformation
		for (int i = 0; i < g.length; i++) {
			g[i] = new SU2Matrix(1, 0, 0, 0);
		}

		/*
			Copy the U-field.
		 */
		for (int ci = 0; ci < numberOfCells; ci++) {
			int[] cellPosition = grid.getCellPos(ci);
			for (int d = 0; d < grid.getNumberOfDimensions(); d++) {
				LinkMatrix U = grid.getU(cellPosition, d);
				gaugedGrid.setU(cellPosition, d, U);
				YMField E = grid.getE(cellPosition, d);
				gaugedGrid.setE(cellPosition, d, E);
			}
		}

		// TODO: Iterate until required accuracy is reached.
		for (int j = 0; j < 3; j++) {
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

		for (int color = 0; color < gaugedGrid.getNumberOfColors(); color++) {
			// Calculate Divergence and put into fftArray
			calculateDivergence.setColorAndResetSum(0);
			gaugedGrid.getCellIterator().execute(gaugedGrid, calculateDivergence);
			divergenceSquaredSum += calculateDivergence.getDivergenceSquaredSum();

			// Solve Poisson's equation by applying the inverse Laplace operator
			// for discrete lattice derivatives in Fourier space:
			fft.complexForward(fftArray);
			gaugedGrid.getCellIterator().execute(gaugedGrid, inverseLaplaceOperator);
			fft.complexInverse(fftArray, true);

			// Add result to gauge transformation
			for (int i = 0; i < g.length; i++) {
				int[] coor = gaugedGrid.getCellPos(i);

				// real part:
				double value = fftArray[coor[0]][coor[1]][2 * coor[2]];
				g[i].set(color, value);
			}
		}

		// Calculate g(x) = exp(i g psi^\dagger)
		for (int i = 0; i < g.length; i++) {
			// psi is stored in g for convenience:
			SU2Field psidagger = (SU2Field) g[i].adj().proj();
			g[i] = psidagger.getLinkExact();
		}

		/*
			Cycle through each cell and apply the gauge transformation
		 */
		gaugedGrid.getCellIterator().execute(gaugedGrid, gaugeTransform);
		return divergenceSquaredSum;
	}

	public Grid getGaugedGrid() {
		return gaugedGrid;
	}

	private class GaugeTransform implements CellAction {
		public void execute(Grid grid, int[] coor) {
			int cellIndex = grid.getCellIndex(coor);
			for (int dir = 0; dir < grid.getNumberOfDimensions(); dir++) {
				/*
				 * U_i(x) -> g(x) U_i(x) g^\dagger(x+i)
				 */
				LinkMatrix U = grid.getU(coor, dir);
				int shiftedCellIndex = grid.getCellIndex(grid.shift(coor, dir, 1));
				LinkMatrix gdaggershifted = g[shiftedCellIndex].adj();
				U = g[cellIndex].mult(U).mult(gdaggershifted);
				grid.setU(coor, dir, U);

				/*
				 * E_i(x) -> g(x) E_i(x) g^\dagger(x)
				 */
				YMField E = grid.getE(coor, dir);
				LinkMatrix gdagger = g[cellIndex].adj();
				E = (g[cellIndex].mult(E.getLink()).mult(gdagger)).getLinearizedAlgebraElement();
				// TODO: rather work with exact mapping?
				//E = (g[cellIndex].mult(E.getLinkExact()).mult(gdagger)).getAlgebraElement();
				grid.setE(coor, dir, E);
			}
		}
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
				LinkMatrix U = grid.getU(coor, dir);
				LinkMatrix Ushifted = grid.getU(grid.shift(coor, dir, -1), dir);
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
