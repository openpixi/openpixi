package org.openpixi.pixi.physics.initial.CGC;

import org.openpixi.pixi.math.AlgebraElement;

/**
 * Common interface for all CGC initial charge densities.
 */
public interface IInitialChargeDensity {
	void initialize();

	AlgebraElement getChargeDensity(int index);
}
