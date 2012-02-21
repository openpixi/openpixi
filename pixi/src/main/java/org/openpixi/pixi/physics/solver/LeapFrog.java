/*The algorithm and the equations that are used can be find here:
 * http://phycomp.technion.ac.il/~david/thesis/node34.html
 */

package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;

public class LeapFrog {
	public static void algorithm(Particle2D particle, Force f, double step) {
		
		particle.ax = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		particle.ay = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		
		double vxminushalf = particle.vx;
		double vyminushalf = particle.vy;
		
		double vxplushalf = vxminushalf + particle.ax * step;;
		double vyplushalf = vyminushalf + particle.ay * step;;
		
		particle.vx = (vxplushalf);// - vxminushalf) / (double) 2;
		particle.vy = (vyplushalf);// - vyminushalf) / (double) 2;
		
		particle.x += vxplushalf * step;
		particle.y += vyplushalf * step;
		
		particle.ax = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		particle.ay = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
	}

}
