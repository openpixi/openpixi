package org.openpixi.pixi.physics.movement.boundary;

import org.openpixi.pixi.physics.util.DoubleBox;
import org.openpixi.pixi.physics.util.Point;

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


	public BoundaryRegions(DoubleBox simulationArea) {
		this.simulationArea = simulationArea;
	}


	public int getRegion(DoubleBox particleBox) {
		return getRegion(
				particleBox.xmin(), particleBox.xmax(),
				particleBox.ymin(), particleBox.ymax());
	}


	public int getRegion(double x, double y) {
		return getRegion(x,x,y,y);
	}


	/**
	 * Gets the region based on simulation area intersection with box.
	 * This way we can check particle's circumference to be in a boundary region.
	 */
	private int getRegion(double xmin, double xmax, double ymin, double ymax) {
		int xidx;
		int yidx;

		if (xmin < simulationArea.xmin()) {
			xidx  = X_MIN;
		} else if (xmax >= simulationArea.xmax()) {
			xidx = X_MAX;
		} else {
			xidx = X_CENTER;
		}

		if (ymin < simulationArea.ymin()) {
			yidx = Y_MIN;
		} else if (ymax >= simulationArea.ymax()) {
			yidx = Y_MAX;
		} else {
			yidx = Y_CENTER;
		}

		return xidx + yidx;
	}


	/**
	 * Decomposes the region id to X and Y components.
	 */
	public static Point decomposeRegionID(int regionID) {
		int y = findHighestPossibleY(regionID);
		int x = findX(y, regionID);
		return new Point(x, y);
	}


	/**
	 * Returns sign of the region.
	 * E.g.:
	 *   X_MIN + Y_MIN = (-1, -1)
	 *   X_CENTER + Y_MAX = (0, 1)
	 */
	public static Point getSign(int regionID) {
		Point region = decomposeRegionID(regionID);
		return new Point(region.x - 1, region.y / 3 - 1);
	}


	private static int findX(int y, int regionID) {
		int potentialX = regionID - y;
		assert X_MIN <= potentialX && potentialX <= X_MAX;
		return potentialX;
	}


	private static int findHighestPossibleY(int regionID) {
		int highetsY = -1;
		for (int y = Y_MIN; y <= Y_MAX; ++y) {
			if (y <= regionID) {
				highetsY = y;
			}
		}
		assert highetsY != -1;
		return  highetsY;
	}
}
