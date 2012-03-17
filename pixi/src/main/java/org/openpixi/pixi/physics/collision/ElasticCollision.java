/*Since the whole calculation is a little bit tricky for one to understand just from looking at the code,
 * I shall do the calculation on a different place soon and make sure that there is a link here.
 * Practically I am first doing a coordinate change, where the x - axis is the collision line of the 2 particles,
 * than I am doing a simple calculation based on the conservation of the momentum to calculate the new velocities and on the end
 * I am making another transformation back to the normal x - y - coordinate system.
 */

package org.openpixi.pixi.physics.collision;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.collision.algorithms.CollisionAlgorithm;
import org.openpixi.pixi.physics.collision.detectors.Detector;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.solver.*;
import java.lang.Math;
import java.util.ArrayList;


public class ElasticCollision extends Collision{
	
	public ElasticCollision() {
		super();
	}
	
	/*public void doCollision(Particle2D p1, Particle2D p2)
	{
		//distance between the particles
		double distance = Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
		
		//finding the unit distance vector
		double dnX = (p1.x - p2.x) / distance;
		double dnY = (p1.y - p2.y) / distance;
		
		//finding the tangential vector;
		double dtX = dnY;
		double dtY = - dtX;
		
		//finding the minimal distance if the ball are overlapping
		double minDistanceX = dnX * (p1.radius + p2.radius - distance);
		double minDistanceY = dnY * (p1.radius + p2.radius - distance);
		
		//moving the balls if they are overlapping (if not, the minimal distance is equal to zero)
		p1.x += minDistanceX * p2.mass / (p1.mass + p2.mass);
		p1.y += minDistanceY * p2.mass / (p1.mass + p2.mass);
		p2.x -= minDistanceX * p1.mass / (p1.mass + p2.mass);
		p2.y -= minDistanceY * p1.mass / (p1.mass + p2.mass);
		
		//double convert = Math.PI / 180;
		double phi = 0.0;
		
		double dx = p2.x - p1.x;
		double dy = p2.y - p1.y;
		
		//finding the angle between the normal coordinate system and the system, where the x - axis is the collision line
		if(dx == 0.)
			phi = Math.PI;
		else
			phi = Math.atan(dy / dx);
		
		//double v1 = Math.sqrt(p1.vx * p1.vx + p1.vy * p1.vy);
		//double v2 = Math.sqrt(p2.vx * p2.vx + p2.vy * p2.vy);
		
		//double theta1 = Math.atan(p1.vy / p1.vx);
		//double theta2 = Math.atan(p2.vy / p2.vx);
		
		//calculating the velocities in the new coordinate system
		//double v1xNewCoor = v1 * Math.cos(theta1 - phi);
		//double v1yNewCoor = v1 * Math.sin(theta1 - phi);
		//double v2xNewCoor = v2 * Math.cos(theta2 - phi);
		//double v2yNewCoor = v2 * Math.sin(theta2 - phi);
		double v1xNewCoor = p1.vx * Math.cos(phi) + p1.vy * Math.sin(phi);
		double v1yNewCoor = - p1.vx * Math.sin(phi) + p1.vy * Math.cos(phi);
		double v2xNewCoor = p2.vx * Math.cos(phi) + p2.vy * Math.sin(phi);
		double v2yNewCoor = - p2.vx * Math.sin(phi) + p2.vy * Math.sin(phi);
		
		//calculating the new velocities in the new coordinate system
		//http://en.wikipedia.org/wiki/Elastic_collision
		double newv1xNewCoor = ((p1.mass - p2.mass) * v1xNewCoor + 2 * p2.mass * v2xNewCoor) / (p1.mass + p2.mass);
		double newv2xNewCoor = (2 * p1.mass * v1xNewCoor + (p2.mass - p1.mass) * v2xNewCoor) / (p1.mass + p2.mass);
		
		//going in the old coordinate system, do not forget that the y coordinates in the new coordinate system have not changed
		//also I am using here that cos(pi + x) = - sin(x) & sin(pi + x) = cos(x)
		p1.vx = newv1xNewCoor * Math.cos(phi) + v1yNewCoor * Math.cos(phi + Math.PI);
		p1.vy = newv1xNewCoor * Math.sin(phi) + v1yNewCoor * Math.sin(phi + Math.PI);
		p2.vx = newv2xNewCoor * Math.cos(phi) + v2yNewCoor * Math.cos(phi + Math.PI);
		p2.vy = newv2xNewCoor * Math.sin(phi) + v2yNewCoor * Math.sin(phi + Math.PI);
	}*/
	
	/*
	public void doCollision(Particle2D p1, Particle2D p2) {
		
		//distance between the particles
		double distance = Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
		
		//finding the unit distance vector
		double dnX = (p1.x - p2.x) / distance;
		double dnY = (p1.y - p2.y) / distance;
		
		//finding the tangential vector;
		double dtX = dnY;
		double dtY = - dnX;
		
		//finding the minimal distance if the particles are overlapping
		double minDistanceX = dnX * (p1.radius + p2.radius - distance);
		double minDistanceY = dnY * (p1.radius + p2.radius - distance);
		
		//moving the particles if they are overlapping (if not, the minimal distance is equal to zero)
		p1.x += minDistanceX * p2.mass / (p1.mass + p2.mass);
		p1.y += minDistanceY * p2.mass / (p1.mass + p2.mass);
		p2.x -= minDistanceX * p1.mass / (p1.mass + p2.mass);
		p2.y -= minDistanceY * p1.mass / (p1.mass + p2.mass);
		
		//finding the normal and the tangential vectors of the particles corresponding with the collision plane
		double v1NX = dnX * (p1.vx * dnX + p1.vy * dnY);
		double v1NY = dnY * (p1.vx * dnX + p1.vy * dnY);
		double v1TX = dtX * (p1.vx * dtX + p1.vy * dtY);
		double v1TY = dtY * (p1.vx * dtX + p1.vy * dtY);
		
		double v2NX = dnX * (p2.vx * dnX + p2.vy * dnY);
		double v2NY = dnY * (p2.vx * dnX + p2.vy * dnY);
		double v2TX = dtX * (p2.vx * dtX + p2.vy * dtY);
		double v2TY = dtY * (p2.vx * dtX + p2.vy * dtY);
		
		//calculating the new velocities
		p1.vx = v1TX + dnX * ((p1.mass - p2.mass) * Math.sqrt(v1NX * v1NX + v1NY * v1NY) / (p1.mass + p2.mass) +
				2 * p2.mass * Math.sqrt(v2NX * v2NX + v2NY * v2NY));
		p1.vy = v1TY + dnY * ((p1.mass - p2.mass) * Math.sqrt(v1NX * v1NX + v1NY * v1NY) / (p1.mass + p2.mass) +
				2 * p2.mass * Math.sqrt(v2NX * v2NX + v2NY * v2NY));
		p2.vx = v2TX + dnX * ((p2.mass - p1.mass) * Math.sqrt(v2NX * v2NX + v2NY * v2NY) / (p1.mass + p2.mass) +
				2 * p1.mass * Math.sqrt(v1NX * v1NX + v1NY * v1NY));
		p2.vy = v2TY + dnY * ((p2.mass - p1.mass) * Math.sqrt(v2NX * v2NX + v2NY * v2NY) / (p1.mass + p2.mass) +
				2 * p1.mass * Math.sqrt(v1NX * v1NX + v1NY * v1NY));
	}*/
	
	public void doCollision(Particle2D p1, Particle2D p2) {
		//distance between the particles
		double distance = Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
		
		//finding the unit distance vector
		double dnX = (p1.x - p2.x) / distance;
		double dnY = (p1.y - p2.y) / distance;
		
		//finding the tangential vector;
		double dtX = dnY;
		double dtY = - dtX;
		
		//finding the minimal distance if the ball are overlapping
		double minDistanceX = dnX * (p1.radius + p2.radius - distance);
		double minDistanceY = dnY * (p1.radius + p2.radius - distance);
		
		//moving the balls if they are overlapping (if not, the minimal distance is equal to zero)
		p1.x += minDistanceX * p2.mass / (p1.mass + p2.mass);
		p1.y += minDistanceY * p2.mass / (p1.mass + p2.mass);
		p2.x -= minDistanceX * p1.mass / (p1.mass + p2.mass);
		p2.y -= minDistanceY * p1.mass / (p1.mass + p2.mass);
		
		double m21 = p2.mass / p1.mass;
	    double x21 = p2.x - p1.x;
	    double y21 = p2.y - p1.y;
	    double vx21 = p2.vx - p1.vx;
	    double vy21 = p2.vy - p1.vy;
	    
	    double angle = y21 / x21;
	    double dvx2 = -2 * (vx21 + angle * vy21) / ((1 + angle * angle) * (1 + m21)) ;
	    p2.vx += dvx2;
	    p2.vy += angle * dvx2;
	    p1.vx -= m21 * dvx2;
	    p1.vy -= angle * m21 * dvx2;
	}
	
	/*for this method I need to integrate a few more things to avoid for example particles getting stuck together,
	 * and solve couple of more problems, be patient!
	 */

	public void check(ArrayList<Particle2D> parlist, Force f, Solver s, double step)
	{
		for(int i = 0; i < (parlist.size() - 1); i++)
		{
			Particle2D p1 = (Particle2D) parlist.get(i);
			//double x1 = Math.sqrt(p1.x * p1.x + p1.y * p1.y);
			for(int k = (i + 1); k < parlist.size(); k++)
			{
				Particle2D p2 = (Particle2D) parlist.get(k);
				double distance = Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
				if(distance <= (p1.radius + p2.radius)) {
					s.complete(p1, f, step);
					s.complete(p2, f, step);
					doCollision(p1, p2);
					s.prepare(p1, f, step);
					s.prepare(p2, f, step);
				}
			}
			
		}
	}

}
