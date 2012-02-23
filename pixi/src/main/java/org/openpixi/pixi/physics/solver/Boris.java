/*The calculation is due to Boris and the equations((7) - (10)) can be found here:
 * http://ptsg.eecs.berkeley.edu/publications/Verboncoeur2005IOP.pdf
 */


package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.ui.*;

public class Boris {
	public static void algorithm(Particle2D particle, Force f, double step) {
		
		double vxminus = particle.vx + particle.charge * f.ex * step / (2.0 * particle.mass);
		double vxplus;
		double vxprime;
		
		double vyminus = particle.vy + particle.charge * f.ey * step / (2.0 * particle.mass);;
		double vyplus;
		double vyprime;
		
		double t_z = particle.charge * f.bz * step / (2.0 * particle.mass);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		particle.vx = vxplus + particle.charge * f.ex * step / (2.0 * particle.mass);
		particle.vy = vyplus + particle.charge * f.ey * step / (2.0 * particle.mass);
		
		particle.x += particle.vx * step;
		particle.y += particle.vy * step;
		
		//until here is the Boris calculation
		//-----------------------------------------------------------------
		/*since I've could not find equations where in the Boris calculation the gravity and dragging
		 *implemented are, I have "updated" this algorithm by my self.
		 */
		//from here are the drag coefficient and the gravity added
		
		particle.ax = (- f.drag * particle.vx + particle.mass * f.gx) / particle.mass;
		particle.ay = (- f.drag * particle.vy + particle.mass * f.gy) / particle.mass;
		
		particle.vx += particle.ax * step / 2.0;
		particle.vy += particle.ay * step / 2.0;
		
		particle.x += particle.vx * step;
		particle.y += particle.vy * step;
		
		
	}
}
