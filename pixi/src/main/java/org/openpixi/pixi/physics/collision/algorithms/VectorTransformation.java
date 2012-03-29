package org.openpixi.pixi.physics.collision.algorithms;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.collision.detectors.*;
import org.openpixi.pixi.physics.collision.util.Pair;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.solver.Solver;

public class VectorTransformation extends CollisionAlgorithm{
	
	//private Detector det;
	
	public VectorTransformation() {
		
		super();
	}
	
	private static double calculateAngle(double dx, double dy) {
		
		double angle = 0.0;
		if(dx < 0.) {
			angle = Math.PI + Math.atan(dy / dx);
		} else if(dx > 0. && dy > 0.) {
			angle = Math.atan(dy / dx);
		} else if(dx > 0. && dy < 0.) {
			angle = 2 * Math.PI + Math.atan(dy / dx);
		} else if( dx == 0. && dy == 0.) {
			angle = 0.0;
		} else if(dx == 0. && dy >= 0.) {
			angle = 0.5 * Math.PI;
		} else {
			angle = 3 * 0.5 * Math.PI;
		}
		
		return angle;
	}
	
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
	}
	
	public void collide(ArrayList<Pair<Particle2D, Particle2D>> pairs, Force f, Solver s, double step) {
		
		for(int i = 0; i < pairs.size(); i++) {
			Particle2D p1 = (Particle2D) pairs.get(i).getFirst();
			Particle2D p2 = (Particle2D) pairs.get(i).getSecond();
		
			double distance = Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
			if(distance <= (p1.radius + p2.radius)) {
				s.complete(p1, f, step);
				s.complete(p2, f, step);
				doCollision(p1, p2);
				//System.out.println("Collision! -> " + distance);
				s.prepare(p1, f, step);
				s.prepare(p2, f, step);
			}
		}
	}
}
