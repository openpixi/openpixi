package org.openpixi.pixi.physics;

/**
 * For classes which can work with grid but also particle boundaries without distinction
 * between particle or grid boundaries.
 * (Used in distributed version)
 */
public enum GeneralBoundaryType {
	Hardwall,
	Periodic,
}
