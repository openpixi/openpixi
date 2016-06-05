package org.openpixi.pixi.physics.initial.CGC;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;

/**
 * This class solves the transverse Poisson equation for a three-dimensional (Lorenz gauge) charge density
 * 'sheet by sheet' in the longitudinal direction and then initializes the fields and particles in the temporal gauge.
 */
public class LightConePoissonSolver {

	Simulation s;
	AlgebraElement[] phi0;
	AlgebraElement[] phi1;
	GroupElement[] V;

	public LightConePoissonSolver(Simulation s) {
		this.s = s;
	}

	public void solve(IInitialChargeDensity chargeDensity) {
		// Solve for phi at t = - at/2 'sheet by sheet'

		// Compute V at t = - at/2

		// Set gauge links at t = - at/2

		// Compute phi at t = at/2 from faked charge density movement

		// Compute V at t = at / 2

		// Set gauge links at t = at/2

		// Compute electric field at t = 0

		// Compute Gauss constraint from grid copy
	}
}
