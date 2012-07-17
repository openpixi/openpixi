package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.movement.BoundingBox;

/**
 * Identifies different boundary regions of the simulation area.
 */
public class BoundaryRegionDecomposition {

	/*
	 * Specific boundary is specified by the combination of X and Y values.
	 * E.g. top-left is XMIN + YMIN.
	 */

	public static final int XMIN = 0;
	public static final int XCENTER = 1;
	public static final int XMAX = 2;
	public static final int YMIN = 0;
	public static final int YCENTER = 3;
	public static final int YMAX = 6;

	/** Box around the simulation area. */
	BoundingBox simulationArea;

	public BoundaryRegionDecomposition(BoundingBox simulationArea) {
		this.simulationArea = simulationArea;
	}


	/**
	 * We do not get the region based on position but a whole box.
	 * This way we are more flexible
	 * - we can check particle's circumference to be in a boundary region.
	 */
	public int getRegion(BoundingBox particleBox) {
		int xidx;
		int yidx;

		if (particleBox.xmin() < simulationArea.xmin()) {
			xidx  = XMIN;
		} else if (particleBox.xmax() >= simulationArea.xmax()) {
			xidx = XMAX;
		} else {
			xidx = XCENTER;
		}

		if (particleBox.ymin() < simulationArea.ymin()) {
			yidx = YMIN;
		} else if (particleBox.ymax() >= simulationArea.ymax()) {
			yidx = YMAX;
		} else {
			yidx = YCENTER;
		}

		return xidx + yidx;
	}
}
