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
		int depth = g.getNumCellsZ();
		
		if( (columns == 1) || (rows == 1) ) {
			solve1D(g);
			return;
		}
		
		if(depth != 1) {
			solve3D(g);
			return;
		}
		
        double eps0 = 1.0/(4*Math.PI);
        //double eps0 = 1;
        
		double cellArea = g.getCellWidth() * g.getCellHeight();
		//JTransform saves the imaginary part as a second row entry
		//therefore there must be twice as many rows
		double[][] trho = new double[columns][2*rows];
		double[][] phi = new double[columns][2*rows];

		DoubleFFT_2D fft = new DoubleFFT_2D(columns, rows);
		
		//prepare input for fft
		for(int i = 0; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				trho[i][2*j] = g.getRho(i,j)/cellArea;
				trho[i][2*j+1] = 0;
			}
		}
		
		//perform Fourier transformation
		fft.complexForward(trho);
		
                for(int i = 1; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				double d = (4 - 2 * Math.cos((2 * Math.PI * i) / g.getNumCellsX()) - 2 * Math.cos((2 * Math.PI * j) / g.getNumCellsY()));
				phi[i][2*j] = (cellArea * trho[i][2*j]) / (d*eps0);
				phi[i][2*j+1] = (cellArea * trho[i][2*j+1]) / (d*eps0);
			}
		}
		//i=0 but j!=0
		for(int j = 1; j < rows; j++) {
			double d = (2 - 2 * Math.cos((2 * Math.PI * j) / g.getNumCellsY()));
			phi[0][2*j] = (cellArea * trho[0][2*j]) / (d * eps0);
			phi[0][2*j+1] = (cellArea * trho[0][2*j+1]) / (d * eps0);		
		}
                phi[0][0] = 0;
                phi[0][1] = 0;

		//perform inverse Fourier transform
		fft.complexInverse(phi, true);
                
                
		//Solve Poisson equation in Fourier space
		//We omit the term with i,j=0 where d would become 0. This term only contributes a constant term
		//to the potential and can therefore be chosen arbitrarily.
		for(int i = 0; i < columns-1; i++) {
			for(int j = 0; j < rows-1; j++) {
				//the electric field in x direction is equal to the negative derivative of the 
				//potential in x direction, analogous for y direction
				//using forward difference, since phi is located in the corner of the grid
                                //and the electric field in the edges of the grid
				g.setEx(i, j, -(phi[i+1][2*j] - phi[i][2*j]) / (g.getCellWidth()));
				g.setEy(i, j, -(phi[i][2*(j+1)] - phi[i][2*j]) / (g.getCellHeight()));
			}
		}
		
		//upper boundary
		for(int i = 0; i < columns-1; i++) {
			g.setEx(i, rows-1, -(phi[i+1][2*(rows-1)] - phi[i][2*(rows-1)]) / (g.getCellWidth()));
			g.setEy(i, rows-1, -(phi[i][0] - phi[i][2*(rows-1)]) / (g.getCellHeight()));
		}
		
		//right boundary
		for(int j = 0; j < rows-1; j++) {
			g.setEx(columns-1, j, -(phi[0][2*j] - phi[columns-1][2*j]) / (g.getCellWidth()));
			g.setEy(columns-1, j, -(phi[columns-1][2*(j+1)] - phi[columns-1][2*j]) / (g.getCellHeight()));
		}
		
		//upper right corner
		g.setEx(columns-1, rows-1, -(phi[0][2*(rows-1)] - phi[columns-1][2*(rows-1)]) / (g.getCellWidth()));
		g.setEy(columns-1, rows-1, -(phi[columns-1][0] - phi[columns-1][2*(rows-1)]) / (g.getCellHeight()));		
		
		//prepare output
		for(int i = 0; i < columns; i++) {
			for(int j = 0; j < rows; j++) {
				g.setPhi(i, j, phi[i][2*j]);
			}
		}
		
	}
	
	private void solve1D(Grid g) {
		
        double eps0 = 1.0/(4*Math.PI);
    
        //size of the array to be transformed
        int columns = g.getNumCellsX();
        int rows = g.getNumCellsY();
        int length = Math.max(columns, rows);


        double cellLength = g.getCellWidth();
        //JTransform saves the imaginary part as a second row entry
        //therefore there must be twice as many rows
        double[] trho = new double[2*length];
        double[] phi = new double[2*length];

        DoubleFFT_1D fft = new DoubleFFT_1D(length);

        //prepare input for fft
        for(int i = 0; i < length; i++) {      
        		trho[2*i] = g.getRho(i,0)/cellLength;//Assumes that the only dimension is the x-axis!!
        		trho[2*i+1] = 0;
        }

//perform Fourier transformation
fft.complexForward(trho);

	for(int i = 1; i < length; i++) {
		double d = ( 2 - 2 * Math.cos((2 * Math.PI * i) / g.getNumCellsX()) );
		phi[2*i] = (cellLength * cellLength * trho[2*i]) / (d*eps0);
		phi[2*i+1] = (cellLength * cellLength * trho[2*i+1]) / (d*eps0);
	}
        phi[0] = 0;

        //perform inverse Fourier transform
        fft.complexInverse(phi, true);
        
        
        //Solve Poisson equation in Fourier space
        //We omit the term with i,j=0 where d would become 0. This term only contributes a constant term
        //to the potential and can therefore be chosen arbitrarily.
        for(int i = 0; i < length; i++) {
        		//the electric field in x direction is equal to the negative derivative of the 
        		//potential in x direction, analogous for y direction
        		//using forward difference, since phi is located in the corner of the grid
                        	//and the electric field in the edges of the grid
        		g.setEx(i, 0, -(phi[2*( (i+1+g.getNumCellsX())%g.getNumCellsX() )] - phi[2*i]) / (g.getCellWidth()));
        }	

        //prepare output
			for(int i = 0; i < length; i++) {
					g.setPhi(i, 0, phi[2*i]);
			}

	}
	
private void solve3D(Grid g) {
		
        double eps0 = 1.0/(4*Math.PI);
    
        //size of the array to be transformed
        int columns = g.getNumCellsX();
        int rows = g.getNumCellsY();
        int depth = g.getNumCellsZ();


        double cellArea = g.getCellWidth() * g.getCellHeight();
        double cellVolume = cellArea * g.getCellDepth();
        //JTransform saves the imaginary part as a second row entry
        //therefore there must be twice as many rows
        double[][][] trho = new double[columns][rows][2*depth];
        double[][][] phi = new double[columns][rows][2*depth];

        DoubleFFT_3D fft = new DoubleFFT_3D(columns, rows, depth);

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

	for(int i = 1; i < columns; i++) {
		for(int j = 1; j < rows; j++) {
			for(int k = 1; k < depth; k++) {
				double d = (6 - 2 * Math.cos((2 * Math.PI * i) / columns) - 2 * Math.cos((2 * Math.PI * j) / rows) - 2 * Math.cos((2 * Math.PI * k) / depth));
				phi[i][j][2*k] = (cellArea * trho[i][j][2*k]) / (d*eps0);
				phi[i][j][2*k+1] = (cellArea * trho[i][j][2*k+1]) / (d*eps0);
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

