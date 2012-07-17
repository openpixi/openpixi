package org.openpixi.pixi.physics.movement;

public class BoundingBox {
	private double xmin;
	private double xmax;
	private double ymin;
	private double ymax;

	public BoundingBox(double xmin, double xmax, double ymin, double ymax) {
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

	public void set(double xmin, double xmax, double ymin, double ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}
}
