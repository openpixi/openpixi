package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.util.DoubleBox;

/**
 * Identifies different boundary regions of the simulation area.
 * The boundary regions lie outside of the simulation area.
 */
public class BoundaryRegions {

	/*
	 * Specific boundary is specified by the combination of X and Y values.
	 * E.g. top-left is X_MIN + Y_MIN.
	 */

	public static final int X_MIN = 0;
	public static final int X_CENTER = 1;
	public static final int X_MAX = 2;
	public static final int Y_MIN = 0;
	public static final int Y_CENTER = 3;
	public static final int Y_MAX = 6;

	public static final int NUM_OF_REGIONS = X_MAX + Y_MAX + 1;

	/** Regions which share an edge with the simulation area. */
	public static int[] EDGE_REGIONS = {
			X_MIN + Y_CENTER,
			X_MAX + Y_CENTER,
			Y_MIN + X_CENTER,
			Y_MAX + X_CENTER
	};
	/** Regions which share a corner with the simulation area. */
	public static int[] CORNER_REGIONS = {
			X_MIN + Y_MIN,
			X_MAX + Y_MIN,
			X_MIN + Y_MAX,
			X_MAX + Y_MAX
	};

	/** Box around the simulation area. */
	private DoubleBox simulationArea;

	/**
	 * Helper box, so that we do not have to create a new box each time we call
	 * getRegion(double, double).
	 */
	private DoubleBox helperBox = new DoubleBox(0, 0, 0, 0);

	public BoundaryRegions(DoubleBox simulationArea) {
		this.simulationArea = simulationArea;
	}

	/**
	 * We do not get the region based on position but a whole box.
	 * This way we are more flexible
	 * - we can check particle's circumference to be in a boundary region.
	 */
	public int getRegion(DoubleBox particleBox) {
		int xidx;
		int yidx;

		if (particleBox.xmin() < simulationArea.xmin()) {
			xidx  = X_MIN;
		} else if (particleBox.xmax() >= simulationArea.xmax()) {
			xidx = X_MAX;
		} else {
			xidx = X_CENTER;
		}

		if (particleBox.ymin() < simulationArea.ymin()) {
			yidx = Y_MIN;
		} else if (particleBox.ymax() >= simulationArea.ymax()) {
			yidx = Y_MAX;
		} else {
			yidx = Y_CENTER;
		}

		return xidx + yidx;
	}


	public int getRegion(double x, double y) {
	 	helperBox.set(x, x, y, y);
		return getRegion(helperBox);
	}
}