package org.openpixi.pixi.physics.movement;

/**
 *  Decomposes continuous area / space into disjunctive regions.
 */
public interface RegionDecomposition {

	/**
	 * Returns ID of the region in which point [x,y] lies.
	 */
	public int getRegion(double x, double y);
}
