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
public class BorisRelativistic extends SolverRelativistic implements Solver{
	
	
	
	public BorisRelativistic(double c)
	{
		super(c);
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
		
		//finding u(t) in order to calculate gamma(t)
		double vx = p.getVx() + (p.getAx() * step / 2);
		double vy = p.getVy() + (p.getAy() * step / 2);

		double gamma = calculateGamma(vx, vy);
		
		double vxminus = p.getVx() + f.getPositionComponentofForceX(p) * step / (2.0 * p.getMass());
		double vxplus;
		double vxprime;
		
		double vyminus = p.getVy() + f.getPositionComponentofForceY(p) * step / (2.0 * p.getMass());
		double vyplus;
		double vyprime;
		
		double t_z = p.getCharge() * f.getBz(p) * step / (2.0 * p.getMass() * gamma);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		p.setVx(vxplus + f.getPositionComponentofForceX(p) * step / (2.0 * p.getMass()) + f.getTangentVelocityComponentOfForceX(p) * step / p.getMass());
		p.setVy(vyplus + f.getPositionComponentofForceY(p) * step / (2.0 * p.getMass()) + f.getTangentVelocityComponentOfForceY(p) * step / p.getMass());
		
		//calculating gamma(t + dt / 2)
		gamma = calculateGamma(p.getVx(), p.getVy());
		
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
		double vxminus = p.getVx() + f.getPositionComponentofForceX(p) * dt / (2.0 * p.getMass()) + f.getTangentVelocityComponentOfForceX(p) * dt / p.getMass();
		double vxplus;
		double vxprime;
		
		double vyminus = p.getVy() + f.getPositionComponentofForceY(p) * dt / (2.0 * p.getMass()) + f.getTangentVelocityComponentOfForceY(p) * dt / p.getMass();
		double vyplus;
		double vyprime;
		
		double t_z = p.getCharge() * f.getBz(p) * dt / (2.0 * p.getMass());   //t vector

		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		p.setVx(vxplus + f.getPositionComponentofForceX(p) * dt / (2.0 * p.getMass()));
		p.setVy(vyplus + f.getPositionComponentofForceY(p) * dt / (2.0 * p.getMass()));
	}

	/**
	 * complete method for bringing the velocity in the desired half step
	 * @param p before the update: v(t-dt/2);
	 *                 after the update: v(t)
	 */
	public void complete(Particle p, Force f, double dt)
	{
		dt = dt * 0.5;
		double vxminus = p.getVx() + p.getPrevPositionComponentForceX() * dt / (2.0 * p.getMass());
		double vxplus;
		double vxprime;
		
		double vyminus = p.getVy() + p.getPrevPositionComponentForceY() * dt / (2.0 * p.getMass());
		double vyplus;
		double vyprime;
		
		double t_z = p.getCharge() * p.getPrevBz() * dt / (2.0 * p.getMass());   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		p.setVx(vxplus + p.getPrevPositionComponentForceX() * dt / (2.0 * p.getMass()) + p.getPrevTangentVelocityComponentOfForceX() * dt / p.getMass());
		p.setVy(vyplus + p.getPrevPositionComponentForceY() * dt / (2.0 * p.getMass()) + p.getPrevTangentVelocityComponentOfForceY() * dt / p.getMass());
	}
}
