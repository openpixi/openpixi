package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.fields.PoissonSolver;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.Interpolation;
import org.openpixi.pixi.physics.particles.IParticle;

import java.util.List;

/**
 * When particles and grid are created they both need to be initialized with the information from
 * the other class.
 */
public class ParticleGridInitializer {
	public void initialize(Interpolation interpolation, PoissonSolver poissonSolver,
	                       List<IParticle> particles, Grid grid) {
		interpolation.interpolateChargedensity(particles, grid);
		poissonSolver.solve(grid);
	}
}
