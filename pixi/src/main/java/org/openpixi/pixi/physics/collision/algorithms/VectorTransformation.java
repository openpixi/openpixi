package org.openpixi.pixi.physics.collision.algorithms;

import java.util.ArrayList;

import org.openpixi.pixi.physics.collision.util.Pair;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.Particle;
import org.openpixi.pixi.physics.solver.Solver;

public class VectorTransformation extends CollisionAlgorithm{
	
	public VectorTransformation() {
		
		super();
	}		
		
	private void doCollision(Particle p1, Particle p2) {
		
		//distance between the particles
		double distance = Math.sqrt((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY()));
		
		//finding the unit distance vector
		double dnX = (p1.getX() - p2.getX()) / distance;
		double dnY = (p1.getY() - p2.getY()) / distance;
		
		//finding the tangential vector;
		double dtX = dnY;
		double dtY = - dnX;
		
		//finding the minimal distance if the particles are overlapping
		double minDistanceX = dnX * (p1.getRadius() + p2.getRadius() - distance);
		double minDistanceY = dnY * (p1.getRadius() + p2.getRadius() - distance);
		
		//moving the particles if they are overlapping (if not, the minimal distance is equal to zero)
		p1.setX(p1.getX() + minDistanceX * p2.getMass() / (p1.getMass() + p2.getMass()));
		p1.setY(p1.getY() + minDistanceY * p2.getMass() / (p1.getMass() + p2.getMass()));
		p2.setX(p2.getX() - minDistanceX * p1.getMass() / (p1.getMass() + p2.getMass()));
		p2.setY(p2.getY() - minDistanceY * p1.getMass() / (p1.getMass() + p2.getMass()));
		
		//finding the normal and the tangential vectors of the particles corresponding with the collision plane
		double v1NX = dnX * (p1.getVx() * dnX + p1.getVy() * dnY);
		double v1NY = dnY * (p1.getVx() * dnX + p1.getVy() * dnY);
		double v1TX = dtX * (p1.getVx() * dtX + p1.getVy() * dtY);
		double v1TY = dtY * (p1.getVx() * dtX + p1.getVy() * dtY);
		
		double v2NX = dnX * (p2.getVx() * dnX + p2.getVy() * dnY);
		double v2NY = dnY * (p2.getVx() * dnX + p2.getVy() * dnY);
		double v2TX = dtX * (p2.getVx() * dtX + p2.getVy() * dtY);
		double v2TY = dtY * (p2.getVx() * dtX + p2.getVy() * dtY);
		
		//calculating the new velocities
		p1.setVx(v1TX + dnX * ((p1.getMass() - p2.getMass()) * Math.sqrt(v1NX * v1NX + v1NY * v1NY) + 2 * p2.getMass() * Math.sqrt(v2NX * v2NX + v2NY * v2NY)) / (p1.getMass() + p2.getMass()));
		
		p1.setVy(v1TY + dnY * ((p1.getMass() - p2.getMass()) * Math.sqrt(v1NX * v1NX + v1NY * v1NY) + 2 * p2.getMass() * Math.sqrt(v2NX * v2NX + v2NY * v2NY)) / (p1.getMass() + p2.getMass()));
		
		p2.setVx(v2TX - dnX * ((p2.getMass() - p1.getMass()) * Math.sqrt(v2NX * v2NX + v2NY * v2NY) + 2 * p1.getMass() * Math.sqrt(v1NX * v1NX + v1NY * v1NY)) / (p1.getMass() + p2.getMass()));
		
		p2.setVy(v2TY - dnY * ((p2.getMass() - p1.getMass()) * Math.sqrt(v2NX * v2NX + v2NY * v2NY) + 2 * p1.getMass() * Math.sqrt(v1NX * v1NX + v1NY * v1NY)) / (p1.getMass() + p2.getMass()));
	}
	
	public void collide(ArrayList<Pair<Particle, Particle>> pairs, Force f, Solver s, double step) {
		
		for(int i = 0; i < pairs.size(); i++) {
			Particle p1 = (Particle) pairs.get(i).getFirst();
			Particle p2 = (Particle) pairs.get(i).getSecond();
		
			double distanceSquare = ((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY()));
			if(distanceSquare <= ((p1.getRadius() + p2.getRadius()) * (p1.getRadius() + p2.getRadius()))) {
				s.complete(p1, f, step);
				s.complete(p2, f, step);
				doCollision(p1, p2);
				s.prepare(p1, f, step);
				s.prepare(p2, f, step);
			}
		}
	}
}
