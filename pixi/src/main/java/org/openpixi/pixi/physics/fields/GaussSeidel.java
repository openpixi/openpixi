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
package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.physics.ConstantsSI;

public class GaussSeidel extends FieldSolver {
	
	private double change;
	private double delta;
	private double [][] previous;
	
	public GaussSeidel() {
		
	}

	public void step(double [][] phi, double [][] b, int nx, int ny, double dx, double dy) {
		
		/*previous = new double[nx][ny];
		for (int i = 1; i < nx-1; i++) {
			for (int j = 1; j < ny-1; j++) {
				previous[i][j] = 0;
			}
		}
		System.out.println(previous[3] == phi[3]);
		System.out.println(previous[3][4]);
		System.out.println(phi[0][0]+"\n");
		
		//CAUTION:! Dirichlet boundary conditions implied! Potentials calculated in the middle of cells, not on grid points!
		for (int iteration = 0; iteration < 1; iteration++){
			
			delta = 0;
			
			for (int i = 0; i < nx; i++) {
				for (int j = 0; j < ny; j++) {
					previous[i][j] = phi[i][j];
				}
			}
			
			/*for (int i = 0; i < nx; i++) {
					previous[i] = phi[i].clone();
			}*/
			/*
			for (int i = 1; i < nx-1; i++) {
				for (int j = 1; j < ny-1; j++) {
					phi[i][j] = (dx*dx+dy*dy)*(b[i][j]+(phi[i+1][j]+phi[i-1][j])/(dx*dx)+(phi[i][j+1]+phi[i][j-1])/(dy*dy))/2;
					//delta += previous[i][j]*previous[i][j]-phi[i][j]*phi[i][j];
				}
			}
			
			//change = Math.sqrt(delta);
			
			if (iteration > 2) {
				for (int i = 1; i < nx-1; i++) {
					for (int j = 1; j < ny-1; j++) {
						delta += (previous[i][j]*previous[i][j]-phi[i][j]*phi[i][j]);
					}
				}
				
				change = Math.sqrt(Math.abs(delta));
			
				if (change < 0.1) {
					break;
				}
			}
			
			System.out.println(previous[3][4]);
			System.out.println(phi[3][4]);
			System.out.println(change+"\n");
		}
		
		System.out.println(previous[3][4] == phi[3][4]);*/
	}
	
}
