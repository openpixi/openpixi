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
	
	public double [][] jx;
	public double [][] jy;
	
	public static int X_BOX = 10;
	public static int Y_BOX = 10;
	
	public int cellWidth;
	public int cellHeight;
	
	//the constructor
	public CurrentGrid() {
	
		this.cellWidth = 0;
		this.cellHeight = 0;
		
		jx = new double[X_BOX][Y_BOX];
		jy = new double[X_BOX][Y_BOX];
		
		for(int i = 0; i < X_BOX; i++)                           //setting the arrays to 0
			for(int k = 0; k < Y_BOX; k++)
			{
				jx[i][k] = 0.0;
				jy[i][k] = 0.0;
			}
	}
	
	public void setGrid(int panelWidth, int panelHeight)
	{
		this.cellWidth = panelWidth / X_BOX;
		this.cellHeight = panelHeight / Y_BOX;
	}
	
	public void updateGrid(ArrayList<Particle2D> parlist)
	{
		for(int i = 0; i < X_BOX; i++)
			for(int k = 0; k < Y_BOX; k++)
			{
				jx[i][k] = 0.0;
				jy[i][k] = 0.0;
			}
		
		for(int i = 0; i < parlist.size(); i++)
		{
			Particle2D par = (Particle2D) parlist.get(i);
			int xCellPosition = (int) (par.x / cellWidth);
			int yCellPosition = (int) (par.y / cellHeight);
			if(xCellPosition > 9)
				xCellPosition = 9;
			if(yCellPosition > 9)
				yCellPosition = 9;
			if(xCellPosition < 0)
				xCellPosition = 0;
			if(yCellPosition < 0)
				yCellPosition = 0;
			//System.out.println("x: " + xCellPosition + ", y: " + yCellPosition);
			jx[xCellPosition][yCellPosition] += par.charge * par.vx;
			jy[xCellPosition][yCellPosition] += par.charge * par.vy;
		}
		
	}

}
