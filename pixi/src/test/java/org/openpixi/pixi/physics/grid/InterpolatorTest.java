package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import junit.framework.TestCase;
import org.openpixi.pixi.physics.grid.Interpolator;
import org.openpixi.pixi.physics.InitialConditions;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.grid.GridFactory;

public class InterpolatorTest extends TestCase {
	
	double ACCURACY_LIMIT = 1.e-16;
	
	public InterpolatorTest(String testName) {
		super(testName);
	}
	
	public void testChargeInterpolation() {
		
		Simulation s = InitialConditions.initEmptySimulation();
		s.width = 100;
		s.height = 100;
		s.c = Math.sqrt(s.width*s.width+s.height*s.height)/5;
		s.particles = InitialConditions.createRandomParticles(s.width, s.height, s.c, 100, 1);
		
		Grid g = GridFactory.createYeeGrid(s, 10, 10, s.width, s.height);
		
		assertEquals(getTotalParticleCharge(s.particles), getChargedensitySum(g), ACCURACY_LIMIT);
	}
	
	public static double getChargedensitySum(Grid grid) {
		double sum = 0;
		for (int x = -1; x < grid.getNumCellsX()+1; x++) {
			for (int y = -1; y < grid.getNumCellsY()+1; y++) {
				sum += grid.getRho(x, y);
			}
		}
		return sum;
	}
	
	public static double getTotalParticleCharge(ArrayList<Particle> particles) {
		double sum = 0;
		for (Particle p : particles) {
			sum += p.getCharge();
		}
		return sum;		
	}

}
