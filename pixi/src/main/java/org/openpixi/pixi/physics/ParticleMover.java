package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.solver.*;

public class ParticleMover {

	/**Contains current solver algorithm*/
	static Solver solver = new Boris();
	
	static void particlePush(int num_particles) {
		
		for (int i = 0; i < num_particles; i++) {
			ParticleMover.solver.step(Simulation.particles[i], Simulation.f, Simulation.tstep);
			Simulation.boundary.check(Simulation.particles[i], Simulation.f, ParticleMover.solver, Simulation.tstep);
		}
		
	}

}
