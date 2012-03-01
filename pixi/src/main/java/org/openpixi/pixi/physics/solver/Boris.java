/*The calculation is due to Boris and the equations((7) - (10)) can be found here:
 * http://ptsg.eecs.berkeley.edu/publications/Verboncoeur2005IOP.pdf
 */


package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.ui.*;

public class Boris extends Solver{
	
	private double vxold;
	private double vyold;
	
	public Boris()
	{
		super();
	}
	
	public void step(Particle2D p, Force f, double step) {
		
		double step1 = step / 2.0;
		
		double f_coef;
		//one need to divide the dragging coefficient with the mass, so the unit of help_coef is dimensionless
		double help_coef = f.drag * step1 / p.mass;
		
		double v_coef = Math.exp(-help_coef);
		
		if(Math.abs(help_coef) < 1.0e-5)
			f_coef = step1 * (1 + help_coef * (1 - help_coef/3)/2); //to avoid problems when f.drag = 0 is
		else
			f_coef = (1 - v_coef) / (f.drag / p.mass);
		
		double vxminus = v_coef * p.vx + f_coef * f.getPositionComponentofForceX(p) * step / (2.0 * p.mass);
		double vxplus;
		double vxprime;
		
		double vyminus = v_coef * p.vy + f_coef * f.getPositionComponentofForceY(p) * step / (2.0 * p.mass);
		double vyplus;
		double vyprime;
		
		double t_z = p.charge * f.bz * step / (2.0 * p.mass);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		vxold = p.vx;
		vyold = p.vy;
		
		p.vx = v_coef * vxplus + f_coef * f.getPositionComponentofForceX(p) * step / (2.0 * p.mass);
		p.vy = v_coef * vyplus + f_coef * f.getPositionComponentofForceY(p) * step / (2.0 * p.mass);
		
		p.x += p.vx * step;
		p.y += p.vy * step;
		
		//until here is the Boris calculation
		//-----------------------------------------------------------------
		/*since I've could not find equations where in the Boris calculation the gravity and dragging
		 *implemented are, I have "updated" this algorithm by my self.
		 */
		//from here are the drag coefficient and the gravity added
		
		//p.ax = (- f.drag * p.vx + p.mass * f.gx) / p.mass;
		//p.ay = (- f.drag * p.vy + p.mass * f.gy) / p.mass;
		
		//p.vx += p.ax * step / 2.0;
		//p.vy += p.ay * step / 2.0;
		/*
		p.ax = (f.getTangentVelocityComponentOfForceX(p)) / p.mass;
		p.ay = (f.getTangentVelocityComponentOfForceY(p)) / p.mass;
		
		p.vx += p.ax * step / 2.0;
		p.vy += p.ay * step / 2.0;
		
		p.x += p.vx * step;
		p.y += p.vy * step;
		
		*/
	}
	
	/*public void prepare(Particle2D p, Force f, double dt)
	{
		dt = dt / 2;
		double vxminus = p.vx + f.getPositionComponentofForceX(p) * dt / (2.0 * p.mass);
		double vxplus;
		double vxprime;
		
		double vyminus = p.vy + f.getPositionComponentofForceY(p) * dt / (2.0 * p.mass);
		double vyplus;
		double vyprime;
		
		double t_z = p.charge * f.bz * dt / (2.0 * p.mass);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		p.vx = vxplus + p.charge * f.getPositionComponentofForceX(p) * dt / (2.0 * p.mass);
		p.vy = vyplus + p.charge * f.getPositionComponentofForceY(p) * dt / (2.0 * p.mass);
		
	}
	
	public void complete(Particle2D p, Force f, double dt)
	{
		dt = dt / 2;
		double vxminus = p.vx + f.getPositionComponentofForceX(p) * dt / (2.0 * p.mass);
		double vxplus;
		double vxprime;
		
		double vyminus = p.vy + f.getPositionComponentofForceY(p) * dt / (2.0 * p.mass);
		double vyplus;
		double vyprime;
		
		double t_z = p.charge * f.bz * dt / (2.0 * p.mass);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		//p.vx = vxplus - p.charge * f.getPositionComponentofForceX(p) * dt / (2.0 * p.mass);
		//p.vy = vyplus - p.charge * f.getPositionComponentofForceY(p) * dt / (2.0 * p.mass);
		
		p.vx = (2 * p.mass * p.vx - 2 * p.mass * s_z * vyold - 2 * f.getPositionComponentofForceX(p) * p.charge * dt - 
				f.getPositionComponentofForceY(p) * p.charge * s_z * dt - 
				f.getPositionComponentofForceX(p) * p.charge * s_z * t_z * dt) / (2 * p.mass * (1 + s_z + t_z));
		p.vy = (2 * p.mass * p.vy - 2 * p.mass * s_z * vxold - 2 * f.getPositionComponentofForceY(p) * p.charge * dt - 
				f.getPositionComponentofForceX(p) * p.charge * s_z * dt - 
				f.getPositionComponentofForceY(p) * p.charge * s_z * t_z * dt) / (2 * p.mass * (1 + s_z + t_z));
	}*/
	
}
