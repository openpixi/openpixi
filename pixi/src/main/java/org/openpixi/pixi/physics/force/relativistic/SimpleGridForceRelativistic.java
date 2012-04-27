package org.openpixi.pixi.physics.force.relativistic;

import org.openpixi.pixi.physics.ConstantsSI;
import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.force.Force;

public class SimpleGridForceRelativistic extends Force {

	Simulation s;

	/** New empty force */
	public SimpleGridForceRelativistic(Simulation s) {
		super();
		this.s = s;
	}

	//getting the force in the x - direction
	@Override
	public double getForceX(Particle2D p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double uy = p.vy * gamma;
		
		return p.charge * ( p.pd.Ex + uy * p.pd.Bz);
	}

	//getting the force in the y - direction
	@Override
	public double getForceY(Particle2D p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.vx * gamma;
		
		return p.charge * (p.pd.Ey - ux * p.pd.Bz);
	}

	@Override
	public double getPositionComponentofForceX(Particle2D p) {
		return p.charge * p.pd.Ex;
	}

	@Override
	public double getPositionComponentofForceY(Particle2D p) {
		return p.charge * p.pd.Ey;
	}

	@Override
	public double getNormalVelocityComponentofForceX(Particle2D p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double uy = p.vy * gamma;
		
		return p.charge * uy * p.pd.Bz;
	}

	@Override
	public double getNormalVelocityComponentofForceY(Particle2D p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.vx * gamma;
		
		return - p.charge * ux * p.pd.Bz;
	}

	@Override
	public double getBz(Particle2D p) {
		return p.pd.Bz;
	}
	
	public double getTangentVelocityComponentOfForceX(Particle2D p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.vx * gamma;
		
		return -getLinearDragCoefficient(p) * ux;
	}

	public double getTangentVelocityComponentOfForceY(Particle2D p) {
		double v = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double uy = p.vy * gamma;
		
		return -getLinearDragCoefficient(p) * uy;
	}
}
