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


public class CurrentGrid {
	
	/**Electric current in the middle of a cell in x-Direction*/
	public double [][] jx;
	/**Electric current in the middle of a cell in y-Direction*/
	public double [][] jy;

	/**Electric charge sum of a cell*/
	public double [][] rho;
	
	/**Electric potential for each cell*/
	public double [][] phi;
	
	public int numCellsX = 10;
	public int numCellsY = 10;
	
	public double cellWidth;
	public double cellHeight;
	
	//the constructor
	public CurrentGrid() {
	
		this.cellWidth = 0;
		this.cellHeight = 0;
		
		jx = new double[numCellsX][numCellsY];
		jy = new double[numCellsX][numCellsY];
		rho = new double[numCellsX][numCellsY];
		phi = new double[numCellsX][numCellsY];
	}
	
	//a method to change the dimensions of the cells, i.e. the width and the height
	public void changeDimension(int xbox, int ybox)
	{
		numCellsX = xbox;
		numCellsY = ybox;
		
		jx = new double[numCellsX][numCellsY];
		jy = new double[numCellsX][numCellsY];
		rho = new double[numCellsX][numCellsY];
	}
	
	public void setGrid(double width, double height)
	{
		this.cellWidth = width / numCellsX;
		this.cellHeight = height / numCellsY;
	}
	
	public void updateGrid(ArrayList<Particle2D> particles)
	{
		reset();
		
		for(Particle2D p : particles)
		{
			int xCellPosition = (int) (p.x / cellWidth);
			int yCellPosition = (int) (p.y / cellHeight);
			if(xCellPosition >= numCellsX) {
				xCellPosition = numCellsX - 1;
			} else if(xCellPosition < 0) {
					xCellPosition = 0;
			}
			if(yCellPosition >= numCellsY) {
				yCellPosition = numCellsY - 1;
			} else if(yCellPosition < 0) {
				yCellPosition = 0;
			}
			//System.out.println("x: " + xCellPosition + ", y: " + yCellPosition);
			jx[xCellPosition][yCellPosition] += p.charge * p.vx;
			jy[xCellPosition][yCellPosition] += p.charge * p.vy;
			rho[xCellPosition][yCellPosition] += p.charge;
		}
		
	}

	private void reset() {
		for(int i = 0; i < numCellsX; i++) {
			for(int k = 0; k < numCellsY; k++) {
				jx[i][k] = 0.0;
				jy[i][k] = 0.0;
				rho[i][k] = 0.0;
			}
		}
	}

}
