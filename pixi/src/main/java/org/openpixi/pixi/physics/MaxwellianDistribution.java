package org.openpixi.pixi.physics;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class MaxwellianDistribution implements ParticleLoader {

	private Random rand;
	//Boltzmann constant
	private double k;
	
	/*Velocity normalization in x direction, temperature dependent */
	private double vNormX;
	/*Velocity normalization in x direction, temperature dependent */
	private double vNormY;
	
	private double rnd1;
	private double rnd2;
	private double rnd3;
	
	/** Generates a thermal VELOCITY distribution
	 * TODO make this loader relativistic
	 * TODO THINK ABOUT USEFUL UNITS FOR K AND T! cgs?
	 * @param temperature
	 */
	public MaxwellianDistribution (long seed, double boltzmannk, double temperature) {
		
		rand = new Random(seed);
		k = boltzmannk;
		setNorms(temperature, temperature);
		
	}
	
	/** Generates a thermal VELOCITY distribution
	 * TODO make this loader relativistic
	 */
	public MaxwellianDistribution (long seed, double boltzmannk,
			double temperatureX, double temperatureY) {
		
		rand = new Random(seed);
		k = boltzmannk;
		setNorms(temperatureX, temperatureY);
		
	}
	
	private void setNorms (double temperatureX, double temperatureY) {
		//0.5 is the mass of the electron that is used later
		//Factor of 2 comes from the denominator in the exponent of
		//the Maxwellian distribution.
		//NOTE: There are no further factors because we are inverting
		//the cumulative distribution hence the factors cancel
		vNormX = Math.sqrt(2 * k * temperatureX / 0.5);
		vNormY = Math.sqrt(2 * k * temperatureY / 0.5);
	}
	
	
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
			p.setVx( vNormX * rnd1 * rnd3 );
			p.setVy( vNormY * rnd2 * rnd3 );		
			
			p.setCharge(-1);
			p.setMass(0.5);		
			p.setRadius(radius);
			
			particles.add(p);
		}
		
		//Generates a spatially uniform, cold and heavy ion background
		for (int i = 0; i < nX; i++) {
			for (int j = 0; j < nY; j++) {
				Particle p = new Particle();
				
				p.setX( (i+1) * deltaX );
				p.setY( (j+1) * deltaY );
				
				p.setVx(0);
				p.setVy(0);
				
				p.setCharge(-1);
				p.setMass(2000);		
				p.setRadius(radius);
				
				particles.add(p);
			}
		}
		
		return particles;
	}

}
