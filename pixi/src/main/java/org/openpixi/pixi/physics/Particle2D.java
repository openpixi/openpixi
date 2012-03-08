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

	/** Empty constructor */
	public Particle2D()
	{
	}

	/** Copy constructor */
	public Particle2D(Particle2D p) {
		x = p.x;
		y = p.y;
		radius = p.radius;
		vx = p.vx;
		vy = p.vy;
		ax = p.ax;
		ay = p.ay;
		mass = p.mass;
		charge = p.charge;
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
}
