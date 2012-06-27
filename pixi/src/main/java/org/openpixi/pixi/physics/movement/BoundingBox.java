package org.openpixi.pixi.physics.movement;

public class BoundingBox {
	public double xmin;
	public double xmax;
	public double ymin;
	public double ymax;

	public BoundingBox(double xmin, double xmax, double ymin, double ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}
}
