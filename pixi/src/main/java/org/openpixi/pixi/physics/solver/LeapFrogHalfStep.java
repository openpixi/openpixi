package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.Force;
import org.openpixi.pixi.physics.Particle2D;

public class LeapFrogHalfStep extends Solver{
	
	public LeapFrogHalfStep()
	{
		super();
	}
	public void step(Particle2D p, Force f, double dt) {
		
		/**
		 * LeapFrog algorithm.
		 * The velocity is stored at the same times as the position.
		 * @param p before the update: x(t), v(t), a(t);
		 *                 after the update: x(t+dt), v(t+dt), a(t+dt)
		 */
		// v(t+dt/2) = v(t) + a(t)*dt/2
		p.vx += p.ax * dt / 2.0;
		p.vy += p.ay * dt / 2.0;

		// x(t+dt) = x(t) + v(t+dt/2)*dt
		p.x += p.vx * dt;
		p.y += p.vy * dt;

		// a(t+dt) = F(v(t+dt/2), x(t+dt)) / m
		// WARNING: Force is evaluated at two different times t+dt/2 and t+dt!
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;

		// v(t+dt) = v(t+dt/2) + a(t+dt)*dt/2
		p.vx += p.ax * dt / 2.0;
		p.vy += p.ay * dt / 2.0;
		
		/*
		 * double step1 = step / 2.0;
		
		double f_coef;
		//one need to divide the dragging coefficient with the mass, so the unit of help_coef is dimensionless
		double help_coef = f.drag * step1 / p.mass;
		
		double v_coef = Math.exp(-help_coef);
		
		if(Math.abs(help_coef) < 1.0e-5)
			f_coef = step1 * (1 + help_coef * (1 - help_coef/3)/2); //to avoid problems when f.drag = 0 is
		else
			f_coef = (1 - v_coef) / (f.drag / p.mass);
			
		p.vx = v_coef * p.vx + f_coef * p.ax * dt / 2.0;
		p.vy = v_coef * p.vy + f_coef * p.ay * dt / 2.0;

		// x(t+dt) = x(t) + v(t+dt/2)*dt
		p.x += p.vx * dt;
		p.y += p.vy * dt;

		// a(t+dt) = F(v(t+dt/2), x(t+dt)) / m
		// WARNING: Force is evaluated at two different times t+dt/2 and t+dt!
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;

		// v(t+dt) = v(t+dt/2) + a(t+dt)*dt/2
		p.vx = v_coef * p.vx + f_coef * p.ax * dt / 2.0;
		p.vy = v_coef * p.vy + f_coef * p.ay * dt / 2.0;
		 */
	}

}
