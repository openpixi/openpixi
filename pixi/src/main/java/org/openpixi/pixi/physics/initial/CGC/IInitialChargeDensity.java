package org.openpixi.pixi.physics.initial.CGC;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;

/**
 * Common interface for all CGC initial charge densities.
 */
public interface IInitialChargeDensity {
	void initialize(Simulation s);

	AlgebraElement getChargeDensity(int index);

	AlgebraElement[] getChargeDensity();

	int getDirection();

	int getOrientation();

	String getInfo();

	void clear();
}
