package org.openpixi.pixi.physics.grid;

import junit.framework.TestCase;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.YeeSolver;

import java.util.ArrayList;

public class InterpolatorTest extends TestCase {
	
	double ACCURACY_LIMIT = 1.e-16;
	
	public InterpolatorTest(String testName) {
		super(testName);
	}
	
	public void testChargeInterpolation() {

		Settings stt = new Settings();
		stt.setSimulationWidth(100);
		stt.setSimulationHeight(100);
		stt.setSpeedOfLight(Math.sqrt(stt.getSimulationWidth() * stt.getSimulationWidth() +
				stt.getSimulationHeight() * stt.getSimulationHeight())/5);

		stt.setNumOfParticles(100);
		stt.setParticleRadius(1);
		stt.setParticleMaxSpeed(stt.getSpeedOfLight());

		stt.setGridCellsX(10);
		stt.setGridCellsY(10);
		stt.setGridSolver(new YeeSolver());
		stt.setInterpolator(new ChargeConservingAreaWeighting());

		Simulation s = new Simulation(stt);

		// TODO: Test does not work:
		//assertEquals(getTotalParticleCharge(s.particles), getChargedensitySum(s.grid), ACCURACY_LIMIT);
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
