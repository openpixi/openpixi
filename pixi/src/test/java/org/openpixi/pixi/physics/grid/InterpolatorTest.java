package org.openpixi.pixi.physics.grid;

import java.util.ArrayList;

import junit.framework.TestCase;
import org.openpixi.pixi.physics.InitialConditions;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Simulation;

public class InterpolatorTest extends TestCase {
	
	double ACCURACY_LIMIT = 1.e-16;
	
	public InterpolatorTest(String testName) {
		super(testName);
	}
	
	public void testChargeInterpolation() {
		
		Simulation s = InitialConditions.initEmptySimulation();
		s.setWidth(100);
		s.setHeight(100);
		s.c = Math.sqrt(s.getWidth() * s.getWidth() + s.getHeight() * s.getHeight())/5;
		s.particles = InitialConditions.createRandomParticles(s.getWidth(), s.getHeight(), s.c, 100, 1);
		
		Grid g = GridFactory.createYeeGrid(s, 10, 10, s.getWidth(), s.getHeight());
		
		// TODO: Test does not work:
//		assertEquals(getTotalParticleCharge(s.particles), getChargedensitySum(g), ACCURACY_LIMIT);
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
