package org.openpixi.pixi.physics.force.relativistic;

import org.openpixi.pixi.physics.RelativisticVelocity;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.Particle;

public class SimpleGridForceRelativistic implements Force {

	Simulation s;
	RelativisticVelocity relvelocity;

	/** New empty force */
	public SimpleGridForceRelativistic(Simulation s) {
		this.s = s;
		
		relvelocity = new RelativisticVelocity(s.getSpeedOfLight());
	}

	public double getForceX(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vy = p.getVy() / gamma;
		double vz = p.getVz() / gamma;
		
		return p.getCharge() * ( p.getEx() + vy * p.getBz() - vz * p.getBy() );
	}

	public double getForceY(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vx = p.getVx() / gamma;
		double vz = p.getVz() / gamma;
		
		return p.getCharge() * ( p.getEy() + vz * p.getBx() - vx * p.getBz() );
	}
	
	public double getForceZ(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vx = p.getVx() / gamma;
		double vy = p.getVy() / gamma;
		
		return p.getCharge() * ( p.getEz() + vx * p.getBy() - vy * p.getBx() );
	}

	public double getPositionComponentofForceX(Particle p) {
		return p.getCharge() * p.getEx();
	}

	public double getPositionComponentofForceY(Particle p) {
		return p.getCharge() * p.getEy();
	}

	public double getNormalVelocityComponentofForceX(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vy = p.getVy() / gamma;
		
		return p.getCharge() * vy * p.getBz();
	}

	public double getNormalVelocityComponentofForceY(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		double vx = p.getVx() / gamma;
		
		return - p.getCharge() * vx * p.getBz();
	}

	public double getBz(Particle p) {
		return p.getBz();
	}

	public double getTangentVelocityComponentOfForceX(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vx = p.getVx() / gamma;
		
		return -getLinearDragCoefficient(p) * vx;
	}

	public double getTangentVelocityComponentOfForceY(Particle p) {
		double gamma = relvelocity.calculateGamma(p);
		
		//v = u / gamma
		double vy = p.getVy() / gamma;
		
		return -getLinearDragCoefficient(p) * vy;
	}

	public double getLinearDragCoefficient(Particle p) {
		return 0;
	}
}
