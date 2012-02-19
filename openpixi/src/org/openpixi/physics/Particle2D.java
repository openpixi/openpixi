package org.openpixi.physics;

import java.lang.Math;

	
public class Particle2D{

	/** x-coordinate */
	public double x;

	/** y-coordinate */
	public double y;
	
	/** radius of particle */
	public double radius;

	/** velocity in x-direction */
	public double vx;

	/** velocity in y-direction */
	public double vy;

	/** acceleration in x-direction */
	public double ax;

	/** acceleration in y-direction */
	public double ay;

	/** mass of the particle */
	public double mass;

	/** electric charge of the particle */
	public double charge;

	public double rightBoundary;   //the right boundary
	public double bottomBoundary;    //the bottom boundary

	/** Empty constructor */
	public Particle2D()
	{
	}

	//a method that calculates the range from the center 0.0 for 2-dim
	public double rangeFromCenter2D()
	{
		return Math.sqrt(x * x + y * y);
	}
	
	
	//a method that calculates the range between two particles in 2-dim
	public double rangeBetween2D(Particle2D a)
	{
		double range;
		range = Math.pow(this.x - a.x, 2) + Math.pow(this.y - this.y, 2);
		return Math.sqrt(range);
	}
	//a method that defines the boundaries
	public void setBoundaries(int height, int width)
	{
		rightBoundary = width - 2 * radius;
		bottomBoundary = height - 2 * radius;
	}
	
}
