package org.openpixi.physics;

import java.lang.Math;

	
public class Particle2D{
		
	public double x;            //x - Coordinate
	public double y;            //y - Coordinate

	public double vx;           //velocity in the x - direction
	public double vy;           //velocity in the y - direction
	
	private double ax;           //acceleration in the x - direction
	private double ay;           //acceleration in the y - direction

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
	
	/*this is a method, that  is based on the simple Euler-Richardson algorithm (it represents a neat way of finding the
	 * numerical solutions of a differential equation, based on the Euler algorithm),
	 *  where I have used the following literature:
	 * http://www.physics.udel.edu/~bnikolic/teaching/phys660/numerical_ode/node4.html
	 * I shall use this method togethet with the class Force.java to get some graphical results*/
	public void algorithm(double step, Force f)
	{
		boolean onlyonce = true;
		
		if(onlyonce)
		{
			ax = f.getForceX(vx, vy, this) / mass;
			ay = f.getForceY(vx, vy, this) / mass;
			onlyonce = false;
		}
		//starting the Euler-Richardson algorithm (the equations correspond with the ones on the above mentioned website)
		double vxmiddle = vx + ax * step / 2;
		double vymiddle = vy + ay * step / 2;
		
		//double xmiddle = x + vx * step / 2;    actually, this two equations are not needed, but I've written them
		//double ymiddle = y + vy * step / 2;    so the algorithm is complete
		
		double axmiddle = f.getForceX(vxmiddle, vymiddle, this) / mass;
		double aymiddle = f.getForceY(vxmiddle, vymiddle, this) / mass;
		
		vx += axmiddle * step;
		vy += aymiddle * step;
		
		x += vxmiddle * step;
		y += vymiddle * step;
		
		ax = f.getForceX(vx, vy, this) / mass;
		ay = f.getForceY(vx, vy, this) / mass;
	}
	
}
