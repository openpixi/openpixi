package org.openpixi.pixi.physics.solver.relativistic;

import org.openpixi.pixi.physics.ConstantsSI;

public class SolverRelativistic {
	
	protected double c;
	
	public SolverRelativistic(double c) {
		this.c = c;
	}
	
	/**gamma = sqrt[1 + (u / c)^2]*/
	public double calculateGamma(double ux, double uy) {		
		double v = Math.sqrt(ux * ux + uy * uy);
		return Math.sqrt(1 + (v / c) * (v / c));
	}

}
