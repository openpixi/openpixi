package org.openpixi.pixi.physics.fields;

import edu.emory.mathcs.jtransforms.fft.*;
import org.openpixi.pixi.physics.grid.Grid;

public class PoissonSolverFFTPeriodic implements PoissonSolver {
	
	/**Solves the electrostatic Poisson equation with FFT assuming periodic boundaries.
	 * 
	 * <p>This method should be called every time when new particles
	 * are loaded into the simulation area (i.e. a new charge
	 * distribution is introduced) It calculates the electrostatic
	 * potential caused by this distribution, calculates the electric
	 * fields by applying the negative nabla operator and saves them
	 * in the field variables of the Grid class. Note that periodic
	 * boundaries are assumed by the transformation itself AND by the
	 * derivative of the potential!</p>
	 * @param g Grid on which the calculation should be performed
	 */
	public void solve(Grid g) {
		
		//size of the array to be transformed
		int columns = g.getNumCellsX();
		int rows = g.getNumCellsY();
		double cellArea = g.getCellWidth() * g.getCellHeight();
		//JTransform saves the imaginary part as a second row entry
		//therefore there must be twice as many rows
		double[][] trho = new double[columns][2*rows];
		double[][] phi = new double[columns][2*rows];

		DoubleFFT_2D fft = new DoubleFFT_2D(columns, rows);
		
		//prepare input for fft
		for(int i = 0; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				trho[i][2*j] = g.getRho(i,j);
				trho[i][2*j+1] = 0;
			}
		}
		
		//perform Fourier transformation
		fft.complexForward(trho);
		
		//Solve Poisson equation in Fourier space
		//We omit the term with i,j=0 where d would become 0. This term only contributes a constant term
		//to the potential and can therefore be chosen arbitrarily.
		for(int i = 1; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				double d = (4 - 2 * Math.cos((2 * Math.PI * i) / g.getNumCellsX()) - 2 * Math.cos((2 * Math.PI * j) / g.getNumCellsY()));
				phi[i][2*j] = (cellArea * trho[i][2*j]) / d;
				phi[i][2*j+1] = (cellArea * trho[i][2*j+1]) / d;
			}
		}
		//i=0 but j!=0
		for(int j = 1; j < rows; j++) {
			double d = (2 - 2 * Math.cos((2 * Math.PI * j) / g.getNumCellsY()));
			phi[0][2*j] = (cellArea * trho[0][2*j]) / d;
			phi[0][2*j+1] = (cellArea * trho[0][2*j+1]) / d;		
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
				g.setEx(i, j, -(phi[i+1][2*j] - phi[i-1][2*j]) / (2 * g.getCellWidth()));
				g.setEy(i, j, -(phi[i][2*(j+1)] - phi[i][2*(j-1)]) / (2 * g.getCellHeight()));
			}
		}
		
		//lower and upper boundaries
		for(int i = 1; i < columns-1; i++) {
			g.setEx(i, 0, -(phi[i+1][0] - phi[i-1][0]) / (2 * g.getCellWidth()));
			//forward difference
			g.setEy(i, 0, -(phi[i][2] - phi[i][2*(rows-2)]) / (2 * g.getCellHeight()));
			g.setEx(i, rows-1, -(phi[i+1][2*(rows-1)] - phi[i-1][2*(rows-1)]) / (2 * g.getCellWidth()));
			//backward difference
			g.setEy(i, rows-1, -(phi[i][0] - phi[i][2*(rows-2)]) / (2 * g.getCellHeight()));
		}
		
		//left and right boundaries
		for(int j = 1; j < rows-1; j++) {
			//forward difference
			g.setEx(0, j, -(phi[1][2*j] - phi[columns-1][2*j]) / (2 * g.getCellWidth()));
			g.setEy(0, j, -(phi[0][2*(j+1)] - phi[0][2*(j-1)]) / (2 * g.getCellHeight()));
			//backward difference
			g.setEx(columns-1, j, -(phi[0][2*j] - phi[columns-2][2*j]) / (2 * g.getCellWidth()));
			g.setEy(columns-1, j, -(phi[columns-1][2*(j+1)] - phi[columns-1][2*(j-1)]) / (2 * g.getCellHeight()));
		}
		
		//4 boundary points
		g.setEx(0, 0, -(phi[1][0] - phi[0][0]) / (2 * g.getCellWidth()));
		g.setEy(0, 0, -(phi[0][2] - phi[0][0]) / (2 * g.getCellHeight()));
		
		g.setEx(0, rows-1, -(phi[1][2*(rows-1)] - phi[0][2*(rows-1)]) / (2 * g.getCellWidth()));
		g.setEy(0, rows-1, -(phi[0][2*(rows-1)] - phi[0][2*(rows-2)]) / (2 * g.getCellHeight()));
		
		g.setEx(columns-1, rows-1, -(phi[columns-1][2*(rows-1)] - phi[columns-2][2*(rows-1)]) / (2 * g.getCellWidth()));
		g.setEy(columns-1, rows-1, -(phi[columns-1][2*(rows-1)] - phi[columns-1][2*(rows-2)]) / (2 * g.getCellHeight()));		
		
		g.setEx(columns-1, 0, -(phi[columns-1][0] - phi[columns-2][0]) / (2 * g.getCellWidth()));
		g.setEy(columns-1, 0, -(phi[columns-1][2] - phi[columns-1][0]) / (2 * g.getCellHeight()));
		
		//prepare output
		for(int i = 0; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				g.setPhi(i, j, phi[i][2*j]);
			}
		}
		
	}
	
}
