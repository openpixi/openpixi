/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openpixi.pixi.physics.collision.algorithms;

import java.util.ArrayList;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.collision.util.Pair;
import org.openpixi.pixi.physics.force.Force;
import org.openpixi.pixi.physics.solver.Solver;
import org.openpixi.pixi.physics.collision.detectors.KdTreeCollision;

/**
 *
 * @author Clemens
 */
public class TestCollisionDetection extends CollisionAlgorithm{
	public TestCollisionDetection()
	{
		super();
	}
	
	//COPIED FROM SIMPLE COLLISION
	
	private void doCollision(Particle p1, Particle p2) {
	/*
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

	*/

	double a = (p1.getVx()-p2.getVx())*(p1.getVx()-p2.getVx()) + (p1.getVy()-p2.getVy())*(p1.getVy()-p2.getVy());
	double b = (p2.getX()-p1.getX())*(p1.getVx()-p2.getVx()) + (p2.getY()-p1.getY())*(p1.getVy()-p2.getVy());
	double c = (p2.getX()-p1.getX())*(p2.getX()-p1.getX()) + (p2.getY()-p1.getY())*(p2.getY()-p1.getY()) - (p1.getRadius()+p2.getRadius())*(p1.getRadius()+p2.getRadius());


	//find the time where the particles actually collide
	double tau = (-b+Math.sqrt(b*b-a*c))/a;


	//Set the particles to the position where the actually collide
	p1.setX(p1.getX()-tau*p1.getVx());
	p1.setY(p1.getY()-tau*p1.getVy());
	p2.setX(p2.getX()-tau*p2.getVx());
	p2.setY(p2.getY()-tau*p2.getVy());


	//defining variables for cleaner calculation
	double m21 = p2.getMass() / p1.getMass();
	double x21 = p2.getX() - p1.getX();
	double y21 = p2.getY() - p1.getY();
	double vx21 = p2.getVx() - p1.getVx();
	double vy21 = p2.getVy() - p1.getVy();

	//avoiding dividing with zero
	double helpCoef = 1.0e-5 * Math.abs(y21); 
	if(Math.abs(x21) < helpCoef) {
		if(x21 < 0) {
			helpCoef = - helpCoef;
		}
		x21 = helpCoef;
	}

	//doing the calculation
	double angle = y21 / x21;
	double dvx2 = -2 * (vx21 + angle * vy21) / ((1 + angle * angle) * (1 + m21)) ;
	p2.setVx(p2.getVx() + dvx2);
	p2.setVy(p2.getVy() + angle * dvx2);
	p1.setVx(p1.getVx() - m21 * dvx2);
	p1.setVy(p1.getVy() - angle * m21 * dvx2);


	//Moving the particles to the Position after the collision
	p1.addX(p1.getVx()*tau);
	p1.addY(p1.getVy()*tau);
	p2.addX(p2.getVx()*tau);
	p2.addY(p2.getVy()*tau);
	}
	
	
	public void collide(ArrayList<Pair<Particle, Particle>> pairs, Force f, Solver s, double step) {
		
		ArrayList<Particle> allParticles = new ArrayList<Particle>();
		for(Pair<Particle,Particle> pair:pairs)
		{
			if(! allParticles.contains(pair.getFirst()))
				allParticles.add(pair.getFirst());
			if(! allParticles.contains(pair.getSecond()))
				allParticles.add(pair.getSecond());
		}
		KdTreeCollision kdtree = new KdTreeCollision(allParticles);
		kdtree.run();
		
		ArrayList<Pair<Particle,Particle>> pairs2 = kdtree.getOverlappedPairs();
		
		boolean itworks = true;
		
		for(int i = 0; i < pairs.size(); i++) {
			Particle p1 = (Particle) pairs.get(i).getFirst();
			Particle p2 = (Particle) pairs.get(i).getSecond();
		
			double distanceSquare = ((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY()));
			if(distanceSquare <= ((p1.getRadius() + p2.getRadius()) * (p1.getRadius() + p2.getRadius()))) {
				
				if(!(pairs2.contains(new Pair(p1,p2)))&&!pairs2.contains(new Pair(p2,p1)))
				{
					System.out.println("There was a collision not detected by the kd-Tree");
					itworks = false;
				}
				
				s.complete(p1, f, step);
				s.complete(p2, f, step);
				doCollision(p1, p2);
				s.prepare(p1, f, step);
				s.prepare(p2, f, step);
			}
		}
		if(itworks)
		{
			System.out.println("Every collision would be detected.");
		}
	}
}
