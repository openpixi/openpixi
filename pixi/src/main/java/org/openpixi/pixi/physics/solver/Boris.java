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
public class Boris implements Solver{
	
	double positionComponentForceX = 0.0;
	double positionComponentForceY = 0.0;
	double bZ = 0.0;
	double tangentVelocityComponentOfForceX = 0.0;
	double tangentVelocityComponentOfForceY = 0.0;
	

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
	public void step(Particle2D p, Force f, double step) {

		/*// remember for complete()
		//a(t) = F(v(t), x(t)) / m*/
		/*p.ax = f.getForceX(p) / p.mass;
		p.ay = f.getForceY(p) / p.mass;*/
		
		positionComponentForceX = f.getPositionComponentofForceX(p);
		positionComponentForceY = f.getPositionComponentofForceY(p);
		bZ = f.getBz(p);
		tangentVelocityComponentOfForceX = f.getTangentVelocityComponentOfForceX(p);
		tangentVelocityComponentOfForceY = f.getTangentVelocityComponentOfForceY(p);
		
		double vxminus = p.vx + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass);
		double vxplus;
		double vxprime;
		
		double vyminus = p.vy + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass);
		double vyplus;
		double vyprime;
		
		double t_z = p.charge * f.getBz(p) * step / (2.0 * p.mass);   //t vector
		//double t_z = Math.atan2(p.charge * f.getBz(p) * step / (2.0 * p.mass), 1);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		p.vx = vxplus + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass) + f.getTangentVelocityComponentOfForceX(p) * step / p.mass;
		p.vy = vyplus + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass) + f.getTangentVelocityComponentOfForceY(p) * step / p.mass;
		
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
		/*p.ax = f.getForceX(p) / p.mass;
		p.ay = f.getForceY(p) / p.mass;
		
		double vx = p.vx - p.ax * dt / 2;
		double vy = p.vy - p.ay * dt / 2;*/
		
		double step = - dt * 0.5;
		/*double vxminus = p.vx + positionComponentForceX * step / (2.0 * p.mass);
		double vxplus;
		double vxprime;
		
		double vyminus = p.vy + positionComponentForceY * step / (2.0 * p.mass);
		double vyplus;
		double vyprime;
		
		double t_z = p.charge * bZ * step / (2.0 * p.mass);   //t vector
		//double t_z = Math.atan2(p.charge * f.getBz(p) * step / (2.0 * p.mass), 1);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		p.vx = vxplus + positionComponentForceX * step / (2.0 * p.mass) + tangentVelocityComponentOfForceX * step / p.mass;
		p.vy = vyplus + positionComponentForceY * step / (2.0 * p.mass) + tangentVelocityComponentOfForceY * step / p.mass;
		*/
		
		double vxminus = p.vx + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass);
		double vxplus;
		double vxprime;
		
		double vyminus = p.vy + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass);
		double vyplus;
		double vyprime;
		
		double t_z = p.charge * f.getBz(p) * step / (2.0 * p.mass);   //t vector
		//double t_z = Math.atan2(p.charge * f.getBz(p) * step / (2.0 * p.mass), 1);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		p.vx = vxplus + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass) + f.getTangentVelocityComponentOfForceX(p) * step / p.mass;
		p.vy = vyplus + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass) + f.getTangentVelocityComponentOfForceY(p) * step / p.mass;
		
		//System.out.println("prepare difference x " + (double)(p.vx - vx));
		//System.out.println("prepare difference y " + (double)(p.vy - vy));
		/*System.out.println("prepare leapfrog x " + vx);
		System.out.println("prepare leapfrog y " + vy);
		System.out.println("prepare Boris x " + p.vx);
		System.out.println("prepare Boris y " + p.vy);*/


	}

	/**
	 * complete method for bringing the velocity in the desired half step
	 * @param p before the update: v(t-dt/2);
	 *                 after the update: v(t)
	 */
	public void complete(Particle2D p, Force f, double dt)
	{
		/*double vx = p.vx + p.ax * dt / 2;
		double vy = p.vy + p.ay * dt / 2;*/
		
		double step = dt * 0.5;
		double vxminus = p.vx + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass);
		double vxplus;
		double vxprime;
		
		double vyminus = p.vy + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass);
		double vyplus;
		double vyprime;
		
		double t_z = p.charge * f.getBz(p) * step / (2.0 * p.mass);   //t vector
		//double t_z = Math.atan2(p.charge * f.getBz(p) * step / (2.0 * p.mass), 1);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		p.vx = vxplus + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass) + f.getTangentVelocityComponentOfForceX(p) * step / p.mass;
		p.vy = vyplus + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass) + f.getTangentVelocityComponentOfForceY(p) * step / p.mass;
		
		//System.out.println("complete difference x " + (double)(p.vx - vx));
		//System.out.println("complete difference y " + (double)(p.vy - vy));


	}
}
