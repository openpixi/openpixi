package org.openpixi.pixi.physics;

/**
 * For classes which can work with grid but also particle boundaries without distinction
 * between particle or grid boundaries.
 * (Used in distributed version)
 *
 * Why it is good to have separate types for grid and particle boundaries.
 * For example, in the future we might want to add rubber boundaries
 * which would reflect the particle like hardwall boundaries but slower (in more time steps).
 * Such boundaries do not require a new grid boundary type,
 * we can still use the hardwall boundary type for the grid.
 */
public enum GeneralBoundaryType {
	Hardwall,
	Periodic,
}
