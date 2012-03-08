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
		
		double step1 = step;

		// remember for complete()
		//a(t) = F(v(t), x(t)) / m
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;
		
		/*double f_coef;
		//one need to divide the dragging coefficient with the mass, so the unit of help_coef is dimensionless
		double help_coef = f.drag * step1 / p.mass;
		
		double v_coef = Math.exp(-help_coef);
		
		if(Math.abs(help_coef) < 1.0e-5)
			f_coef = step1 * (1 + help_coef * (1 - help_coef/3)/2); //to avoid problems when f.drag = 0 is
		else
			f_coef = (1 - v_coef) / (f.drag / p.mass);*/
		
		double help1_coef = 1 - f.drag * step1 / (2 * p.mass);
		double help2_coef = 1 + f.drag * step1 / (2 * p.mass);
		
		double vxminus1 = help1_coef * p.vx / help2_coef + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass * help2_coef);
		double vxminus = p.vx + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass);
		double vxplus;
		double vxprime;
		
		double vyminus1 = help1_coef * p.vy / help2_coef + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass * help2_coef);
		double vyminus = p.vy + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass);
		double vyplus;
		double vyprime;
		
		double t_z = p.charge * f.bz * step / (2.0 * p.mass);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus1 + vyprime * s_z / help2_coef;
		vyplus = vyminus1 - vxprime * s_z / help2_coef;
		
		vxold = p.vx;
		vyold = p.vy;
		
		p.vx = vxplus + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass * help2_coef);
		p.vy = vyplus + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass * help2_coef);
		
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
	/*
	public void prepare(Particle2D p, Force f, double dt)
	{
		//dt = dt * 0.5;
		
		
		double help1_coef = 1 - f.drag * dt / (2 * p.mass);
		double help2_coef = 1 + f.drag * dt / (2 * p.mass);
		
		double vxminus = help1_coef * p.vx / help2_coef + f.getPositionComponentofForceX(p) * dt / (2.0 * p.mass * help2_coef);
		double vxplus;
		double vxprime;
		
		double vyminus = help1_coef * p.vy / help2_coef + f.getPositionComponentofForceY(p) * dt / (2.0 * p.mass * help2_coef);
		double vyplus;
		double vyprime;
		
		double t_z = p.charge * f.bz * dt / (2.0 * p.mass);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		p.vx = vxplus + f.getPositionComponentofForceX(p) * dt / (2.0 * p.mass * help2_coef);
		p.vy = vyplus + f.getPositionComponentofForceY(p) * dt / (2.0 * p.mass * help2_coef);
	}
	
	public void complete(Particle2D p, Force f, double dt)
	{

		/*double help1_coef = 1 - f.drag * dt / (2 * p.mass);
		double help2_coef = 1 + f.drag * dt / (2 * p.mass);
		
		double vxminus = help1_coef * p.vx / help2_coef + f.getPositionComponentofForceX(p) * dt / (2.0 * p.mass * help2_coef);
		double vxplus;
		double vxprime;
		
		double vyminus = help1_coef * p.vy / help2_coef + f.getPositionComponentofForceY(p) * dt / (2.0 * p.mass * help2_coef);
		double vyplus;
		double vyprime;
		
		double t_z = p.charge * f.bz * dt / (2.0 * p.mass);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		vxold = p.vx;
		vyold = p.vy;
		
		p.vx = vxplus + f.getPositionComponentofForceX(p) * dt / (2.0 * p.mass * help2_coef);
		p.vy = vyplus + f.getPositionComponentofForceY(p) * dt / (2.0 * p.mass * help2_coef);
		
		p.vx = (p.vx + vxold) * 0.5;
		p.vy = (p.vy + vyold) * 0.5;
	}
	*/

	public void prepare(Particle2D p, Force f, double dt)
	{
		//a(t) = F(v(t), x(t)) / m
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;

		//v(t + dt / 2) = v(t) + a(t)*dt / 2
		p.vx -= p.ax * dt / 2;
		p.vy -= p.ay * dt / 2;
	}

	public void complete(Particle2D p, Force f, double dt)
	{
		//v(t) = v(t + dt / 2) - a(t)*dt / 2
		p.vx += p.ax * dt / 2;
		p.vy += p.ay * dt / 2;
	}
}
