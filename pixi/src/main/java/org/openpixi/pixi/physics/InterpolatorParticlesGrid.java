package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.Simulation;

public class InterpolatorParticlesGrid {
	
	static double [][] jx = new double[Simulation.num_cells_x][Simulation.num_cells_y];
	static double [][] jy = new double[Simulation.num_cells_x][Simulation.num_cells_y];
	static void interpolateParticlesGrid(int num_particles, Particle2D [] particles) {
		
		for (int i = 0; i < Simulation.num_cells_x; i++) {
			for (int k = 0; k < Simulation.num_cells_y; k++) {
				jx[i][k] = 0.0;
				jy[i][k] = 0.0;
			}
		}
		
		
		for (int i = 0; i < num_particles; i++) {
			int xCellPosition = (int) (particles[i].x / Simulation.cell_width);
			int yCellPosition = (int) (particles[i].y / Simulation.cell_height);
			if(xCellPosition > (Simulation.num_cells_x - 1))
				xCellPosition = (Simulation.num_cells_x - 1);
			if(yCellPosition > (Simulation.num_cells_y - 1))
				yCellPosition = (Simulation.num_cells_y - 1);
			if(xCellPosition < 0)
				xCellPosition = 0;
			if(yCellPosition < 0)
				yCellPosition = 0;
			jx[xCellPosition][yCellPosition] += Simulation.particles[i].charge * Simulation.particles[i].vx;
			jy[xCellPosition][yCellPosition] += Simulation.particles[i].charge * Simulation.particles[i].vy;
		}
		
	}

}
