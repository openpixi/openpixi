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
package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.*;


public class SimpleGrid extends Grid {

	public SimpleGrid(Simulation s) {
		
		super(s);
		interp = new CloudInCell();
		
		cellWidth = 0;
		cellHeight = 0;
		
		jx = new double[numCellsX+3][numCellsY+3];
		jy = new double[numCellsX+3][numCellsY+3];
		rho = new double[numCellsX+3][numCellsY+3];
		Ex = new double[numCellsX+3][numCellsY+3];
		Ey = new double[numCellsX+3][numCellsY+3];
		Bz = new double[numCellsX+3][numCellsY+3];
		initFields();
	}
	
	//a method to change the dimensions of the cells, i.e. the width and the height
	public void changeDimension(int xbox, int ybox)
	{
		numCellsX = xbox;
		numCellsY = ybox;
		
		jx = new double[numCellsX+3][numCellsY+3];
		jy = new double[numCellsX+3][numCellsY+3];
		rho = new double[numCellsX+3][numCellsY+3];
		Ex = new double[numCellsX+3][numCellsY+3];
		Ey = new double[numCellsX+3][numCellsY+3];
		Bz = new double[numCellsX+3][numCellsY+3];
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
		interp.interpolateToGrid(particles, this);		
		s.fsolver.step(this);		
	}
	
	public double[] interpolateToParticle(Particle2D p) {
		
		double[] fields = new double[3];
		int xCellPosition = (int) (p.x / cellWidth) + 1;
		int yCellPosition = (int) (p.y / cellHeight) + 1;
		
		int xCellPosition2 = xCellPosition;
		int yCellPosition2 = yCellPosition;
		
		if(xCellPosition >= numCellsX + 1) {
			xCellPosition = numCellsX + 1;
		} else if(xCellPosition < 1) {
				xCellPosition = 1;
		}
		if(yCellPosition >= numCellsY + 1) {
			yCellPosition = numCellsY + 1;
		} else if(yCellPosition < 1) {
			yCellPosition = 1;
		}
		
		fields[0] = ( Ex[xCellPosition][yCellPosition] * (xCellPosition2 * cellWidth - p.x) *
				(yCellPosition2 * cellHeight - p.y) +
				Ex[xCellPosition + 1][yCellPosition] * (p.x - (xCellPosition2 - 1) * cellWidth) *
				(yCellPosition2 * cellHeight - p.y) +
				Ex[xCellPosition][yCellPosition + 1] * (xCellPosition2 * cellWidth - p.x) *
				(p.y - (yCellPosition2 - 1) * cellHeight) +
				Ex[xCellPosition + 1][yCellPosition + 1] * (p.x - (xCellPosition2 - 1) * cellWidth) *
				(p.y - (yCellPosition2 - 1) * cellHeight) ) / (cellWidth * cellHeight);
		
		fields[1] = ( Ey[xCellPosition][yCellPosition] * (xCellPosition2 * cellWidth - p.x) *
				(yCellPosition2 * cellHeight - p.y) +
				Ey[xCellPosition + 1][yCellPosition] * (p.x - (xCellPosition2 - 1) * cellWidth) *
				(yCellPosition2 * cellHeight - p.y) +
				Ey[xCellPosition][yCellPosition + 1] * (xCellPosition2 * cellWidth - p.x) *
				(p.y - (yCellPosition2 - 1) * cellHeight) +
				Ey[xCellPosition + 1][yCellPosition + 1] * (p.x - (xCellPosition2 - 1) * cellWidth) *
				(p.y - (yCellPosition2 - 1) * cellHeight) ) / (cellWidth * cellHeight);
		
		fields[2] = ( Bz[xCellPosition][yCellPosition] * (xCellPosition2 * cellWidth - p.x) *
				(yCellPosition2 * cellHeight - p.y) +
				Bz[xCellPosition + 1][yCellPosition] * (p.x - (xCellPosition2 - 1) * cellWidth) *
				(yCellPosition2 * cellHeight - p.y) +
				Bz[xCellPosition][yCellPosition + 1] * (xCellPosition2 * cellWidth - p.x) *
				(p.y - (yCellPosition2 - 1) * cellHeight) +
				Bz[xCellPosition + 1][yCellPosition + 1] * (p.x - (xCellPosition2 -1) * cellWidth) *
				(p.y - (yCellPosition2 -1) * cellHeight) ) / (cellWidth * cellHeight);
		
		return fields;	
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
		for(int i = 0; i < numCellsX + 3; i++) {
			for(int k = 0; k < numCellsY + 3; k++) {
				jx[i][k] = 0.0;
				jy[i][k] = 0.0;
				rho[i][k] = 0.0;
			}
		}
	}
	
	private void initFields() {
		for (int i = 0; i < numCellsX + 3; i++) {
			for (int j = 0; j < numCellsY + 3; j++) {
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
