package org.openpixi.physics;

import java.lang.Math;

	
public class Particle2D{
		
	public double x;            //x - Coordinate
	public double y;            //y - Coordinate

	public double vx;           //velocity in the x - direction
	public double vy;           //velocity in the y - direction
	
	public double ax;           //acceleration in the x - direction
	public double ay;           //acceleration in the y - direction

	private double mass;         // the mass of the particle
	private double echarge;      //the electric charge of the particle  
	
	public static final double ELC = 1.602e-19;      //defining the electric charge
	
	//building the constructor for 2-dim
	public Particle2D (double x, double y, double vx, double vy, double ax, double ay, double mass, double echarge)
	{
		this.x = x;
		this.y = y;
	
		this.vx = vx;
		this.vy = vy;
		
		this.ax = ax;
		this.ay = ay;
		
		this.mass = mass;
		this.echarge = echarge;
	}
	
	
	//a method that gives the mass
	public double getMass()
	{
		return(mass);
	}
	
	//a method that sets the mass to a certain value
	public void setMass(double newMass)
	{
		mass = newMass;
	}
	
	//a method that gives the electric charge
	public double getCharge()
	{
		System.out.println(ELC);
		return(echarge * ELC);
	}
			
	//a method that sets the mass to a certain value
	public void setCharge(double newEcharge)
	{
		echarge = newEcharge;
	}
	
	//a method that calculates the range from the center 0.0 for 2-dim
	public double rangeFromCenter2D()
	{
		return Math.sqrt(x * x + y * y);
	}
	
	
	//a method that calculates the range between two particles in 2-dim
	public double rangeBetween2D(Particle a)
	{
		double range;
		range = Math.pow(this.x - a.x, 2) + Math.pow(this.y - this.y, 2);
		return Math.sqrt(range);
	}
	
}




