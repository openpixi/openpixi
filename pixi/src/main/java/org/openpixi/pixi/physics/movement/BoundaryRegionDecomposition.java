package org.openpixi.pixi.physics.movement;

/**
 * Identifies different boundary regions of the simulation area.
 */
public class BoundaryRegionDecomposition implements RegionDecomposition {

	/** Identifies top-left boundary. */
	public static final int XMIN_YMIN = 0;
	/** Identifies top boundary. */
	public static final int XCEN_YMIN = 1;
	/** Identifies top-right boundary. */
	public static final int XMAX_YMIN = 2;
	/** Identifies left boundary. */
	public static final int XMIN_YCEN = 3;
	/** Identifies simulation area. */
	public static final int XCEN_YCEN = 4;
	public static final int XMAX_YCEN = 5;
	public static final int XMIN_YMAX = 6;
	public static final int XCEN_YMAX = 7;
	public static final int XMAX_YMAX = 8;

	/** Box around the simulation area. */
	BoundingBox sa;

	public BoundaryRegionDecomposition(BoundingBox sa) {
		this.sa = sa;
	}

	public int getRegion(double x, double y) {
		if (x < sa.xmin) {
			if (y < sa.ymin) {
				return XMIN_YMIN;
			} else if (y > sa.ymax) {
				return XMIN_YMAX;
			} else {
				return XMIN_YCEN;
			}
		} else if (x > sa.xmax) {
			if (y < sa.ymin) {
				return XMAX_YMIN;
			} else if (y > sa.ymax) {
				return XMAX_YMAX;
			} else {
				return XMAX_YCEN;
			}
		} else {
			if (y < sa.ymin) {
				return XCEN_YMIN;
			} else if (y > sa.ymax) {
				return XCEN_YMAX;
			} else {
				return XCEN_YCEN;
			}
		}
	}
}
