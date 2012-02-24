package org.openpixi.pixi.physics.boundary;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.solver.*;

public class Boundary {
	public double xmin;
	public double xmax;
	public double ymin;
	public double ymax;

	public Boundary() {
		
	}

	public void setBoundaries(double xmin, double ymin, double xmax, double ymax) {
		this.xmin = xmin;
		this.ymin = ymin;
		this.xmax = xmax;
		this.ymax = ymax;
	}

	public void check(Particle2D particle, Solver s) {
		
	}
}
