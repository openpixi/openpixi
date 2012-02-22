
package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;

public class Boris {
	public static void algorithm(Particle2D particle, Force f, double step) {
		
		//particle.ax = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		//particle.ay = f.getForceY(particle.vx, particle.vy, particle) / particle.mass;
		
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
		
	}
}
