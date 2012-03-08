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

		// remember for complete()
		//a(t) = F(v(t), x(t)) / m
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;
		
		double vxminus = p.vx + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass);
		double vxplus;
		double vxprime;
		
		double vyminus = p.vy + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass);
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
		
		p.vx = vxplus + f.getPositionComponentofForceX(p) * step / (2.0 * p.mass) + f.getTangentVelocityComponentOfForceX(p) / p.mass;
		p.vy = vyplus + f.getPositionComponentofForceY(p) * step / (2.0 * p.mass) + f.getTangentVelocityComponentOfForceY(p) / p.mass;
		
		p.x += p.vx * step;
		p.y += p.vy * step;
	}

	public void prepare(Particle2D p, Force f, double dt)
	{
		//a(t) = F(v(t), x(t)) / m
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;

		//v(t - dt / 2) = v(t) - a(t)*dt / 2
		p.vx -= p.ax * dt / 2;
		p.vy -= p.ay * dt / 2;
	}

	public void complete(Particle2D p, Force f, double dt)
	{
		//v(t) = v(t - dt / 2) + a(t)*dt / 2
		p.vx += p.ax * dt / 2;
		p.vy += p.ay * dt / 2;
	}
}
