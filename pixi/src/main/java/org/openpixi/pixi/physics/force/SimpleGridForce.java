package org.openpixi.pixi.physics.force;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.Simulation;

public class SimpleGridForce extends Force {

	Simulation s;

	/** New empty force */
	public SimpleGridForce(Simulation s) {
		super();
		this.s = s;
	}

	//getting the force in the x - direction
	@Override
	public double getForceX(Particle2D p) {
		return p.charge * ( p.data.Ex + p.vy * p.data.Bz);
	}

	//getting the force in the y - direction
	@Override
	public double getForceY(Particle2D p) {
		return p.charge * (p.data.Ey - p.vx * p.data.Bz);
	}

	@Override
	public double getPositionComponentofForceX(Particle2D p) {
		return p.charge * p.data.Ex;
	}

	@Override
	public double getPositionComponentofForceY(Particle2D p) {
		return p.charge * p.data.Ey;
	}

	@Override
	public double getNormalVelocityComponentofForceX(Particle2D p) {
		return p.charge * p.vy * p.data.Bz;
	}

	@Override
	public double getNormalVelocityComponentofForceY(Particle2D p) {
		return - p.charge * p.vx * p.data.Bz;
	}

	@Override
	public double getBz(Particle2D p) {
		return p.data.Bz;
	}
}
