package org.openpixi.pixi.physics.grid;

import junit.framework.TestCase;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.grid.CloudInCell;
import org.openpixi.pixi.physics.particles.ParticleFull;
import org.openpixi.pixi.physics.particles.Particle;

import java.util.Random;
import java.util.ArrayList;

public class InterpolatorTest extends TestCase {
	
	double ACCURACY_LIMIT = 1.e-13;
	Random random = new Random();
	
	public InterpolatorTest(String testName) {
		super(testName);
	}
	
	/**Tests the interpolateChargeDensity() method of InterpolatorAlgorithm
	 * classes. Except of the interpolator class itself it also relies on the
	 * proper functioning of grid accessors as well as on the Cell class.
	 */
	public void testChargeInterpolation() {

		Settings stt = new Settings();
		stt.setSimulationWidth(100);
		stt.setSimulationHeight(100);
		stt.setGridCellsX(100);
		stt.setGridCellsY(100);

		Grid grid = new Grid(stt);
		
		//We iterate over all the grid cells manually to avoid dependence of this
		//test on the cellIterator implementation. But using the grid method
		//grid.resetCharge() should also work!
		for (int x = -grid.EXTRA_CELLS_BEFORE_GRID; x < 
				(grid.getNumCellsX()+grid.EXTRA_CELLS_AFTER_GRID); x++) {
			for (int y = -grid.EXTRA_CELLS_BEFORE_GRID; y < 
					(grid.getNumCellsY()+grid.EXTRA_CELLS_AFTER_GRID); y++) {
				grid.getCell(x, y).resetCharge();
			}
		}

		
		ArrayList<Particle> particles = new ArrayList<Particle>(); 
		
		for (int i=0; i < 100; i++) {
			Particle p = new ParticleFull();
			p.setX(random.nextDouble()*stt.getSimulationWidth());
			p.setY(random.nextDouble()*stt.getSimulationHeight());
			//Assign random integer charge in the range (-10,10)
			//Accuracy decreases with non integer charge, more particles
			//as well as more cells.
			p.setCharge((random.nextInt(20)-10));
			particles.add(p);
		}
		
		InterpolatorAlgorithm interpolation = new CloudInCell();
		
		for (Particle p : particles) {
			interpolation.interpolateChargedensity(p, grid);
		}

		assertEquals(getTotalParticleCharge(particles), getChargedensitySum(grid), ACCURACY_LIMIT);
	}
	
	public static double getChargedensitySum(Grid grid) {
		double sum = 0;
		//NOTE: If periodic boundaries are chosen the sum has to go over the "real" cells.
		//In the hardwall boundary case the extra cells have to be included.
		//This behavior is ok and consistent.
		for (int x = 0; x < grid.getNumCellsX(); x++) {
			for (int y = 0; y < grid.getNumCellsY(); y++) {
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
