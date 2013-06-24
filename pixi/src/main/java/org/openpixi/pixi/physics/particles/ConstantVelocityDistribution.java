package org.openpixi.pixi.physics.particles;

import java.util.List;


public class ConstantVelocityDistribution{

	public static List<Particle> apply(List<Particle> particles, int startIndex, int endIndex,
			double constantVelocityX, double constantVelocityY) {
		
		for(int i = startIndex; i < endIndex; i++) {
			 particles.get(i).setVx(constantVelocityX);
			 particles.get(i).setVy(constantVelocityY);				 
		 }
		
		return particles;
	}
	
}
