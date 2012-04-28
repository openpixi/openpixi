package org.openpixi.pixi.physics.grid;

import junit.framework.TestCase;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.ParticleMover;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.force.*;
import org.openpixi.pixi.physics.solver.*;

/**
 * Unit test for Solver.
 */
public class CloudInCellTest extends TestCase {

	//double ACCURACY_LIMIT = 1.e-16;
	double ACCURACY_LIMIT = 1.e-14;

	void assertAlmostEquals(String text, double x, double y, double limit) {
		if ((Math.abs(x - y) / Math.abs(x + y) > limit)
				|| (Double.isNaN(x) != Double.isNaN(y))
				|| (Double.isInfinite(x) != Double.isInfinite(y))) {
			assertTrue(text + " expected:<" + x + "> but was:<" + y + ">", false);
		}
	}

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public CloudInCellTest(String testName) {
		super(testName);
	}

	public void testFourBoundaryMoves() {
		// Positive charge
		for (int charge = -1; charge <=1; charge++){
		//bottom up
		testMove(4.8, 4.8, 4.8, 5.2, charge, "four boundary: x=const");
		testMove(5.3, 5.2, 5.3, 4.8, charge, "four boundary: x=const");
		//left to right
		testMove(4.8, 5.3, 5.2, 5.3, charge, "four boundary: left - right y=const");
		testMove(4.8, 4.8, 5.2, 4.8, charge, "four boundary: left - right y=const");
		//from top left to down right
		testMove(4.9, 5.4, 5.4, 4.6, charge, "four boundary: top left - down right ");
		//from down left to top right
		testMove(4.7, 4.6, 5.4, 5.3, charge, "four boundary: down left - top right ");
		
		//special moves
		testMove(4.6, 4.5, 5.4, 4.5, charge, "four boundary: middle of cell");
		testMove(4.6, 5.0, 5.4, 5.0, charge, "four boundary: on teh edge" );
		}
	}

	public void testSevenBoundaryMoves() {
		testMove(3.3, 5.2, 3.6, 5.4, +1, "seven boundary, move right");
		testMove(5.7, 5.2, 5.3, 5.4, +1, "seven boundary, move left");
		testMove(5.3, 5.2, 5.2, 5.8, +1, "seven boundary, move up");
		testMove(5.7, 7.6, 5.6, 7.4, +1, "seven boundary, move down");
	}

	public void testTenBoundaryMoves() {
		testMove(5.7, 5.8, 5.3, 5.2, +1, "ten boundary");
	}

	public void testRandomMoves() {
		for (int i = 0; i < 100000; i++) {
			double x1 = 2 + 6 * Math.random();
			double y1 = 2 + 6 * Math.random();
			double phi = 2 * Math.PI * Math.random();
			double distance = 1 * Math.random();
			double x2 = x1 + distance * Math.cos(phi);
			double y2 = y1 + distance * Math.sin(phi);
			testMove(x1, y1, x2, y2, +1, "random boundary " + i);
		}
	}
	
	public void testFourBoundtatryMovesForce() {
		// Positive charge
		//bottom up
		int charge = 1;
		testMoveForce(4.8, 4.8, 0.2, 0, charge, "four boundary: x=const");
	}

	
	private void testMoveForce(double x1, double y1, double vx, double vy, double charge, String text) {
		Simulation s = new Simulation(10, 10, 0, 1);
		s.psolver = new Euler();

		// Add single particle
		Particle2D p = new Particle2D();
		p.x = x1;
		p.y = y1;
		p.vx = vx;
		p.vy = vy;
		p.mass = 1;
		p.charge = charge;
		s.particles.add(p);
		
		ConstantForce force = new ConstantForce();
		force.ex= 0.1;
		s.f.add(force);
		
		ParticleMover.prepareAllParticles(s);
		
		// Use Yeegrid
		s.setSize(10, 10);
		SimpleGrid grid = new SimpleGrid(s); // 10x10 grid

		// Remember old values
		double sx = p.x;
		double sy = p.y;

		// Advance particle

		ParticleMover.particlePush(s);

		// Calculate current
		grid.interp.interpolateToGrid(s.particles);

		double jx = getSum(grid.jx);
		double jy = getSum(grid.jy);
		
		System.out.println("Total current " + text + ": jx = " + jx + ", jy = " + jy
				+ " (from " + sx + ", " + sy + " to " + p.x + ", " + p.y + ")");

//		assertAlmostEquals(text + ", jx", charge * (p.x - sx), jx, ACCURACY_LIMIT);
//		assertAlmostEquals(text + ", jy", charge * (p.y - sy), jy, ACCURACY_LIMIT);
		assertAlmostEquals(text + ", jx", charge * p.vx, jx, ACCURACY_LIMIT);
		assertAlmostEquals(text + ", jy", charge * p.vy, jy, ACCURACY_LIMIT);
	}
	
	private void testMove(double x1, double y1, double x2, double y2, double charge, String text) {
		Simulation s = new Simulation(10, 10, 0, 1);

		// Add single particle
		Particle2D p = new Particle2D();
		p.x = x1;
		p.y = y1;
		p.vx = (x2 - x1) / s.tstep;
		p.vy = (y2 - y1) / s.tstep;
		p.mass = 1;
		p.charge = charge;
		s.particles.add(p);

		// Use Yeegrid
		s.setSize(10, 10);
		SimpleGrid grid = new SimpleGrid(s); // 10x10 grid

		// Remember old values
		s.particles.get(0).pd.x = s.particles.get(0).x;
		s.particles.get(0).pd.y = s.particles.get(0).y;

		// Advance particle
		ParticleMover.prepareAllParticles(s);
		ParticleMover.particlePush(s);

		// Calculate current
		grid.interp.interpolateToGrid(s.particles);

		double jx = getSum(grid.jx);
		double jy = getSum(grid.jy);

		System.out.println("Total current " + text + ": jx = " + jx + ", jy = " + jy
				+ " (from " + x1 + ", " + y1 + " to " + x2 + ", " + y2 + ")");

		assertAlmostEquals(text + ", jx", charge * (x2 - x1), jx, ACCURACY_LIMIT);
		assertAlmostEquals(text + ", jy", charge * (y2 - y1), jy, ACCURACY_LIMIT);
	}

	private double getSum(double[][] field) {
		double sum = 0;
		for (double[] row : field) {
			for (double value : row) {
				sum += value;
			}
		}
		return sum;
	}

}
