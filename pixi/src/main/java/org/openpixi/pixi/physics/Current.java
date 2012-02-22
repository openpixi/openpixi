package org.openpixi.pixi.physics;

public class Current {
	
	public double [][] jx;
	public double [][] jy;
	
	public static int X_BOX = 10;
	public static int Y_BOX = 10;
	
	//the constructor
	public Current() {
		
		jx = new double[X_BOX][Y_BOX];
		jy = new double[X_BOX][Y_BOX];
		
		for(int i = 0; i < X_BOX; i++)                           //setting the arrays to 0
			for(int k = 0; k < Y_BOX; k++)
			{
				jx[i][k] = 0.0;
				jy[i][k] = 0.0;
			}
	}

}
