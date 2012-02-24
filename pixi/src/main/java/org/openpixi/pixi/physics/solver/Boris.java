/*The calculation is due to Boris and the equations((7) - (10)) can be found here:
 * http://ptsg.eecs.berkeley.edu/publications/Verboncoeur2005IOP.pdf
 */


package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.ui.*;

public class Boris extends Solver{
	
	public Boris()
	{
		super();
	}
	
	public static void step(Particle2D p, Force f, double step) {
		
		//step = step / 2.0;
		
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
		
		p.vx = vxplus + p.charge * f.getPositionComponentofForceX(p) * step / (2.0 * p.mass);
		p.vy = vyplus + p.charge * f.getPositionComponentofForceY(p) * step / (2.0 * p.mass);
		
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
}
