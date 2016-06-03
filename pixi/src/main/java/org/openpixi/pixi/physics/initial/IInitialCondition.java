package org.openpixi.pixi.physics.initial;

import org.openpixi.pixi.physics.Simulation;

/**
 * A simple interface used for all initial conditions.
 */
public interface IInitialCondition {
	/**
	 * Applies the initial condition to the Grid in the Simulation class.
	 * In the case of pure field initial conditions (YM) this simply sets all the fields to the desired values.
	 * For CGC initial condition this initializes the charge density, solves the Poisson equation and initializes the
	 * fields, and finally spawns charged particles and refines their charge distribution.
	 *
	 * @param s Reference to the Simulation object
	 */
	void applyInitialCondition(Simulation s);
}
