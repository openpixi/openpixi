package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.gauge.DoubleFFTWrapper;

public class LightConePoissonSolver {

	private DoubleFFTWrapper fft;
	private int dir;
	private int[] pos;

	public LightConePoissonSolver(int[] position, int direction) {
		this.pos = position;
		this.dir = direction;
	}

	private void solve(Grid g) {

		int[] size = new int[g.getNumberOfDimensions() - 1];
		int k = 0;
		for(int i = 0; i < g.getNumberOfDimensions(); i++) {
			if(i != dir) {
				size[k] = g.getNumCells(i);
				k++;
			} else {}
		}
		fft = new DoubleFFTWrapper(size);

		double norm = Math.pow(g.getLatticeSpacing(), g.getNumberOfDimensions() - 1);

		double[][][] trho = new double[columns][rows][2*depth];
		double[][][] phi = new double[columns][rows][2*depth];

		//prepare input for fft
		for(int i = 0; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				for(int k = 0; k < depth; k++) {
					trho[i][j][2*k] = g.getRho(i,j,k)/cellVolume;
					trho[i][j][2*k+1] = 0;
				}
			}
		}

//perform Fourier transformation
		fft.complexForward(trho);

		for(int i = 0; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				for(int k = 0; k < depth; k++) {

					double d = (6 - 2 * Math.cos((2 * Math.PI * i) / columns) - 2 * Math.cos((2 * Math.PI * j) / rows) - 2 * Math.cos((2 * Math.PI * k) / depth));
					if( (i+j+k) != 0 ) {
						phi[i][j][2*k] = (cellArea * trho[i][j][2*k]) / (d*eps0);
						phi[i][j][2*k+1] = (cellArea * trho[i][j][2*k+1]) / (d*eps0);
					}

				}
			}
		}
		phi[0][0][0] = 0;

		//perform inverse Fourier transform
		fft.complexInverse(phi, true);


		//Solve Poisson equation in Fourier space
		//We omit the term with i,j=0 where d would become 0. This term only contributes a constant term
		//to the potential and can therefore be chosen arbitrarily.
		//the electric field in x direction is equal to the negative derivative of the
		//potential in x direction, analogous for y direction
		//using forward difference, since phi is located in the corner of the grid
		//and the electric field in the edges of the grid
		for(int i = 0; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				for(int k = 0; k < depth; k++) {
					g.setEx(i, j, k, -(phi[(i+1)%g.getNumCellsX()][j][2*k] - phi[i][j][2*k]) / g.getCellWidth() );
					g.setEy(i, j, k, -(phi[i][(j+1)%g.getNumCellsY()][2*k] - phi[i][j][2*k]) / g.getCellHeight() );
					g.setEz(i, j, k, -(phi[i][j][2*((k+1)%g.getNumCellsZ())] - phi[i][j][2*k]) / g.getCellDepth() );
				}
			}
		}

		//prepare output
		for(int i = 0; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				for(int k = 0; k < depth; k++) {
					g.setPhi(i, j, k, phi[i][j][2*k]);
				}
			}
		}

	}

}
