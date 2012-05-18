package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.Particle;

public class SimpleGridForce implements Force {

	public double getForceX(Particle p) {
		return p.charge * ( p.Ex + p.vy * p.Bz);
	}

	public double getForceY(Particle p) {
		return p.charge * (p.Ey - p.vx * p.Bz);
	}

	public double getPositionComponentofForceX(Particle p) {
		return p.charge * p.Ex;
	}

	public double getPositionComponentofForceY(Particle p) {
		return p.charge * p.Ey;
	}

	public double getNormalVelocityComponentofForceX(Particle p) {
		return p.charge * p.vy * p.Bz;
	}

	public double getNormalVelocityComponentofForceY(Particle p) {
		return - p.charge * p.vx * p.Bz;
	}

	public double getBz(Particle p) {
		return p.Bz;
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
