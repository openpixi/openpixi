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
	public double getForceX(Particle2D par) {
		return par.charge * fields[0] + par.charge * par.vy * fields[2];
	}

	//getting the force in the y - direction
	@Override
	public double getForceY(Particle2D par) {
		return par.charge * fields[1] -
				par.charge * par.vx * fields[2];
	}

	@Override
	public double getPositionComponentofForceX(Particle2D par) {
		return par.charge * fields[0];
	}

	@Override
	public double getPositionComponentofForceY(Particle2D par) {
		return par.charge * fields[1];
	}

	@Override
	public double getNormalVelocityComponentofForceX(Particle2D par) {
		return par.charge * par.vy * fields[2];
	}

	@Override
	public double getNormalVelocityComponentofForceY(Particle2D par) {
		return - par.charge * par.vx * fields[2];
	}

	@Override
	public double getBz(Particle2D par) {
		return fields[2];
	}
}
