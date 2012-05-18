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
		double v = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double uy = p.getVy() * gamma;
		
		return p.getCharge() * ( p.getEx() + uy * p.getBz());
	}

	public double getForceY(Particle p) {
		double v = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.getVx() * gamma;
		
		return p.getCharge() * (p.getEy() - ux * p.getBz());
	}

	public double getPositionComponentofForceX(Particle p) {
		return p.getCharge() * p.getEx();
	}

	public double getPositionComponentofForceY(Particle p) {
		return p.getCharge() * p.getEy();
	}

	public double getNormalVelocityComponentofForceX(Particle p) {
		double v = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double uy = p.getVy() * gamma;
		
		return p.getCharge() * uy * p.getBz();
	}

	public double getNormalVelocityComponentofForceY(Particle p) {
		double v = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.getVx() * gamma;
		
		return - p.getCharge() * ux * p.getBz();
	}

	public double getBz(Particle p) {
		return p.getBz();
	}

	public double getTangentVelocityComponentOfForceX(Particle p) {
		double v = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double ux = p.getVx() * gamma;
		
		return -getLinearDragCoefficient(p) * ux;
	}

	public double getTangentVelocityComponentOfForceY(Particle p) {
		double v = Math.sqrt(p.getVx() * p.getVx() + p.getVy() * p.getVy());
		double gamma = Math.sqrt(1 / (1 - (v / ConstantsSI.c) * (v / ConstantsSI.c)));
		
		double uy = p.getVy() * gamma;
		
		return -getLinearDragCoefficient(p) * uy;
	}

	public double getLinearDragCoefficient(Particle p) {
		return 0;
	}
}
