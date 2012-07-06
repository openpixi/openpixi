package org.openpixi.pixi.physics;

import org.openpixi.pixi.physics.fields.PoissonSolver;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.Interpolator;

import java.util.List;

/**
 * When particles and grid are created they both need to be initialized with the information from
 * the other class.
 */
public class ParticleGridInitializer {
	public void initialize(Interpolator interpolator, PoissonSolver poissonSolver,
	                       List<Particle> particles, Grid grid) {
		interpolator.interpolateChargedensity(particles, grid);
		poissonSolver.solve(grid);
		for (Particle p: particles){
			// Assuming rectangular particle shape i.e. area weighting
			p.setChargedensity(p.getCharge() / (grid.getCellWidth() * grid.getCellHeight()));
		}
	}
}
