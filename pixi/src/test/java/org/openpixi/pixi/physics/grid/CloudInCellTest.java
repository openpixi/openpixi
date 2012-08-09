package org.openpixi.pixi.physics.grid;

import junit.framework.TestCase;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.SimpleSolver;
import org.openpixi.pixi.physics.force.ConstantForce;

/**
 * Unit test for Solver.
 */
public class CloudInCellTest extends TestCase {

	boolean VERBOSE = false;

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
		for (int i = 0; i < 1000; i++) {
			double x1 = 2 + 6 * Math.random();
			double y1 = 2 + 6 * Math.random();
			double phi = 2 * Math.PI * Math.random();
			double distance = 1 * Math.random();
			double x2 = x1 + distance * Math.cos(phi);
			double y2 = y1 + distance * Math.sin(phi);
			testMove(x1, y1, x2, y2, +1, "random boundary " + i);
		}
	}

	private void testMove(double x1, double y1, double x2, double y2, double charge, String text) {
		Settings stt = GridTestCommon.getCommonSettings();
		stt.setInterpolator(new CloudInCell());
		stt.setGridSolver(new SimpleSolver());

		// Add single particle
		Particle p = new Particle();
		p.setX(x1);
		p.setY(y1);
		p.setVx((x2 - x1) / stt.getTimeStep());
		p.setVy((y2 - y1) / stt.getTimeStep());
		p.setMass(1);
		p.setCharge(charge);
		stt.addParticle(p);

		Simulation s = new Simulation(stt);
		s.prepareAllParticles();

		// The simulation always creates its own copy of particles
		// (in fact the setting class does so)
		// and we would like to obtain the reference to our initial particle p.
		p = s.particles.get(0);

		// Advance particle
		s.particlePush();

		//Remember old values after boundary check
		double sx = p.getPrevX();
		double sy = p.getPrevY();

		// Calculate current
		s.getInterpolation().interpolateToGrid(s.particles, s.grid, s.tstep);

		double jx = GridTestCommon.getJxSum(s.grid);
		double jy = GridTestCommon.getJySum(s.grid);

		if (VERBOSE) System.out.println("Total current " + text + ": jx = " + jx + ", jy = " + jy
				+ " (from " + sx + ", " + sy + " to " + p.getX() + ", " + p.getY() + ")");

		GridTestCommon.checkSignJx(s.grid);
		GridTestCommon.checkSignJy(s.grid);

//		This is what ChargeConservingAreaWeightningTest test for (current during timestep)
//		assertAlmostEquals(text + ", jx", charge * (p.x - sx), jx, ACCURACY_LIMIT);
//		assertAlmostEquals(text + ", jy", charge * (p.y - sy), jy, ACCURACY_LIMIT);
//		This is what is appropriate for CIC: momentary current
		assertAlmostEquals(text + ", jx", charge * p.getVx(), jx, ACCURACY_LIMIT);
		assertAlmostEquals(text + ", jy", charge * p.getVy(), jy, ACCURACY_LIMIT);
	}

	public void testFourBoundtatryMovesForce() {
		// Positive charge
		//bottom up
		int charge = 1;
		testMoveForce(4.8, 4.8, 0.2, 0, -1, 1, charge, "four boundary: x=const");
	}

	public void testRandomMovesForce() {
		for (int i = 0; i < 1000; i++) {
			double x1 = 2 + 6 * Math.random();
			double y1 = 2 + 6 * Math.random();
			double phi = 2 * Math.PI * Math.random();
			double distance = 0.8 * Math.random();
			double vx = distance * Math.cos(phi);
			double vy = distance * Math.sin(phi);
			testMoveForce(x1, y1, vx, vy, 0.5*Math.random(), 0.5*Math.random(), +1, "random boundary " + i);
		}
	}

	private void testMoveForce(double x1, double y1, double vx, double vy, double ex, double bz, double charge, String text) {
		Settings stt = GridTestCommon.getCommonSettings();
		stt.setInterpolator(new CloudInCell());
		stt.setGridSolver(new SimpleSolver());

		// Add single particle
		Particle p = new Particle();
		p.setX(x1);
		p.setY(y1);
		p.setVx(vx);
		p.setVy(vy);
		p.setMass(1);
		p.setCharge(charge);
		stt.addParticle(p);

		ConstantForce force = new ConstantForce();
		force.ex = ex;
		force.bz = bz;
		stt.addForce(force);

		Simulation s = new Simulation(stt);
		s.prepareAllParticles();

		// Advance particle
		s.particlePush();

		// The simulation always creates its own copy of particles
		// (in fact the setting class does so)
		// and we would like to obtain the reference to our initial particle p.
		p = s.particles.get(0);

		//Remember old values after boundary check
		double sx = p.getPrevX();
		double sy = p.getPrevY();

		// Calculate current
		s.getInterpolation().interpolateToGrid(s.particles, s.grid, s.tstep);

		double jx = GridTestCommon.getJxSum(s.grid);
		double jy = GridTestCommon.getJySum(s.grid);

		if (VERBOSE) System.out.println("Total current " + text + ": jx = " + jx + ", jy = " + jy
				+ " (from " + sx + ", " + sy + " to " + p.getX() + ", " + p.getY() + ")");

		GridTestCommon.checkSignJx(s.grid);
		GridTestCommon.checkSignJy(s.grid);

//		This is what ChargeConservingAreaWeightningTest test for (current during timestep)
//		assertAlmostEquals(text + ", jx", charge * (p.x - sx), jx, ACCURACY_LIMIT);
//		assertAlmostEquals(text + ", jy", charge * (p.y - sy), jy, ACCURACY_LIMIT);
//		This is what is appropriate for CIC: momentary current
		assertAlmostEquals(text + ", jx", charge * p.getVx(), jx, ACCURACY_LIMIT);
		assertAlmostEquals(text + ", jy", charge * p.getVy(), jy, ACCURACY_LIMIT);
	}

}
