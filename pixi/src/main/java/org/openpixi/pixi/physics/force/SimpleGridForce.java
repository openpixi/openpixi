package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Simulation;

public class SimpleGridForce implements Force {

	public double getForceX(Particle p) {
		return p.charge * ( p.data.Ex + p.vy * p.data.Bz);
	}

	public double getForceY(Particle p) {
		return p.charge * (p.data.Ey - p.vx * p.data.Bz);
	}

	public double getPositionComponentofForceX(Particle p) {
		return p.charge * p.data.Ex;
	}

	public double getPositionComponentofForceY(Particle p) {
		return p.charge * p.data.Ey;
	}

	public double getNormalVelocityComponentofForceX(Particle p) {
		return p.charge * p.vy * p.data.Bz;
	}

	public double getNormalVelocityComponentofForceY(Particle p) {
		return - p.charge * p.vx * p.data.Bz;
	}

	public double getBz(Particle p) {
		return p.data.Bz;
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
