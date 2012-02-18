//First I would like to start with very simple class Force.java, so we could see the graphic result.
package org.openpixi.physics;

public class Force {
	
	private final static double g = 9.81;     //the Earth acceleration
	private double dragcoef;                  //just a simple coefficient that represents the dragging
	private double ex;                          //the electric field in x - direction
	private double ey;				            //the electric field in y - direction
	private double bz;                          //the magnetic field in z - direction
	
	public Force(double dragcoeff, double ex, double ey, double bz)       //the constructor
	{
		this.dragcoef = dragcoeff;
		this.ex = ex;
		this.ey = ey;
		this.bz = bz;
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
		return -dragcoef * vx + par.getCharge() * ex + par.getCharge() * vy * bz;
	}
	
	//getting the force in the y - direction
	public double getForceY(double vx, double vy, Particle2D par)
	{
		return - dragcoef * vy - par.getMass() * g + par.getCharge() * ey - par.getCharge() * vx * bz;
	}
	
}
