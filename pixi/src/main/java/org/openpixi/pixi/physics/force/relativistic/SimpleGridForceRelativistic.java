package org.openpixi.pixi.physics.force.relativistic;

import org.openpixi.pixi.physics.ConstantsSI;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.force.Force;

public class SimpleGridForceRelativistic implements Force {

	Simulation s;

	/** New empty force */
	public SimpleGridForceRelativistic(Simulation s) {
		this.s = s;
	}

	public double getForceX(Particle p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double uy = p.vy * gamma;
		
		return p.charge * ( p.data.Ex + uy * p.data.Bz);
	}

	public double getForceY(Particle p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.vx * gamma;
		
		return p.charge * (p.data.Ey - ux * p.data.Bz);
	}

	public double getPositionComponentofForceX(Particle p) {
		return p.charge * p.data.Ex;
	}

	public double getPositionComponentofForceY(Particle p) {
		return p.charge * p.data.Ey;
	}

	public double getNormalVelocityComponentofForceX(Particle p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double uy = p.vy * gamma;
		
		return p.charge * uy * p.data.Bz;
	}

	public double getNormalVelocityComponentofForceY(Particle p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.vx * gamma;
		
		return - p.charge * ux * p.data.Bz;
	}

	public double getBz(Particle p) {
		return p.data.Bz;
	}

	public double getTangentVelocityComponentOfForceX(Particle p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.vx * gamma;
		
		return -getLinearDragCoefficient(p) * ux;
	}

	public double getTangentVelocityComponentOfForceY(Particle p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double uy = p.vy * gamma;
		
		return -getLinearDragCoefficient(p) * uy;
	}

	public double getLinearDragCoefficient(Particle p) {
		return 0;
	}
}
