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
public class BorisDamped implements Solver{
	
	public BorisDamped()
	{
		super();
	}
	
	/**
	 * Boris algorithm for implementing the electric and magnetic field.
	 * The damping is implemented with an error O(dt^2), the same error of accuracy that the algorithm has.
	 * Warning: the velocity is stored half a time step before of the position.
	 * @param p before the update: x(t), v(t-dt/2);
	 *                 after the update: x(t+dt), v(t+dt/2)
	 */
	public void step(Particle p, Force f, double step) {

		double getPositionComponentofForceX = f.getPositionComponentofForceX(p);
		double getPositionComponentofForceY = f.getPositionComponentofForceY(p);
		double getBz = f.getBz(p);
		double getLinearDragCoefficient = f.getLinearDragCoefficient(p);
		double getMass = p.getMass();
		
		// remember for complete()
		p.setPrevPositionComponentForceX(getPositionComponentofForceX);
		p.setPrevPositionComponentForceY(getPositionComponentofForceY);
		p.setPrevBz(getBz);
		p.setPrevLinearDragCoefficient(getLinearDragCoefficient);
		
		//help coefficients for the dragging
		double help1_coef = 1 - getLinearDragCoefficient * step / (2 * getMass);
		double help2_coef = 1 + getLinearDragCoefficient * step / (2 * getMass);
		
		double vxminus = help1_coef * p.getVx() / help2_coef + getPositionComponentofForceX * step / (2.0 * getMass * help2_coef);
		double vyminus = help1_coef * p.getVy() / help2_coef + getPositionComponentofForceY * step / (2.0 * getMass * help2_coef);
		
		double t_z = p.getCharge() * getBz * step / (2.0 * getMass * help2_coef);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		double kappa = - 4 * getMass * getLinearDragCoefficient * step / (4 * getMass * getMass - 
				getLinearDragCoefficient * getLinearDragCoefficient * step * step);
		
		double vxprime = vxminus + help2_coef * vyminus * t_z / help1_coef + kappa * step * getPositionComponentofForceY * t_z / (2.0 * getMass);;
		double vyprime = vyminus - help2_coef * vxminus * t_z / help1_coef - kappa * step * getPositionComponentofForceX * t_z / (2.0 * getMass);;
		
		double vxplus = vxminus + vyprime * s_z + (help2_coef / help1_coef - 1) * (vyminus * t_z + vxminus * t_z * t_z) / (1 + t_z * t_z) +
				kappa * step * (getPositionComponentofForceY + getPositionComponentofForceX * t_z) * s_z / (4.0 * getMass);
		
		double vyplus = vyminus - vxprime * s_z + (help2_coef / help1_coef - 1) * (- vxminus * t_z + vyminus * t_z * t_z) / (1 + t_z * t_z) -
				kappa * step * (getPositionComponentofForceX - getPositionComponentofForceY * t_z) * s_z / (4.0 * getMass);
	
		p.setVx(vxplus + getPositionComponentofForceX * step / (2.0 * getMass * help2_coef));
		p.setVy(vyplus + getPositionComponentofForceY * step / (2.0 * getMass * help2_coef));
		
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
		double getLinearDragCoefficient = f.getLinearDragCoefficient(p);
		double getMass = p.getMass();
		
		// remember for complete()
		p.setPrevPositionComponentForceX(getPositionComponentofForceX);
		p.setPrevPositionComponentForceY(getPositionComponentofForceY);
		p.setPrevBz(getBz);
		p.setPrevLinearDragCoefficient(getLinearDragCoefficient);
		
		double step = - dt * 0.5;
		
		//help coefficients for the dragging
		double help1_coef = 1 - getLinearDragCoefficient * step / (2 * getMass);
		double help2_coef = 1 + getLinearDragCoefficient * step / (2 * getMass);
		
		double vxminus = help1_coef * p.getVx() / help2_coef + getPositionComponentofForceX * step / (2.0 * getMass * help2_coef);
		double vyminus = help1_coef * p.getVy() / help2_coef + getPositionComponentofForceY * step / (2.0 * getMass * help2_coef);
		
		double t_z = p.getCharge() * getBz * step / (2.0 * getMass * help2_coef);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		double kappa = - 4 * getMass * getLinearDragCoefficient * step / (4 * getMass * getMass - 
				getLinearDragCoefficient * getLinearDragCoefficient * step * step);
		
		double vxprime = vxminus + help2_coef * vyminus * t_z / help1_coef + kappa * step * getPositionComponentofForceY * t_z / (2.0 * getMass);;
		double vyprime = vyminus - help2_coef * vxminus * t_z / help1_coef - kappa * step * getPositionComponentofForceX * t_z / (2.0 * getMass);;
		
		double vxplus = vxminus + vyprime * s_z + (help2_coef / help1_coef - 1) * (vyminus * t_z + vxminus * t_z * t_z) / (1 + t_z * t_z) +
				kappa * step * (getPositionComponentofForceY + getPositionComponentofForceX * t_z) * s_z / (4.0 * getMass);
		
		double vyplus = vyminus - vxprime * s_z + (help2_coef / help1_coef - 1) * (- vxminus * t_z + vyminus * t_z * t_z) / (1 + t_z * t_z) -
				kappa * step * (getPositionComponentofForceX - getPositionComponentofForceY * t_z) * s_z / (4.0 * getMass);
	
		p.setVx(vxplus + getPositionComponentofForceX * step / (2.0 * getMass * help2_coef));
		p.setVy(vyplus + getPositionComponentofForceY * step / (2.0 * getMass * help2_coef));		
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
		double getPrevLinearDragCoefficient = p.getPrevLinearDragCoefficient();
		double getMass = p.getMass();
		
		double step = dt * 0.5;
		
		//help coefficients for the dragging
		double help1_coef = 1 - getPrevLinearDragCoefficient * step / (2 * getMass);
		double help2_coef = 1 + getPrevLinearDragCoefficient * step / (2 * getMass);
		
		double vxminus = help1_coef * p.getVx() / help2_coef + getPrevPositionComponentForceX * step / (2.0 * getMass * help2_coef);
		double vyminus = help1_coef * p.getVy() / help2_coef + getPrevPositionComponentForceY * step / (2.0 * getMass * help2_coef);
		
		double t_z = p.getCharge() * p.getPrevBz() * step / (2.0 * getMass * help2_coef);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		double kappa = - 4 * getMass * getPrevLinearDragCoefficient * step / (4 * getMass * getMass - 
				getPrevLinearDragCoefficient * getPrevLinearDragCoefficient * step * step);
		
		double vxprime = vxminus + help2_coef * vyminus * t_z / help1_coef + kappa * step * getPrevPositionComponentForceY * t_z / (2.0 * getMass);;
		double vyprime = vyminus - help2_coef * vxminus * t_z / help1_coef - kappa * step * getPrevPositionComponentForceX * t_z / (2.0 * getMass);;
		
		double vxplus = vxminus + vyprime * s_z + (help2_coef / help1_coef - 1) * (vyminus * t_z + vxminus * t_z * t_z) / (1 + t_z * t_z) +
				kappa * step * (getPrevPositionComponentForceY + getPrevPositionComponentForceX * t_z) * s_z / (4.0 * getMass);
		
		double vyplus = vyminus - vxprime * s_z + (help2_coef / help1_coef - 1) * (- vxminus * t_z + vyminus * t_z * t_z) / (1 + t_z * t_z) -
				kappa * step * (getPrevPositionComponentForceX - getPrevPositionComponentForceY * t_z) * s_z / (4.0 * getMass);
	
		p.setVx(vxplus + getPrevPositionComponentForceX * step / (2.0 * getMass * help2_coef));
		p.setVy(vyplus + getPrevPositionComponentForceY * step / (2.0 * getMass * help2_coef));
	}
}
