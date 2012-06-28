package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.movement.BoundingBox;

/**
 * Identifies different boundary regions of the simulation area.
 */
public class BoundaryRegionDecomposition {

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

	public int getRegion(double x, double y) {
		if (x < simulationArea.xmin()) {
			if (y < simulationArea.ymin()) {
				return XMIN + YMIN;
			} else if (y >= simulationArea.ymax()) {
				return XMIN + YMAX;
			} else {
				return XMIN + YCENTER;
			}
		} else if (x >= simulationArea.xmax()) {
			if (y < simulationArea.ymin()) {
				return XMAX + YMIN;
			} else if (y >= simulationArea.ymax()) {
				return XMAX + YMAX;
			} else {
				return XMAX + YCENTER;
			}
		} else {
			if (y < simulationArea.ymin()) {
				return XCENTER + YMIN;
			} else if (y >= simulationArea.ymax()) {
				return XCENTER + YMAX;
			} else {
				return XCENTER + YCENTER;
			}
		}
	}
}
