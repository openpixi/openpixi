package org.openpixi.pixi.physics.util;

public class IntBox {
	private int xmin;
	private int xmax;
	private int ymin;
	private int ymax;

	public IntBox(int xmin, int xmax, int ymin, int ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}

	public int xmin() {
		return xmin;
	}

	public int xsize() {
		return xmax - xmin;
	}

	public int xmax() {
		return xmax;
	}

	public int ymin() {
		return ymin;
	}

	public int ymax() {
		return ymax;
	}

	public int ysize() {
		return ymax - ymin;
	}

	public boolean contains(double x, double y) {
		if (xmin <= x && x < xmax && ymin <= y && y < ymax()) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		return String.format("[%d,%d,%d,%d]", xmin, xmax, ymin, ymax);
	}
}
