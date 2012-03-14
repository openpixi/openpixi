package org.openpixi.pixi.ui;

import org.openpixi.pixi.physics.InterpolatorParticlesGrid;
import org.openpixi.pixi.physics.ParticleMover;
import org.openpixi.pixi.physics.Simulation;

public class MainBatch {

	public static void main(String[] args) {
				
		Simulation.boundary.xmin = 0;
		Simulation.boundary.xmax = (double) Simulation.width;
		Simulation.boundary.ymin = 0;
		Simulation.boundary.ymax = (double) Simulation.height;
				
		Simulation.CreateParticles(Simulation.num_particles, Simulation.particle_radius);
		Simulation.f.bz = 0.001;
		Simulation.f.ex = 0.1;
		
		System.out.println("-------- INITIAL CONDITIONS--------");		
		
		for (int i=0; i < 10; i++) {
			System.out.println(Simulation.particles.get(i).x);	
		}
		
		System.out.println("\n-------- SIMULATION RESULTS --------");			
		
		long start = System.currentTimeMillis();
		
		for (int i = 0; i < Simulation.steps; i++) {
			ParticleMover.particlePush(Simulation.num_particles);
			InterpolatorParticlesGrid.interpolateParticlesGrid(Simulation.num_particles, Simulation.particles);
		}
		
		long elapsed = System.currentTimeMillis()-start;
		
		for (int i=0; i < 10; i++) {
			System.out.println(Simulation.particles.get(i).x);	
		}
		
		System.out.println("\nCurrent: ");
		
		for (int i = 0; i < Simulation.num_cells_x; i++) {
				System.out.println(InterpolatorParticlesGrid.jx[i][0]);
		}
		
		System.out.println("\nCalculation time: "+elapsed);
	}

}
