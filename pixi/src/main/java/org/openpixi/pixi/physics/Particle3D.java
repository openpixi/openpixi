/*
 * OpenPixi - Open Particle-In-Cell (PIC) Simulator
 * Copyright (C) 2012  OpenPixi.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
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




