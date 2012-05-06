package org.openpixi.pixi.physics.fields;

import edu.emory.mathcs.jtransforms.fft.*;
import java.math.*;

public class PoissonSolver {
	
	public PoissonSolver() {
		
	}
	
	public static void solve2D(double[][] rho) {
		
		int rows = rho.length;
		int columns = 2 * rho[0].length;
		int n = 0;
		double[][] trho = new double[rows][columns];

		DoubleFFT_2D fft = new DoubleFFT_2D(rows, columns);
		
		//prepare input for fft
		for(int i = 0; i < rows; i++) {
			n = 0;
			for(int j = 0; j < columns; j += 2) {
				trho[i][j] = rho[i][j-n];
				trho[i][j+1] = 0;
				n += 1;
			}
		}
		
		//perform Fourier transformation
		fft.complexForward(trho);
		
		//solve Poisson equation in Fourier space
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j += 2) {
				
			}
		}
		
		
	}
	
}
