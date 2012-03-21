/*
 * OpenPixi - Open Particle-In-Cell (PIC) Simulator
 * Copyright (C) 2012  OpenPixi.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openpixi.pixi.physics;

import java.util.ArrayList;

import org.openpixi.pixi.physics.fields.*;


public class CurrentGrid {
	
	/**Electric current in the middle of a cell in x-Direction*/
	public double [][] jx;
	/**Electric current in the middle of a cell in y-Direction*/
	public double [][] jy;

	/**Electric charge sum of a cell*/
	public double [][] rho;
	
	/**Electric field in x direction*/
	public double [][] Ex;
	/**Electric field in y direction*/
	public double [][] Ey;
	/**Magnetic field in z direction*/
	public double [][] Bz;
	
	public int numCellsX = 10;
	public int numCellsY = 10;
	
	public double cellWidth;
	public double cellHeight;
	
	public Simulation s;
	
	
	//the constructor
	public CurrentGrid(Simulation s) {
		
		this.s = s;
		
		cellWidth = 0;
		cellHeight = 0;
		
		jx = new double[numCellsX+2][numCellsY+2];
		jy = new double[numCellsX+2][numCellsY+2];
		rho = new double[numCellsX+2][numCellsY+2];
		Ex = new double[numCellsX+2][numCellsY+2];
		Ey = new double[numCellsX+2][numCellsY+2];
		Bz = new double[numCellsX+2][numCellsY+2];
		initFields();
	}
	
	//a method to change the dimensions of the cells, i.e. the width and the height
	public void changeDimension(int xbox, int ybox)
	{
		numCellsX = xbox;
		numCellsY = ybox;
		
		jx = new double[numCellsX+2][numCellsY+2];
		jy = new double[numCellsX+2][numCellsY+2];
		rho = new double[numCellsX+2][numCellsY+2];
		Ex = new double[numCellsX+2][numCellsY+2];
		Ey = new double[numCellsX+2][numCellsY+2];
		Bz = new double[numCellsX+2][numCellsY+2];
		initFields();
		
		//this.setGrid() should be called here since changeDimension() can not appear alone. This would cause dualities with MainControlApplet
	}
	
	public void setGrid(double width, double height)
	{
		cellWidth = width / numCellsX;
		cellHeight = height / numCellsY;

		
		//include updateGrid() and the first calculation of Fields here
	}
	
	public void updateGrid(ArrayList<Particle2D> particles)
	{
		reset();
		
		for(Particle2D p : particles)
		{
			int xCellPosition = (int) (p.x / cellWidth) + 1;
			int yCellPosition = (int) (p.y / cellHeight) + 1;
			if(xCellPosition >= numCellsX+1) {
				xCellPosition = numCellsX+1;
			} else if(xCellPosition < 0) {
					xCellPosition = 0;
			}
			if(yCellPosition >= numCellsY+1) {
				yCellPosition = numCellsY+1;
			} else if(yCellPosition < 0) {
				yCellPosition = 0;
			}
			//System.out.println("x: " + xCellPosition + ", y: " + yCellPosition);
			jx[xCellPosition][yCellPosition] += p.charge * p.vx;
			jy[xCellPosition][yCellPosition] += p.charge * p.vy;
			rho[xCellPosition][yCellPosition] += p.charge;
		}
		
		s.fsolver.step(this);		
	}
	
	public int checkCellX(Particle2D p) {
			int xCellPosition = (int) (p.x / cellWidth) + 1;
			if(xCellPosition >= numCellsX+1) {
				xCellPosition = numCellsX+1;
			} else if(xCellPosition < 0) {
					xCellPosition = 0;
			}
			
			return xCellPosition;
	}
	
	public int checkCellY(Particle2D p) {
			int yCellPosition = (int) (p.y / cellHeight) + 1;
			if(yCellPosition >= numCellsY+1) {
				yCellPosition = numCellsY+1;
			} else if(yCellPosition < 0) {
				yCellPosition = 0;
			}
			
			return yCellPosition;
			
	}

	private void reset() {
		for(int i = 0; i < numCellsX + 2; i++) {
			for(int k = 0; k < numCellsY + 2; k++) {
				jx[i][k] = 0.0;
				jy[i][k] = 0.0;
				rho[i][k] = 0.0;
			}
		}
	}
	
	private void initFields() {
		for (int i = 0; i < numCellsX + 2; i++) {
			for (int j = 0; j < numCellsY + 2; j++) {
				Ex[i][j] = 0.0;
				Ey[i][j] = 0.0;
				Bz[i][j] = 0.0;
			}
		}
	}
	
	/*
	//Dirichlet boundary conditions - will be moved to a different class later on.
	private void initPotentialDirichlet() {
		for (int i = 0; i < numCellsX; i++) {
			phi[i][0] = phi[i][numCellsY-1] = 1;
		}
		for (int i = 1; i < numCellsY-1; i++) {
			phi[0][i] = phi[numCellsX-1][i] = 1;
		}
	
		for (int i = 1; i < (numCellsX-1); i++) {
			for (int j = 1; j < (numCellsY-1); j++) {
				phi[i][j] = 0;
			}
		}
	}*/

}
