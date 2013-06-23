package org.openpixi.pixi.physics.particles;

/**
 * A lightweight particle class. It does not move. Perfectly suited for a heavy ion lattice.
 */
public class ImmobileParticle extends Particle {
	
	/** x-coordinate */
	private double x;

	/** y-coordinate */
	private double y;
	
	/** radius of particle */
	private double radius;
	
	/** electric charge of the particle */
	private double charge;
	
	//----------------------------------------------------------------------------------------------
	// GETTERS
	//----------------------------------------------------------------------------------------------
	
	@Override
	public double getX() {return x;}
	
	@Override
	public double getY() {return y;}
	
	@Override
	public double getPrevX() {return x;}
	
	@Override
	public double getPrevY() {return y;}
	
	@Override
	public double getRadius() {return radius;}
	
	@Override
	public double getCharge() {return charge;}

	//----------------------------------------------------------------------------------------------
	// SETTERS
	//----------------------------------------------------------------------------------------------
	
	@Override
	public void setX(double x) {
		this.x = x;
	}

	@Override
	public void addX(double x) {
		this.x += x;
	}

	@Override
	public void setY(double y) {
		this.y = y;
	}

	@Override
	public void addY(double y) {
		this.y += y;
	}

	@Override
	public void setRadius(double radius) {
		this.radius = radius;
	}

	@Override
	public void setCharge(double charge) {
		this.charge = charge;
	}

	//----------------------------------------------------------------------------------------------
	// UTILITY METHODS
	//----------------------------------------------------------------------------------------------

	/** Epty constructor */
	public ImmobileParticle() {}
	
	@Override
	public Particle copy() {
		Particle p = new ImmobileParticle();
		p.setX(x);
		p.setY(y);	
		p.setRadius(radius);
		p.setCharge(charge);
		
		return p;
	}
	
	@Override
	public String toString() {
		return String.format("[%.3f,%.3f]", x, y);
	}
}
