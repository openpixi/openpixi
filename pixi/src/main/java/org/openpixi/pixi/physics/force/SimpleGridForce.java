package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.particles.Particle;

public class SimpleGridForce implements Force {

	public double getForceX(Particle p) {
		return p.getCharge() * (p.getEx() + p.getVy() * p.getBz() - p.getVz() * p.getBy());
	}

	public double getForceY(Particle p) {
		return p.getCharge() * (p.getEy() + p.getVz() * p.getBx() - p.getVx() * p.getBz());
	}
	
	public double getForceZ(Particle p) {
		return p.getCharge() * (p.getEz() + p.getVx() * p.getBy() - p.getVy() * p.getBx());
	}

	public double getPositionComponentofForceX(Particle p) {
		return p.getCharge() * p.getEx();
	}

	public double getPositionComponentofForceY(Particle p) {
		return p.getCharge() * p.getEy();
	}

	public double getNormalVelocityComponentofForceX(Particle p) {
		return p.getCharge() * p.getVy() * p.getBz();
	}

	public double getNormalVelocityComponentofForceY(Particle p) {
		return - p.getCharge() * p.getVx() * p.getBz();
	}

	public double getBz(Particle p) {
		return p.getBz();
	}

	public double getTangentVelocityComponentOfForceX(Particle p) {
		return 0;
	}

	public double getTangentVelocityComponentOfForceY(Particle p) {
		return 0;
	}

	public double getLinearDragCoefficient(Particle p) {
		return 0;
	}
}
