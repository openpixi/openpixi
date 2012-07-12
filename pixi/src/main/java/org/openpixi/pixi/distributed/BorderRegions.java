package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.util.DoubleBox;

/**
 * Identifies different border regions of the simulation area.
 */
public class BorderRegions {

	public static final int X_BOUNDARY_MIN = 0;
	public static final int X_BORDER_MIN = 1;
	public static final int X_CENTER = 2;
	public static final int X_BORDER_MAX = 3;
	public static final int X_BOUNDARY_MAX = 4;

	public static final int Y_BOUNDARY_MIN = 0;
	public static final int Y_BORDER_MIN = 5;
	public static final int Y_CENTER = 10;
	public static final int Y_BORDER_MAX = 15;
	public static final int Y_BOUNDARY_MAX = 20;

	public static final int NUM_OF_REGIONS = X_BOUNDARY_MAX + Y_BOUNDARY_MAX + 1;

	/** Regions which share an edge with the inner simulation area. */
	public static int[] EDGE_REGIONS = {
			X_BORDER_MIN + Y_CENTER,
			X_BORDER_MAX + Y_CENTER,
			Y_BORDER_MIN + X_CENTER,
			Y_BORDER_MAX + X_CENTER
	};
	/** Regions which share a corner with the inner simulation area. */
	public static int[] CORNER_REGIONS = {
			X_BORDER_MIN + Y_BORDER_MIN,
			X_BORDER_MAX + Y_BORDER_MIN,
			X_BORDER_MIN + Y_BORDER_MAX,
			X_BORDER_MAX + Y_BORDER_MAX
	};
	/** Special border regions present only under hardwall boundaries. */
	public static int[] HARDWALL_CORNER_REGIONS = {
			X_BORDER_MIN + Y_BOUNDARY_MIN,
			X_BORDER_MIN + Y_BOUNDARY_MAX,
			X_BORDER_MAX + Y_BOUNDARY_MIN,
			X_BORDER_MAX + Y_BOUNDARY_MAX,
			Y_BORDER_MIN + X_BOUNDARY_MIN,
			Y_BORDER_MIN + X_BOUNDARY_MAX,
			Y_BORDER_MAX + X_BOUNDARY_MIN,
			Y_BORDER_MAX + X_BOUNDARY_MAX,
	};

	private DoubleBox simulationArea;
	/** Inner simulation area (without border cells). */
	private DoubleBox innerArea;

	public BorderRegions(DoubleBox simulationArea, DoubleBox innerArea) {
		this.simulationArea = simulationArea;
		this.innerArea = innerArea;
	}

	public int getRegion(double x, double y) {
		int xidx;
		if (x < simulationArea.xmin()) {
			xidx = X_BOUNDARY_MIN;
		} else if (x < innerArea.xmin()) {
			xidx = X_BORDER_MIN;
		} else if (x < innerArea.xmax()) {
			xidx = X_CENTER;
		} else if (x < simulationArea.xmax()) {
			xidx = X_BORDER_MAX;
		} else {
			xidx = X_BOUNDARY_MAX;
		}

		int yidx;
		if (y < simulationArea.ymin()) {
			yidx = Y_BOUNDARY_MIN;
		} else if (y < innerArea.ymin()) {
			yidx = Y_BORDER_MIN;
		} else if (y < innerArea.xmax()) {
			yidx = Y_CENTER;
		} else if (y < simulationArea.ymax()) {
			yidx = Y_BORDER_MAX;
		} else {
			yidx = Y_BOUNDARY_MAX;
		}

		return xidx + yidx;
	}
}
