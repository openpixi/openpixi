//First I would like to start with very simple class Force.java, so we could see the graphic result.
package org.openpixi.physics;

public class Force {
	
	private final static double g = 9.81;     //the Earth acceleration
	private double dragcoef;                  //just a simple coefficient that represents the dragging
	
	public Force(double dragcoeff)
	{
		this.dragcoef = dragcoeff;
	}
	
	public double getDrag()
	{
		return dragcoef;
	}
	
	public void setDrag(double newcoeff)
	{
		dragcoef = newcoeff;
	}
	
	//getting the force in the x - direction
	public double getForceX(double vx, double vy, Particle2D par)
	{
		return -dragcoef * vx;
	}
	
	//getting the force in the y - direction
	public double getForceY(double vx, double vy, Particle2D par)
	{
		return -dragcoef * vy - par.getMass() * g;
	}
	
}
