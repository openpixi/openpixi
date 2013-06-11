package org.openpixi.pixi.physics;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class MaxwellianDistribution implements ParticleLoader {

	private Random rand = new Random(1);
	double rnd1;
	double rnd2;
	double rnd3;
	
	/* Generates a thermal VELOCITY distribution
	 * TODO make this loader relativistic
	 */
	@Override
	public List<Particle> load (int numOfParticles, int numCellsX, int numCellsY,
			double simulationWidth, double simulationHeight, double radius) {
		
		int numOfElectrons= (numOfParticles / 2);
		int numOfIons = numOfParticles - (numOfParticles / 2);
		
		//WARNING: THIS CODE WILL VIOLATE THE ASSERT CONDITION IN MOST CASES
		int nX = (int) Math.sqrt((simulationWidth / simulationHeight) * numOfIons);
		int nY = (int) numOfParticles / nX;
		
		//2 added in the numerator because we do not want to place
		//the ions on the boundaries
		double deltaX = simulationWidth / (nX+2);
		double deltaY = simulationHeight / (nY+2);
		
		if (Debug.asserts) {
			assert nX*nY == numOfIons: nX*nY;
		}
		
		List<Particle> particles = new ArrayList<Particle>();
		
		//Generates thermal electrons that are randomly distributed
		//across the simulation area
		for (int i = 0; i < numOfElectrons; i++) {
			Particle p = new Particle();
			
			p.setX( simulationWidth * rand.nextDouble());
			p.setY( simulationHeight * rand.nextDouble());
			
			do {
				rnd1 = rand.nextDouble();
				rnd2 = rand.nextDouble();
				rnd3 = (rnd1*rnd1 + rnd2*rnd2);
			} while (rnd3 > 1);
			
			rnd3 = Math.sqrt( - Math.log(rnd3) / rnd3 );
			p.setVx( rnd1 * rnd3 );
			p.setVy( rnd2 * rnd3 );		
			
			p.setCharge(-1);
			p.setMass(0.5);		
			p.setRadius(radius);
			
			particles.add(p);
		}
		
		//Generates a spatially uniform, cold and heavy ion background
		for (int i = 0; i < numOfIons; i++) {
			Particle p = new Particle();
			
			p.setX( (i+1) * deltaX );
			p.setY( (i+1) * deltaY );
			
			p.setVx(0);
			p.setVy(0);
			
			p.setCharge(-1);
			p.setMass(2000);		
			p.setRadius(radius);
			
			particles.add(p);
		}
		
		return null;
	}

}
