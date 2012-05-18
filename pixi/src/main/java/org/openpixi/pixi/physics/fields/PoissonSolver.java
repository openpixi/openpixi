package org.openpixi.pixi.physics.fields;

import edu.emory.mathcs.jtransforms.fft.*;
import org.openpixi.pixi.physics.grid.Grid;
import java.math.*;

public class PoissonSolver {
	
	/**Solves the electrostatic Poisson equation with FFT
	 * 
	 * This method should be called every time when new particles
	 * are loaded into the simulation area (i.e. a new charge
	 * distribution is introduced) It calculates the electrostatic
	 * potential caused by this distribution, calculates the electric
	 * fields by applying the negative nabla operator and saves them
	 * in the field variables of the Grid class
	 * @param g
	 */
	public static void solve2D(Grid g) {
		
		int rows = g.rho.length;
		int columns = 2 * g.rho[0].length;
		int n = 0;
		double[][] trho = new double[rows][columns];
		double[][] phi = new double[rows][columns];

		DoubleFFT_2D fft = new DoubleFFT_2D(rows, columns/2);
		
		//prepare input for fft
		for(int i = 0; i < rows; i++) {
			n = 0;
			for(int j = 0; j < columns; j += 2) {
				trho[i][j] = g.rho[i][j-n];
				trho[i][j+1] = 0;
				n += 1;
			}
		}
		
		//perform Fourier transformation
		fft.complexForward(trho);
		
		//solve Poisson equation in Fourier space
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j += 2) {
				double d = (4 - 2 * Math.cos((2 * Math.PI * i) / g.numCellsX) - 2 * Math.cos((2 * Math.PI * (j/2)) / g.numCellsY));
				if (d != 0) {
				phi[i][j] = (g.cellWidth * g.cellHeight * trho[i][j]) / d;						;
//				phi[i][j+1] = (g.cellWidth * g.cellHeight * trho[i][j+1]) / d;
				} else {
					phi[i][j] = trho[i][j];
					phi[i][j+1] = trho[i][j+1];
				}
//				System.out.println(trho[i][j] + " " + trho[i][j+1]);
			}
		}
		
		//perform inverse Fourier transform
		fft.complexInverse(phi, true);
		
		for(int i = 0; i < rows; i++) {
			n = 0;
			for(int j = 0; j < columns; j += 2) {
				System.out.println(phi[i][j] + " " + phi[i][j+1]);
			}
		}
		
		//prepare output
		for(int i = 0; i < rows; i++) {
			n = 0;
			for(int j = 0; j < columns; j += 2) {
				g.phi[i][j-n] = phi[i][j];
				n += 1;
			}
		}
		
	}
	
}
