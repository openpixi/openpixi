package org.openpixi.pixi.physics;

import java.lang.Math;

	
public class Particle3D{
		
	public double x;            //x - Coordinate
	public double y;            //y - Coordinate
	public double z;            //z - Coordinate
	
	public double vx;           //velocity in the x - direction
	public double vy;           //velocity in the y - direction
	public double vz;           //velocity in the z - direction
	
	public double ax;           //acceleration in the x - direction
	public double ay;           //acceleration in the y - direction
	public double az;           //acceleration in the z - direction
	
	private double mass;         // the mass of the particle
	private double echarge;      //the electric charge of the particle  
		
	/** Empty constructor */
	public Particle3D ()
	{
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
		return(echarge);
	}
			
	//a method that sets the mass to a certain value
	public void setCharge(double newEcharge)
	{
		echarge = newEcharge;
	}
	
	
	//a method that calculates the range from the center 0.0 for 3-dim
	public double rangeFromCenter3D()
	{
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	
	//a method that calculates the range between two particles in 3-dim
	public double rangeBetween3D(Particle3D a)
	{
		double range;
		range = Math.pow(this.x - a.x, 2) + Math.pow(this.y - a.y, 2) + Math.pow(this.z - a.z, 2);
		return Math.sqrt(range);
	}
}




