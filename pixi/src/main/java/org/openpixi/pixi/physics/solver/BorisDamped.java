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

/**The calculation is due to Boris and the equations((7) - (10)) can be found here:
 * http://ptsg.eecs.berkeley.edu/publications/Verboncoeur2005IOP.pdf
 */
public class BorisDamped extends Solver{
	
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
	public void step(Particle2D p, Force f, double step) {

		// remember for complete()
		//a(t) = F(v(t), x(t)) / m
		p.ax = (f.getPositionComponentofForceX(p) + f.getNormalVelocityComponentofForceX(p)) / p.mass;
		p.ay = (f.getPositionComponentofForceY(p) + f.getNormalVelocityComponentofForceY(p)) / p.mass;
		
		//help coefficients for the dragging
		double help1_coef = 1 - f.getLinearDragCoefficient(p) * step / (2 * p.mass);
		double help2_coef = 1 + f.getLinearDragCoefficient(p) * step / (2 * p.mass);
		
		double vxminus1 = help1_coef * p.vx / help2_coef + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass * help2_coef);
		double vxminus = p.vx + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass);
		double vxplus;
		double vxprime;
		
		double vyminus1 = help1_coef * p.vy / help2_coef + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass * help2_coef);
		double vyminus = p.vy + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass);
		double vyplus;
		double vyprime;
		
		double t_z = p.charge * f.getBz(p) * step / (2.0 * p.mass);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus1 + vyprime * s_z / help2_coef;
		vyplus = vyminus1 - vxprime * s_z / help2_coef;
	
		p.vx = vxplus + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass * help2_coef);
		p.vy = vyplus + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass * help2_coef);
		
		p.x += p.vx * step;
		p.y += p.vy * step;
		
	}
	/**
	 * prepare method for bringing the velocity in the desired half step
	 * @param p before the update: v(t);
	 *                 after the update: v(t-dt/2)
	 */
	public void prepare(Particle2D p, Force f, double dt)
	{
		double help1_coef = 1 - f.getLinearDragCoefficient(p) * dt / (2 * p.mass);
		double help2_coef = 1 + f.getLinearDragCoefficient(p) * dt / (2 * p.mass);
		
		//a(t) = F(v(t), x(t)) / m
		p.ax = (f.getPositionComponentofForceX(p) + f.getNormalVelocityComponentofForceX(p)) / p.mass;
		p.ay = (f.getPositionComponentofForceY(p) + f.getNormalVelocityComponentofForceY(p)) / p.mass;

		//v(t - dt / 2) = v(t) - a(t)*dt / 2
		p.vx = (p.vx * help2_coef - p.ax * dt * 0.5) / help1_coef;
		p.vy = (p.vy * help2_coef - p.ay * dt * 0.5) / help1_coef;
	}
	/**
	 * complete method for bringing the velocity in the desired half step
	 * @param p before the update: v(t-dt/2);
	 *                 after the update: v(t)
	 */
	public void complete(Particle2D p, Force f, double dt)
	{
		double help1_coef = 1 - f.getLinearDragCoefficient(p) * dt / (2 * p.mass);
		double help2_coef = 1 + f.getLinearDragCoefficient(p) * dt / (2 * p.mass);
		
		//v(t) = v(t - dt / 2) + a(t)*dt / 2
		p.vx = (p.vx * help1_coef + p.ax * dt * 0.5) / help2_coef;
		p.vy = (p.vy * help1_coef + p.ay * dt * 0.5) / help2_coef;
	}
}
