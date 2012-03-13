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

package org.openpixi.pixi.physics.arraylist;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.arraylist.SimulationArrayList;

public class InterpolatorParticlesGridArrayList {
	
	static double [][] jx = new double[SimulationArrayList.num_cells_x][SimulationArrayList.num_cells_y];
	static double [][] jy = new double[SimulationArrayList.num_cells_x][SimulationArrayList.num_cells_y];
	static double [][] rho = new double[SimulationArrayList.num_cells_x][SimulationArrayList.num_cells_y];
	
	static void interpolateParticlesGrid(int num_particles, ArrayList<Particle2D> particles) {
		
		for (int i = 0; i < SimulationArrayList.num_cells_x; i++) {
			for (int k = 0; k < SimulationArrayList.num_cells_y; k++) {
				jx[i][k] = 0.0;
				jy[i][k] = 0.0;
			}
		}
		
		
		for (int i = 0; i < num_particles; i++) {
			int xCellPosition = (int) (particles.get(i).x / SimulationArrayList.cell_width);
			int yCellPosition = (int) (particles.get(i).y / SimulationArrayList.cell_height);
			if(xCellPosition > (SimulationArrayList.num_cells_x - 1))
				xCellPosition = (SimulationArrayList.num_cells_x - 1);
			if(yCellPosition > (SimulationArrayList.num_cells_y - 1))
				yCellPosition = (SimulationArrayList.num_cells_y - 1);
			if(xCellPosition < 0)
				xCellPosition = 0;
			if(yCellPosition < 0)
				yCellPosition = 0;
			rho[xCellPosition][yCellPosition] += SimulationArrayList.particles.get(i).charge;
			jx[xCellPosition][yCellPosition] += SimulationArrayList.particles.get(i).charge * SimulationArrayList.particles.get(i).vx;
			jy[xCellPosition][yCellPosition] += SimulationArrayList.particles.get(i).charge * SimulationArrayList.particles.get(i).vy;
		}
		
	}

}
