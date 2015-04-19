package org.openpixi.pixi.physics.force.relativistic;

import org.openpixi.pixi.physics.RelativisticVelocity;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.IParticle;

public class SimpleGridForceRelativistic implements Force {

	Simulation s;
	RelativisticVelocity relvelocity;

	/** New empty force */
	public SimpleGridForceRelativistic(Simulation s) {
		this.s = s;
		
		relvelocity = new RelativisticVelocity(s.getSpeedOfLight());
	}
	
	public SimpleGridForceRelativistic(double speedOfLight) {
		
		relvelocity = new RelativisticVelocity(speedOfLight);
	}

	public double getForceX(IParticle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vy = p.getVy() / gamma;
		double vz = p.getVz() / gamma;
		
		return p.getCharge() * ( p.getEx() + vy * p.getBz() - vz * p.getBy() );
	}

	public double getForceY(IParticle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vx = p.getVx() / gamma;
		double vz = p.getVz() / gamma;
		
		return p.getCharge() * ( p.getEy() + vz * p.getBx() - vx * p.getBz() );
	}
	
	public double getForceZ(IParticle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vx = p.getVx() / gamma;
		double vy = p.getVy() / gamma;
		
		return p.getCharge() * ( p.getEz() + vx * p.getBy() - vy * p.getBx() );
	}
}
