package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.solver.Solver;

public interface ParticleBoundaries {
	void changeType(ParticleBoundaryType boundaryType);

	/**
	 * Uses bounding box around the particle based on particle's radius to determine the region
	 * of the particle. In another words, it enables to reflect the particle based on particle's
	 * radius being outside of simulation area.
	 *
	 * This method is not safe and should be use only when really required!
	 *
	 * !!! IMPORTANT !!!
	 * - Can not be used in distributed simulation because crossing particles
	 *   would be detected too early!
	 * - Can not be used in parallel simulation because the usage of common particle bounding box
	 *   is not thread safe. On the other hand, to create a new bounding box (call constructor)
	 *   for each particle would be thread safe but also slow.
	 */
	void applyOnParticleBoundingBox(Solver solver, Force force, Particle particle, double timeStep);

	/**
	 * We need to pass the solver force and time step so that the hardwall boundary can call
	 * complete and prepare methods from solver.
	 * This solution is very ugly and cumbersome but if we want to have the periodic boundaries
	 * fast we have no other solution.
	 */
	void applyOnParticleCenter(Solver solver, Force force, Particle particle, double timeStep);

	ParticleBoundaryType getType();
}
