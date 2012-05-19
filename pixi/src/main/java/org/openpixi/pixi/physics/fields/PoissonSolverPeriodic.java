package org.openpixi.pixi.physics.fields;

import edu.emory.mathcs.jtransforms.fft.*;
import org.openpixi.pixi.physics.grid.Grid;

public class PoissonSolverPeriodic {
	
	/**Solves the electrostatic Poisson equation with FFT
	 * 
	 * This method should be called every time when new particles
	 * are loaded into the simulation area (i.e. a new charge
	 * distribution is introduced) It calculates the electrostatic
	 * potential caused by this distribution, calculates the electric
	 * fields by applying the negative nabla operator and saves them
	 * in the field variables of the Grid class
	 * @param g Grid on which the calculation shoudl be performed
	 */
	public void solve(Grid g) {
		
		//size of the array to be transformed
		int columns = g.rho.length;
		int rows = g.rho[0].length;
		double cellArea = g.cellWidth * g.cellHeight;
		//JTransform saves the imaginary part as a second row entry
		//therefore there must be twice as many rows
		double[][] trho = new double[columns][2*rows];
		double[][] phi = new double[columns][2*rows];

		DoubleFFT_2D fft = new DoubleFFT_2D(columns, rows);
		
		//prepare input for fft
		for(int i = 0; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				trho[i][2*j] = g.rho[i][j];
				trho[i][2*j+1] = 0;
			}
		}
		
		//perform Fourier transformation
		fft.complexForward(trho);
		
		//solve Poisson equation in Fourier space
		for(int i = 0; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				double d = (4 - 2 * Math.cos((2 * Math.PI * i) / g.numCellsX) - 2 * Math.cos((2 * Math.PI * j) / g.numCellsY));
				if (d != 0) {
				phi[i][2*j] = (cellArea * trho[i][2*j]) / d;
				phi[i][2*j+1] = (cellArea * trho[i][2*j+1]) / d;
				} else {
					phi[i][2*j] = trho[i][2*j];
					phi[i][2*j+1] = trho[i][2*j+1];
				}
			}
		}
		
		//perform inverse Fourier transform
		fft.complexInverse(phi, true);
		
		//calculate and save electric fields

		//simulation area without boundaries
		for(int i = 1; i < columns-1; i++) {
			for(int j = 1; j < rows-1; j++) {
				//the electric field in x direction is equal to the negative derivative of the 
				//potential in x direction, analogous for y direction
				//using central difference, omitting imaginary part since it should be 0 anyway
				g.Ex[i][j] = -(phi[i+1][2*j] - phi[i-1][2*j]) / (2 * g.cellWidth);
				g.Ey[i][j] = -(phi[i][2*(j+1)] - phi[i][2*(j-1)]) / (2 * g.cellHeight);
			}
		}
		
		//lower and upper boundaries
		for(int i = 1; i < columns-1; i++) {
			g.Ex[i][0] = -(phi[i+1][0] - phi[i-1][0]) / (2 * g.cellWidth);
			//forward difference
			g.Ey[i][0] = -(phi[i][2] - phi[i][0]) / g.cellHeight;
			g.Ex[i][rows-1] = -(phi[i+1][2*(rows-1)] - phi[i-1][2*(rows-1)]) / (2 * g.cellWidth);
			//backward difference
			g.Ey[i][rows-1] = -(phi[i][2*(rows-1)] - phi[i][2*(rows-2)]) / g.cellHeight;
		}
		
		//left and right boundaries
		for(int j = 1; j < rows-1; j++) {
			//forward difference
			g.Ex[0][j] = -(phi[1][2*j] - phi[0][2*j]) / g.cellWidth;
			g.Ey[0][j] = -(phi[0][2*(j+1)] - phi[0][2*(j-1)]) / (2 * g.cellHeight);
			//backward difference
			g.Ex[columns-1][j] = -(phi[columns-1][2*j] - phi[columns-2][2*j]) / g.cellWidth;
			g.Ey[columns-1][j] = -(phi[columns-1][2*(j+1)] - phi[columns-1][2*(j-1)]) / (2 * g.cellHeight);
		}
		
		//4 boundary points
		g.Ex[0][0] = -(phi[1][0] - phi[0][0]) / g.cellWidth;
		g.Ey[0][0] = -(phi[0][2] - phi[0][0]) / g.cellHeight;
		
		g.Ex[0][rows-1] = -(phi[1][2*(rows-1)] - phi[0][2*(rows-1)]) / g.cellWidth;
		g.Ey[0][rows-1] = -(phi[0][2*(rows-1)] - phi[0][2*(rows-2)]) / g.cellHeight;
		
		g.Ex[columns-1][rows-1] = -(phi[columns-1][2*(rows-1)] - phi[columns-2][2*(rows-1)]) / g.cellWidth;
		g.Ey[columns-1][rows-1] = -(phi[columns-1][2*(rows-1)] - phi[columns-1][2*(rows-2)]) / g.cellHeight;		
		
		g.Ex[columns-1][0] = -(phi[columns-1][0] - phi[columns-2][0]) / g.cellWidth;
		g.Ey[columns-1][0] = -(phi[columns-1][2] - phi[columns-1][0]) / g.cellHeight;
		
		//prepare output
		for(int i = 0; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				g.phi[i][j] = phi[i][2*j];
			}
		}
		
	}
	
}
