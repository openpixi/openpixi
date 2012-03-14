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

import org.openpixi.pixi.physics.Simulation;

public class InterpolatorParticlesGrid {
	
	static double [][] jx = new double[Simulation.num_cells_x][Simulation.num_cells_y];
	static double [][] jy = new double[Simulation.num_cells_x][Simulation.num_cells_y];
	static double [][] rho = new double[Simulation.num_cells_x][Simulation.num_cells_y];
	
	static void interpolateParticlesGrid(int num_particles, ArrayList<Particle2D> particles) {
		
		for (int i = 0; i < Simulation.num_cells_x; i++) {
			for (int k = 0; k < Simulation.num_cells_y; k++) {
				jx[i][k] = 0.0;
				jy[i][k] = 0.0;
			}
		}
		
		
		for (int i = 0; i < num_particles; i++) {
			int xCellPosition = (int) (particles.get(i).x / Simulation.cell_width);
			int yCellPosition = (int) (particles.get(i).y / Simulation.cell_height);
			if(xCellPosition > (Simulation.num_cells_x - 1))
				xCellPosition = (Simulation.num_cells_x - 1);
			if(yCellPosition > (Simulation.num_cells_y - 1))
				yCellPosition = (Simulation.num_cells_y - 1);
			if(xCellPosition < 0)
				xCellPosition = 0;
			if(yCellPosition < 0)
				yCellPosition = 0;
			rho[xCellPosition][yCellPosition] += Simulation.particles.get(i).charge;
			jx[xCellPosition][yCellPosition] += Simulation.particles.get(i).charge * Simulation.particles.get(i).vx;
			jy[xCellPosition][yCellPosition] += Simulation.particles.get(i).charge * Simulation.particles.get(i).vy;
		}
		
	}

}
