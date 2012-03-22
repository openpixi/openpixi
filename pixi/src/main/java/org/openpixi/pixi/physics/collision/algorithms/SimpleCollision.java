package org.openpixi.pixi.physics.collision.algorithms;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.collision.detectors.*;
import org.openpixi.pixi.physics.collision.util.Pair;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.solver.Solver;

public class SimpleCollision extends CollisionAlgorithm{
	
	//private Detector det;
	
	public SimpleCollision() {
		
		super();
	}
	
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
		
		double helpCoef = 1.0e-5 * Math.abs(y21); 
		//int sign = 1;
		if(Math.abs(x21) < helpCoef) {
			if(x21 < 0) {
				helpCoef = - helpCoef;
			} //else {
				//sign = 1;
			//}
			x21 = helpCoef;
		}
		
	    double angle = y21 / x21;
	    double dvx2 = -2 * (vx21 + angle * vy21) / ((1 + angle * angle) * (1 + m21)) ;
	    p2.vx += dvx2;
	    p2.vy += angle * dvx2;
	    p1.vx -= m21 * dvx2;
	    p1.vy -= angle * m21 * dvx2;
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
