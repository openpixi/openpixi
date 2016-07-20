package org.openpixi.pixi.physics.initial.CGC;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.initial.IInitialCondition;

/**
 * This abstract class implements the common steps for CGC initial conditions:
 *      1) Initialize/load charge density. This depends on the particular CGC model.
 *      2) Solve the Poisson equation and initialize the fields in temporal gauge.
 *      3) Spawn particles based on the Gauss constraint and refine charge distribution.
 */
public class CGCInitialCondition implements IInitialCondition {

	/**
	 * Model for initial charge density.
	 */
	protected IInitialChargeDensity initialChargeDensity;

	/**
	 * Particle creation algorithm.
	 */
	protected IParticleCreator initialParticleCreator;

	/**
	 * Applies CGC initial conditions.
	 * @param s Reference to the Simulation object
	 */
	public void applyInitialCondition(Simulation s) {
		// Initialize charge density.
		initialChargeDensity.initialize(s);
		int direction = initialChargeDensity.getDirection();
		int orientation = initialChargeDensity.getOrientation();

		// Solve Poisson equation and set fields on the grid. Also computes Gauss constraint and saves it.
		//ICGCPoissonSolver solver = new LightConePoissonSolver();
		ICGCPoissonSolver solver = new LightConePoissonSolverTadpole();
		solver.initialize(s);
		solver.solve(initialChargeDensity);

		// Spawn particles.
		initialParticleCreator = new LightConeParticles(direction, orientation);
		initialParticleCreator.setGaussConstraint(solver.getGaussViolation());
		//initialParticleCreator.setGaussConstraint(initialChargeDensity.getChargeDensity());
		initialParticleCreator.initialize(s);

		// Clear some memory.
		initialChargeDensity.clear();
	}

	public void setInitialChargeDensity(IInitialChargeDensity rho) {
		this.initialChargeDensity = rho;
	}
}