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

		// remember for complete()
		p.setPrevPositionComponentForceX(f.getPositionComponentofForceX(p));
		p.setPrevPositionComponentForceY(f.getPositionComponentofForceY(p));
		p.setPrevBz(f.getBz(p));
		p.setPrevTangentVelocityComponentOfForceX(f.getTangentVelocityComponentOfForceX(p));
		p.setPrevTangentVelocityComponentOfForceY(f.getTangentVelocityComponentOfForceY(p));
		
		//calculating u(t + dt / 2). Although getV() and setV() are used, the represent the relativistic momentum, i.e. v->u
		double uxminus = p.getVx() + f.getPositionComponentofForceX(p) * step / (2.0 * p.getMass());
		
		double uyminus = p.getVy() + f.getPositionComponentofForceY(p) * step / (2.0 * p.getMass());
		
		//gamma(t)
		double gamma = relvelocity.calculateGamma(uxminus, uyminus);
		
		double t_z = p.getCharge() * f.getBz(p) * step / (2.0 * p.getMass() * gamma);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		double uxprime = uxminus + uyminus * t_z;
		double uyprime = uyminus - uxminus * t_z;
		
		double uxplus = uxminus + uyprime * s_z;
		double uyplus = uyminus - uxprime * s_z;
		
		p.setVx(uxplus + f.getPositionComponentofForceX(p) * step / (2.0 * p.getMass()) + f.getTangentVelocityComponentOfForceX(p) * step / p.getMass());
		p.setVy(uyplus + f.getPositionComponentofForceY(p) * step / (2.0 * p.getMass()) + f.getTangentVelocityComponentOfForceY(p) * step / p.getMass());
		
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
		p.setPrevPositionComponentForceX(f.getPositionComponentofForceX(p));
		p.setPrevPositionComponentForceY(f.getPositionComponentofForceY(p));
		p.setPrevBz(f.getBz(p));
		p.setPrevTangentVelocityComponentOfForceX(f.getTangentVelocityComponentOfForceX(p));
		p.setPrevTangentVelocityComponentOfForceY(f.getTangentVelocityComponentOfForceY(p));

		dt = - dt * 0.5;
		double uxminus = p.getVx() + f.getPositionComponentofForceX(p) * dt / (2.0 * p.getMass()) + f.getTangentVelocityComponentOfForceX(p) * dt / p.getMass();
	
		double uyminus = p.getVy() + f.getPositionComponentofForceY(p) * dt / (2.0 * p.getMass()) + f.getTangentVelocityComponentOfForceY(p) * dt / p.getMass();
	
		//gamma(t)
		double gamma = relvelocity.calculateGamma(uxminus, uyminus);
		double t_z = p.getCharge() * f.getBz(p) * dt / (2.0 * p.getMass() * gamma);   //t vector

		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		double uxprime = uxminus + uyminus * t_z;
		double uyprime = uyminus - uxminus * t_z;
		
		double uxplus = uxminus + uyprime * s_z;
		double uyplus = uyminus - uxprime * s_z;
		
		p.setVx(uxplus + f.getPositionComponentofForceX(p) * dt / (2.0 * p.getMass()));
		p.setVy(uyplus + f.getPositionComponentofForceY(p) * dt / (2.0 * p.getMass()));
	}

	/**
	 * complete method for bringing the velocity in the desired half step
	 * @param p before the update: v(t-dt/2);
	 *                 after the update: v(t)
	 */
	public void complete(Particle p, Force f, double dt)
	{
		dt = dt * 0.5;
		double uxminus = p.getVx() + p.getPrevPositionComponentForceX() * dt / (2.0 * p.getMass());
		
		double uyminus = p.getVy() + p.getPrevPositionComponentForceY() * dt / (2.0 * p.getMass());
		
		//gamma(t)
		double gamma = relvelocity.calculateGamma(uxminus, uyminus);
		double t_z = p.getCharge() * f.getBz(p) * dt / (2.0 * p.getMass() * gamma);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		double uxprime = uxminus + uyminus * t_z;
		double uyprime = uyminus - uxminus * t_z;
		
		double uxplus = uxminus + uyprime * s_z;
		double uyplus = uyminus - uxprime * s_z;
		
		p.setVx(uxplus + p.getPrevPositionComponentForceX() * dt / (2.0 * p.getMass()) + p.getPrevTangentVelocityComponentOfForceX() * dt / p.getMass());
		p.setVy(uyplus + p.getPrevPositionComponentForceY() * dt / (2.0 * p.getMass()) + p.getPrevTangentVelocityComponentOfForceY() * dt / p.getMass());
	}
}
