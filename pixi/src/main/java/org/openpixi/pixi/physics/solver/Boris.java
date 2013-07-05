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

package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.Particle;

/**The calculation is due to Boris and the equations((7) - (10)) can be found here:
 * http://ptsg.eecs.berkeley.edu/publications/Verboncoeur2005IOP.pdf
 */
public class Boris implements Solver{
	
	public Boris()
	{
		super();
	}
	
	/**
	 * Boris algorithm for implementing the electric and magnetic field.
	 * The damping is implemented with an linear error O(dt).
	 * Warning: the velocity is stored half a time step before of the position.
	 * @param p before the update: x(t), v(t-dt/2);
	 *                 after the update: x(t+dt), v(t+dt/2)
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

		double vxminus = p.getVx() + getPositionComponentofForceX * step / (2.0 * getMass);
		
		double vyminus = p.getVy() + getPositionComponentofForceY * step / (2.0 * getMass);
		
		double t_z = p.getCharge() * getBz * step / (2.0 * getMass);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		double vxprime = vxminus + vyminus * t_z;
		double vyprime = vyminus - vxminus * t_z;
		
		double vxplus = vxminus + vyprime * s_z;
		double vyplus = vyminus - vxprime * s_z;
		
		p.setVx(vxplus + getPositionComponentofForceX * step / (2.0 * getMass) + getTangentVelocityComponentOfForceX * step / getMass);
		p.setVy(vyplus + getPositionComponentofForceY * step / (2.0 * getMass) + getTangentVelocityComponentOfForceY * step / getMass);
		
		p.setX(p.getX() + p.getVx() * step);
		p.setY(p.getY() + p.getVy() * step);
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

		double step = -0.5 * dt;
		
		double vxminus = p.getVx() + getPositionComponentofForceX * step / (2.0 * getMass) + getTangentVelocityComponentOfForceX * step / getMass;
		
		double vyminus = p.getVy() + getPositionComponentofForceY * step / (2.0 * getMass) + getTangentVelocityComponentOfForceY * step / getMass;
		
		double t_z = p.getCharge() * getBz * step / (2.0 * getMass);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		double vxprime = vxminus + vyminus * t_z;
		double vyprime = vyminus - vxminus * t_z;
		
		double vxplus = vxminus + vyprime * s_z;
		double vyplus = vyminus - vxprime * s_z;
		
		p.setVx(vxplus + getPositionComponentofForceX * step / (2.0 * getMass));
		p.setVy(vyplus + getPositionComponentofForceY * step / (2.0 * getMass));
		
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
		double vxminus = p.getVx() + getPrevPositionComponentForceX * dt / (2.0 * getMass);
		
		double vyminus = p.getVy() + getPrevPositionComponentForceY * dt / (2.0 * getMass);
		
		double t_z = p.getCharge() * p.getPrevBz() * dt / (2.0 * getMass);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		double vxprime = vxminus + vyminus * t_z;
		double vyprime = vyminus - vxminus * t_z;
		
		double vxplus = vxminus + vyprime * s_z;
		double vyplus = vyminus - vxprime * s_z;
		
		p.setVx(vxplus + getPrevPositionComponentForceX * dt / (2.0 * getMass) + p.getPrevTangentVelocityComponentOfForceX() * dt / getMass);
		p.setVy(vyplus + getPrevPositionComponentForceY * dt / (2.0 * getMass) + p.getPrevTangentVelocityComponentOfForceY() * dt / getMass);
	}
}
