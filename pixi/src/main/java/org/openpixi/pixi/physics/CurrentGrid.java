package org.openpixi.pixi.physics;

import java.util.ArrayList;


public class CurrentGrid {
	
	public double [][] jx;
	public double [][] jy;
	
	public static int X_BOX = 10;
	public static int Y_BOX = 10;
	
	private double cellWidth;
	private double cellHeight;
	
	//the constructor
	public CurrentGrid(int panelWidth, int panelHeight) {
		
		this.cellWidth = panelWidth / X_BOX;
		this.cellHeight = panelHeight / Y_BOX;
		
		jx = new double[X_BOX][Y_BOX];
		jy = new double[X_BOX][Y_BOX];
		
		for(int i = 0; i < X_BOX; i++)                           //setting the arrays to 0
			for(int k = 0; k < Y_BOX; k++)
			{
				jx[i][k] = 0.0;
				jy[i][k] = 0.0;
			}
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
			jx[xCellPosition][yCellPosition] += par.charge * par.vx;
			jy[xCellPosition][yCellPosition] += par.charge * par.vy;
		}
	}

}
