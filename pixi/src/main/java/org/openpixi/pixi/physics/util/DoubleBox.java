package org.openpixi.pixi.physics.util;

public class DoubleBox {
	private double xmin;
	private double xmax;
	private double ymin;
	private double ymax;

	public DoubleBox(double xmin, double xmax, double ymin, double ymax) {
		set(xmin, xmax, ymin, ymax);
	}

	public double xmin() {
		return xmin;
	}

	public double xsize() {
		return xmax - xmin;
	}

	public double xmax() {
		return xmax;
	}

	public double ymin() {
		return ymin;
	}

	public double ymax() {
		return ymax;
	}

	public double ysize() {
		return ymax - ymin;
	}

	public boolean contains(double x, double y) {
		if (xmin <= x && x <= xmax && ymin <= y && y <= ymax()) {
			return true;
		}
		else {
			return false;
		}
	}

	public void set(double xmin, double xmax, double ymin, double ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}
}
