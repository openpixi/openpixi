package org.openpixi.pixi.physics.force.relativistic;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.Simulation;

public class SimpleGridForceRelativistic extends ForceRelativistic {

	Simulation s;

	/** New empty force */
	public SimpleGridForceRelativistic(Simulation s) {
		super();
		this.s = s;
	}

	//getting the force in the x - direction
	@Override
	public double getForceX(Particle2D p) {
		return p.charge * ( p.pd.Ex + p.vy * p.pd.Bz);
	}

	//getting the force in the y - direction
	@Override
	public double getForceY(Particle2D p) {
		return p.charge * (p.pd.Ey - p.vx * p.pd.Bz);
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
		return p.charge * p.vy * p.pd.Bz;
	}

	@Override
	public double getNormalVelocityComponentofForceY(Particle2D p) {
		return - p.charge * p.vx * p.pd.Bz;
	}

	@Override
	public double getBz(Particle2D p) {
		return p.pd.Bz;
	}
}
