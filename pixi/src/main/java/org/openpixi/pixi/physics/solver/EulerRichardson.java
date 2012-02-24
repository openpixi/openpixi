package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;

/**
 * This class is based on the simple Euler-Richardson algorithm (it
 * represents a neat way of finding the numerical solutions of a differential
 * equation, based on the Euler algorithm).
 * 
 * <p>See also:
 * <a href="http://www.physics.udel.edu/~bnikolic/teaching/phys660/numerical_ode/node4.html">
 * http://www.physics.udel.edu/~bnikolic/teaching/phys660/numerical_ode/node4.html</a>
 * </p>
 */
public class EulerRichardson extends Solver{
	
	public EulerRichardson()
	{
		super();
	}
	
	public void step(Particle2D p, Force f, double step)
	{
		//a(t) = F(v(t), x(t)) / m
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceX(p.vx, p.vy, p) / p.mass;
		
		//starting the Euler-Richardson algorithm (the equations correspond with the ones on the above mentioned website)
		//v(t + dt / 2) = v(t) + a(t) * dt / 2
		double vxmiddle = p.vx + p.ax * step / 2;
		double vymiddle = p.vy + p.ay * step / 2;
		
		//x(t + dt / 2) = x(t) + v(t) * dt / 2
		//double xmiddle = x + vx * step / 2;    actually, this two equations are not needed, but I've written them
		//double ymiddle = y + vy * step / 2;    so the algorithm is complete
		
		//a(t + dt / 2) = F(v(t + dt / 2), x(t)) / m
		double axmiddle = f.getForceX(vxmiddle, vymiddle, p) / p.mass;
		double aymiddle = f.getForceY(vxmiddle, vymiddle, p) / p.mass;
		
		//v(t + dt) = v(t) + a(t + dt / 2) * dt
		p.vx += axmiddle * step;
		p.vy += aymiddle * step;
		
		//x(t + dt) = x(t) + v(t + dt / 2) * dt
		p.x += vxmiddle * step;
		p.y += vymiddle * step;
		
		//a(t) = F(v(t + dt), x(t + dt)) / m
		p.ax = f.getForceX(p.vx, p.vy, p) / p.mass;
		p.ay = f.getForceY(p.vx, p.vy, p) / p.mass;
	}

}
