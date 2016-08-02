package org.openpixi.pixi.physics.initial.CGC;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;

public interface ICGCPoissonSolver {
	void initialize(Simulation s);
	void solve(IInitialChargeDensity chargeDensity);
	AlgebraElement[] getGaussViolation();
	GroupElement[] getV();
}
