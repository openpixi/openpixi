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

package org.openpixi.pixi.physics.solver.relativistic;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.Particle;
import org.openpixi.pixi.physics.solver.Solver;

/**The calculation is due to Boris and the equations((7) - (10)) can be found here:
 * http://ptsg.eecs.berkeley.edu/publications/Verboncoeur2005IOP.pdf
 */
public class BorisRelativistic implements Solver{
	
	RelativisticVelocity relvelocity;
	
	public BorisRelativistic(double c)
	{
		relvelocity = new RelativisticVelocity(c);
	}
	
	/**
	 * Boris algorithm for implementing the electric and magnetic field.
	 * The damping is implemented with an linear error O(dt).
	 * Warning: the velocity is stored half a time step before of the position.
	 * @param p before the update: x(t), u(t-dt/2);
	 *                 after the update: x(t+dt), u(t+dt/2)
	 *                 u(t) is the relativistic momentum
	 */
	public void step(Particle p, Force f, double step) {

		double getPositionComponentofForceX = f.getPositionComponentofForceX(p);
		double getPositionComponentofForceY = f.getPositionComponentofForceY(p);
		double getBz = f.getBz(p);
		double getTangentVelocityComponentOfForceX = f.getTangentVelocityComponentOfForceX(p);
		double getTangentVelocityComponentOfForceY = f.getTangentVelocityComponentOfForceY(p);
		double getMass = p.getMass();
		
		// remember for complete()
		p.setPrevPositionComponentForceX(getPositionComponentofForceX);
		p.setPrevPositionComponentForceY(getPositionComponentofForceY);
		p.setPrevBz(getBz);
		p.setPrevTangentVelocityComponentOfForceX(getTangentVelocityComponentOfForceX);
		p.setPrevTangentVelocityComponentOfForceY(getTangentVelocityComponentOfForceY);
		
		//calculating u(t + dt / 2). Although getV() and setV() are used, the represent the relativistic momentum, i.e. v->u
		double uxminus = p.getVx() + getPositionComponentofForceX * step / (2.0 * getMass);
		
		double uyminus = p.getVy() + getPositionComponentofForceY * step / (2.0 * getMass);
		
		//gamma(t)
		double gamma = relvelocity.calculateGamma(uxminus, uyminus);
		
		double t_z = p.getCharge() * getBz * step / (2.0 * getMass * gamma);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		double uxprime = uxminus + uyminus * t_z;
		double uyprime = uyminus - uxminus * t_z;
		
		double uxplus = uxminus + uyprime * s_z;
		double uyplus = uyminus - uxprime * s_z;
		
		p.setVx(uxplus + getPositionComponentofForceX * step / (2.0 * getMass) + getTangentVelocityComponentOfForceX * step / getMass);
		p.setVy(uyplus + getPositionComponentofForceY * step / (2.0 * getMass) + getTangentVelocityComponentOfForceY * step / getMass);
		
		//calculating gamma(t + dt / 2)
		gamma = relvelocity.calculateGamma(p);
		
		// x(t+dt) = u(t) + u(t+dt/2) * dt / gamma(t + dt / 2)
		p.setX(p.getX() + p.getVx() * step / gamma);
		p.setY(p.getY() + p.getVy() * step / gamma);
	}	
	
	/**
	 * prepare method for bringing the velocity in the desired half step
	 * @param p before the update: v(t);
	 *                 after the update: v(t-dt/2)
	 */
	public void prepare(Particle p, Force f, double dt)
	{	
		double getPositionComponentofForceX = f.getPositionComponentofForceX(p);
		double getPositionComponentofForceY = f.getPositionComponentofForceY(p);
		double getBz = f.getBz(p);
		double getTangentVelocityComponentOfForceX = f.getTangentVelocityComponentOfForceX(p);
		double getTangentVelocityComponentOfForceY = f.getTangentVelocityComponentOfForceY(p);
		double getMass = p.getMass();
		
		// remember for complete()
		p.setPrevPositionComponentForceX(getPositionComponentofForceX);
		p.setPrevPositionComponentForceY(getPositionComponentofForceY);
		p.setPrevBz(getBz);
		p.setPrevTangentVelocityComponentOfForceX(getTangentVelocityComponentOfForceX);
		p.setPrevTangentVelocityComponentOfForceY(getTangentVelocityComponentOfForceY);
		
		double step = - dt * 0.5;
		
		//calculating u(t + dt / 2). Although getV() and setV() are used, the represent the relativistic momentum, i.e. v->u
		double uxminus = p.getVx() + getPositionComponentofForceX * step / (2.0 * getMass) + getTangentVelocityComponentOfForceX * step / getMass;
		
		double uyminus = p.getVy() + getPositionComponentofForceY * step / (2.0 * getMass) + getTangentVelocityComponentOfForceY * step / getMass;
		
		//gamma(t)
		double gamma = relvelocity.calculateGamma(uxminus, uyminus);
		
		double t_z = p.getCharge() * getBz * step / (2.0 * getMass * gamma);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		double uxprime = uxminus + uyminus * t_z;
		double uyprime = uyminus - uxminus * t_z;
		
		double uxplus = uxminus + uyprime * s_z;
		double uyplus = uyminus - uxprime * s_z;
		
		p.setVx(uxplus + getPositionComponentofForceX * step / (2.0 * getMass));
		p.setVy(uyplus + getPositionComponentofForceY * step / (2.0 * getMass));
	}

	/**
	 * complete method for bringing the velocity in the desired half step
	 * @param p before the update: v(t-dt/2);
	 *                 after the update: v(t)
	 */
	public void complete(Particle p, Force f, double dt)
	{
		double getPrevPositionComponentForceX = p.getPrevPositionComponentForceX();
		double getPrevPositionComponentForceY = p.getPrevPositionComponentForceY();
		double getMass = p.getMass();
		
		dt = dt * 0.5;
		double uxminus = p.getVx() + getPrevPositionComponentForceX * dt / (2.0 * getMass);
		
		double uyminus = p.getVy() + getPrevPositionComponentForceY * dt / (2.0 * getMass);
		
		//gamma(t)
		double gamma = relvelocity.calculateGamma(uxminus, uyminus);
		double t_z = p.getCharge() * f.getBz(p) * dt / (2.0 * getMass * gamma);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		double uxprime = uxminus + uyminus * t_z;
		double uyprime = uyminus - uxminus * t_z;
		
		double uxplus = uxminus + uyprime * s_z;
		double uyplus = uyminus - uxprime * s_z;
		
		p.setVx(uxplus + getPrevPositionComponentForceX * dt / (2.0 * getMass) + p.getPrevTangentVelocityComponentOfForceX() * dt / getMass);
		p.setVy(uyplus + getPrevPositionComponentForceY * dt / (2.0 * getMass) + p.getPrevTangentVelocityComponentOfForceY() * dt / getMass);
	}
}
