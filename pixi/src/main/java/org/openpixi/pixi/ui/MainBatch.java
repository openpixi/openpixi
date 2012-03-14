package org.openpixi.pixi.ui;

import org.openpixi.pixi.physics.InitialConditions;
import org.openpixi.pixi.physics.ParticleMover;
import org.openpixi.pixi.physics.Simulation;

public class MainBatch {

	public static final int num_particles = 1000;
	public static final double particle_radius = 0.1;
	/**Total number of timesteps*/
	public static final int steps = 1000;

	public static void main(String[] args) {
		Simulation.setSize(100, 100);
				
		InitialConditions.createRandomParticles(num_particles, particle_radius);
		ParticleMover.prepareAllParticles();
		Simulation.f.bz = 0.001;
		Simulation.f.ex = 0.1;
		
		System.out.println("-------- INITIAL CONDITIONS--------");		
		
		for (int i=0; i < 10; i++) {
			System.out.println(Simulation.particles.get(i).x);	
		}
		
		System.out.println("\n-------- SIMULATION RESULTS --------");			
		
		long start = System.currentTimeMillis();
		
		for (int i = 0; i < steps; i++) {
			Simulation.step();
		}
		
		long elapsed = System.currentTimeMillis()-start;
		
		for (int i=0; i < 10; i++) {
			System.out.println(Simulation.particles.get(i).x);	
		}
		
		System.out.println("\nCurrent: ");
		
		for (int i = 0; i < Simulation.num_cells_x; i++) {
				System.out.println(Simulation.currentGrid.jx[i][0]);
		}
		
		System.out.println("\nCalculation time: "+elapsed);
	}

}
