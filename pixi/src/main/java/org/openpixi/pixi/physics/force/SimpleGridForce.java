package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.Simulation;

public class SimpleGridForce implements Force {

	Simulation s;

	/** New empty force */
	public SimpleGridForce(Simulation s) {
		this.s = s;
	}

	public double getForceX(Particle2D p) {
		return p.charge * ( p.data.Ex + p.vy * p.data.Bz);
	}

	public double getForceY(Particle2D p) {
		return p.charge * (p.data.Ey - p.vx * p.data.Bz);
	}

	public double getPositionComponentofForceX(Particle2D p) {
		return p.charge * p.data.Ex;
	}

	public double getPositionComponentofForceY(Particle2D p) {
		return p.charge * p.data.Ey;
	}

	public double getNormalVelocityComponentofForceX(Particle2D p) {
		return p.charge * p.vy * p.data.Bz;
	}

	public double getNormalVelocityComponentofForceY(Particle2D p) {
		return - p.charge * p.vx * p.data.Bz;
	}

	public double getBz(Particle2D p) {
		return p.data.Bz;
	}

	public double getTangentVelocityComponentOfForceX(Particle2D p) {
		return 0;
	}

	public double getTangentVelocityComponentOfForceY(Particle2D p) {
		return 0;
	}

	public double getLinearDragCoefficient(Particle2D p) {
		return 0;
	}
}
