package org.openpixi.pixi.physics.collision.algorithms;

import java.util.ArrayList;

import org.openpixi.pixi.physics.collision.util.Pair;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.particles.Particle;
import org.openpixi.pixi.physics.solver.Solver;

public class MatrixTransformation extends CollisionAlgorithm{
	
	public MatrixTransformation() {
		
		super();
	}
	
	private void doCollision(Particle p1, Particle p2) {
		
		//distance between the particles
		double distance = Math.sqrt((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY()));
		
		//finding the unit distance vector
		double dnX = (p1.getX() - p2.getX()) / distance;
		double dnY = (p1.getY() - p2.getY()) / distance;
		
		//finding the minimal distance if the ball are overlapping
		double minDistanceX = dnX * (p1.getRadius() + p2.getRadius() - distance);
		double minDistanceY = dnY * (p1.getRadius() + p2.getRadius() - distance);
		
		//moving the balls if they are overlapping (if not, the minimal distance is equal to zero)
		p1.setX(p1.getX() + minDistanceX * p2.getMass() / (p1.getMass() + p2.getMass()));
		p1.setY(p1.getY() + minDistanceY * p2.getMass() / (p1.getMass() + p2.getMass()));
		p2.setX(p2.getX() - minDistanceX * p1.getMass() / (p1.getMass() + p2.getMass()));
		p2.setY(p2.getY() - minDistanceY * p1.getMass() / (p1.getMass() + p2.getMass()));
		
		double phi = 0.0;
		
		double dx = p2.getX() - p1.getX();
		double dy = p2.getY() - p1.getY();
		
		phi = Math.atan2(dy, dx);
		
		double v1 = Math.sqrt(p1.getVx() * p1.getVx() + p1.getVy() * p1.getVy());
		double v2 = Math.sqrt(p2.getVx() * p2.getVx() + p2.getVy() * p2.getVy());
		
		double theta1 = Math.atan2(p1.getVy(), p1.getVx());
		double theta2 = Math.atan2(p2.getVy(), p2.getVx());
		
		//calculating the velocities in the new coordinate system
		double v1xNewCoor = v1 * Math.cos(theta1 - phi);
		double v1yNewCoor = v1 * Math.sin(theta1 - phi);
		double v2xNewCoor = v2 * Math.cos(theta2 - phi);
		double v2yNewCoor = v2 * Math.sin(theta2 - phi);
		
		//another transformation to go in the new coordinate system
		//double v1xNewCoor = p1.vx * Math.cos(phi) + p1.vy * Math.sin(phi);
		//double v1yNewCoor = - p1.vx * Math.sin(phi) + p1.vy * Math.cos(phi);
		//double v2xNewCoor = p2.vx * Math.cos(phi) + p2.vy * Math.sin(phi);
		//double v2yNewCoor = - p2.vx * Math.sin(phi) + p2.vy * Math.sin(phi);
		
		//calculating the new velocities in the new coordinate system
		//http://en.wikipedia.org/wiki/Elastic_collision
		double newv1xNewCoor = ((p1.getMass() - p2.getMass()) * v1xNewCoor + 2 * p2.getMass() * v2xNewCoor) / (p1.getMass() + p2.getMass());
		double newv2xNewCoor = (2 * p1.getMass() * v1xNewCoor + (p2.getMass() - p1.getMass()) * v2xNewCoor) / (p1.getMass() + p2.getMass());
		
		//going in the old coordinate system, do not forget that the y coordinates in the new coordinate system have not changed
		p1.setVx(newv1xNewCoor * Math.cos(phi) - v1yNewCoor * Math.sin(phi));
		p1.setVy(newv1xNewCoor * Math.sin(phi) + v1yNewCoor * Math.cos(phi));
		p2.setVx(newv2xNewCoor * Math.cos(phi) - v2yNewCoor * Math.sin(phi));
		p2.setVy(newv2xNewCoor * Math.sin(phi) + v2yNewCoor * Math.cos(phi));
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
