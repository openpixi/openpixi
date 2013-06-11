package org.openpixi.pixi.physics;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class MaxwellianDistribution implements ParticleLoader {

	private Random rand;
	private boolean usecutoff;
	
	/**Velocity normalization in x direction, temperature dependent */
	private double vnormX;
	/**Velocity normalization in x direction, temperature dependent */
	private double vnormY;
	/**Cutoff velocity SQUARED*/
	private double vcutoff;
	
	private double rnd1;
	private double rnd2;
	private double rnd3;
	
	/** Generates a thermal VELOCITY distribution
	 * TODO make this loader relativistic
	 * @param temperature
	 */
	public MaxwellianDistribution (long seed, double vthermal) {
		
		rand = new Random(seed);
		usecutoff = false;
		setNorms(vthermal, vthermal);
		
	}
	
	public MaxwellianDistribution (long seed, double vthermalX,
			double vthermalY) {
		
		rand = new Random(seed);
		usecutoff = false;
		setNorms(vthermalX, vthermalY);
		
	}
	
	public MaxwellianDistribution (long seed, double vthermalX,
			double vthermalY, double vcutoff) {
		
		rand = new Random(seed);
		usecutoff = true;
		this.vcutoff = vcutoff * vcutoff;
		setNorms(vthermalX, vthermalY);
		
	}
	
	private void setNorms (double vthermalX, double vthermalY) {
		//0.5 is the mass of the electron that is used later
		//Factor of 2 comes from the denominator in the exponent of
		//the Maxwellian distribution.
		//NOTE: There are no further factors because we are inverting
		//the cumulative distribution hence the factors cancel
		vnormX = Math.sqrt(2) * vthermalX;
		vnormY = Math.sqrt(2) * vthermalY;
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
			
			if (usecutoff) {
				rnd3 = Math.sqrt(
						(vcutoff*vcutoff - Math.log(rnd3 + (1-rnd3) * Math.exp(vcutoff*vcutoff)))
						/ rnd3);
				p.setVx( vnormX * rnd1 * rnd3 );
				p.setVy( vnormY * rnd2 * rnd3 );
			} else {
				rnd3 = Math.sqrt( - Math.log(rnd3) / rnd3 );
				p.setVx( vnormX * rnd1 * rnd3 );
				p.setVy( vnormY * rnd2 * rnd3 );
			}
			
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
				
				p.setCharge(1);
				p.setMass(2000);		
				p.setRadius(radius);
				
				particles.add(p);
			}
		}
		
		return particles;
	}

}
