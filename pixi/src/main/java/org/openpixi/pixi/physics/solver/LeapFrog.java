/*The algorithm and the equations that are used one can be find here:
 * http://phycomp.technion.ac.il/~david/thesis/node34.html
 * and also here:
 * http://www.artcompsci.org/vol_1/v1_web/node34.html#leapfrog-step2
 * The first equations that are as comments now are just a different way of writing the algorithm.
 * The other equations that are used now one can find in the second link.
 * Personally I find the second way better, but it is just a question of taste.
 */

package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;

public class LeapFrog {
	public static void algorithm(Particle2D particle, Force f, double step) {
		
		/*particle.ax = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
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
		particle.ay = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;*/
		
		particle.ax = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		particle.ay = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		
		particle.vx += particle.ax * step * 0.5;
		particle.vy += particle.ay * step * 0.5;
		
		particle.x += particle.vx * step;
		particle.y += particle.vy * step;
		
		particle.ax = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		particle.ay = f.getForceX(particle.vx, particle.vy, particle) / particle.mass;
		
		particle.vx += particle.ax * step * 0.5;
		particle.vy += particle.ay * step * 0.5;
	}

}
