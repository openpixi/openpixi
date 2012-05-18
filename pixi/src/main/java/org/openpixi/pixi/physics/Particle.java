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

	
public class Particle{

	/** x-coordinate */
	private double x;

	/** y-coordinate */
	private double y;
	
	/** radius of particle */
	private double radius;

	/** velocity in x-direction */
	private double vx;

	/** velocity in y-direction */
	private double vy;

	/** acceleration in x-direction */
	private double ax;

	/** acceleration in y-direction */
	private double ay;

	/** mass of the particle */
	private double mass;

	/** electric charge of the particle */
	private double charge;
	
	/**previous x position of particle*/
	private double prevX;
	/**previous y position of particle*/
	private double prevY;
	
	/**charge density of particle for a specific form factor*/
	private double chargedensity;
	
	private double Ex;
	private double Ey;
	private double Bz;
	
	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getVx() {
		return vx;
	}

	public void setVx(double vx) {
		this.vx = vx;
	}

	public double getVy() {
		return vy;
	}

	public void setVy(double vy) {
		this.vy = vy;
	}

	public double getAx() {
		return ax;
	}

	public void setAx(double ax) {
		this.ax = ax;
	}

	public double getAy() {
		return ay;
	}

	public void setAy(double ay) {
		this.ay = ay;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public double getCharge() {
		return charge;
	}

	public void setCharge(double charge) {
		this.charge = charge;
	}

	public double getPrevX() {
		return prevX;
	}

	public void setPrevX(double prevX) {
		this.prevX = prevX;
	}

	public double getPrevY() {
		return prevY;
	}

	public void setPrevY(double prevY) {
		this.prevY = prevY;
	}

	public double getChargedensity() {
		return chargedensity;
	}

	public void setChargedensity(double chargedensity) {
		this.chargedensity = chargedensity;
	}

	public double getEx() {
		return Ex;
	}

	public void setEx(double Ex) {
		this.Ex = Ex;
	}

	public double getEy() {
		return Ey;
	}

	public void setEy(double Ey) {
		this.Ey = Ey;
	}

	public double getBz() {
		return Bz;
	}

	public void setBz(double Bz) {
		this.Bz = Bz;
	}
	
	
	/** Empty constructor */
	public Particle() {
	}	
	
	/** Copy constructor */
	public Particle(Particle p) {
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

	
	public void storePosition() {
		prevX = x;
		prevY = y;
	}
	
	//a method that calculates the range from the center 0.0 for 2-dim
	public double rangeFromCenter2D()
	{
		return Math.sqrt(getX() * getX() + getY() * getY());
	}
	
	//a method that calculates the range between two particles in 2-dim
	public double rangeBetween2D(Particle a)
	{
		double range;
		range = Math.pow(this.getX() - a.getX(), 2) + Math.pow(this.getY() - this.getY(), 2);
		return Math.sqrt(range);
	}	
}
