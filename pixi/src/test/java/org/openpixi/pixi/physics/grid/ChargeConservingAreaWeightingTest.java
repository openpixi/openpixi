package org.openpixi.pixi.physics.grid;

import junit.framework.TestCase;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.YeeSolver;
import org.openpixi.pixi.physics.force.ConstantForce;

/**
 * Unit test for Solver.
 */
public class ChargeConservingAreaWeightingTest extends TestCase {

	boolean VERBOSE = false;

//	double ACCURACY_LIMIT = 1.e-15;
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
	public ChargeConservingAreaWeightingTest(String testName) {
		super(testName);
	}

	public void testFourBountdaryMoves() {
		// Positive charge
		for (int charge = 1; charge <=1; charge++){
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
		testMove(4.6, 5.0, 5.4, 5.0, charge, "four boundary: on the edge" );
		}
	}

	public void testSevenBoundaryMoves() {
		testMove(3.3, 5.2, 3.6, 5.4, +1, "seven boundary, move right");
		testMove(5.7, 5.2, 5.3, 5.4, +1, "seven boundary, move left");
		testMove(5.3, 5.2, 5.2, 5.8, +1, "seven boundary, move up");
		testMove(5.7, 7.6, 5.6, 7.4, +1, "seven boundary, move down");
		testMove(5.7, 7.6, 6.6, 7.6, +1, "seven boundary, y=const");
		testMove(5.7, 6.7, 5.7, 7.6, +1, "seven boundary, x=const");
		testMove(5.0, 5.0, 5.6, 5.6, +1, "seven boundary, diagonal");
	}

	public void testTenBoundaryMoves() {
		//from middle top right to top right right subcell
		testMove(5.4, 5.1, 6.1, 5.7, +1, "ten boundary: top right right");
		//from middle top right to top right subcell (stays in the same cell)
		testMove(5.4, 5.1, 5.9, 5.8, +1, "ten boundary: top right");
		//from middle top right to top right left subcell
		testMove(5.4, 5.1, 5.8, 6.1, +1, "ten boundary: top right left");
		//from middle bottom left to top right subcell
		testMove(4.9, 4.6, 5.9, 5.8, +1, "ten boundary: top right left");

		//special diagonal move
		testMove(5.0, 5.0, 5.7, 5.7, +1, "ten boundary: diagonal");

		testMove(4.5, 5.0, 4.5, 6.1, +1, "ten boundary: on the edge");

		testMove(2.515455551020562, 4.096749046269608, 2.4949265893858295, 4.9716269624132305, +1, "special");

	}
//	Initializes one particle and moves it in a random direction
//	This test may fail with a "wrong sign" assertion if "distance" is chosen gerater than
//	either cell width or cell height.
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

	public void testRandomMoves2() {
		for (int i = 0; i < 1000; i++) {
			double x1 = 2 + 6 * Math.random();
			double y1 = 2 + 6 * Math.random();
			double x2 = x1 + 1 * Math.random();
			double y2 = y1 + 1 * Math.random();
			testMove(x1, y1, x2, y2, +1, "random boundary " + i);
		}
	}


	public void testSpecific() {
		testMove(3.6374390759171615, 6.441039158290589, 3.1049714386963867, 6.686037692843083, 1, "s");
		testMove(7.762230349789808, 2.4447032134825077, 6.940823082228207, 2.800297294538329, 1, "s2");
//		testMove(-cellWidth/2 -0.4957224966229246 , -0.3950285613036133, 0.18176018946600775
	}

	public void testPeriodicBoundary() {
		int charge = 1;
		testMoveForce(0.3, 5.2, -2, 0.1, 1, 2, charge, "org/openpixi/pixi/distributed/movement/boundary");
		testMove(9.8, 5.2, 10.3, 5.2, charge, "org/openpixi/pixi/distributed/movement/boundary");
		testMove(5.2, 9.3, 5.2, 10.6, charge, "org/openpixi/pixi/distributed/movement/boundary");
	}

	private void testMove(double x1, double y1, double x2, double y2, double charge, String text) {
		Settings stt = GridTestCommon.getCommonSettings();
		stt.setInterpolator(new ChargeConservingAreaWeighting());
		stt.setGridSolver(new YeeSolver());

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

		assertAlmostEquals(text + ", jx", charge * (p.getX() - sx) / s.tstep, jx, ACCURACY_LIMIT);
		assertAlmostEquals(text + ", jy", charge * (p.getY() - sy) / s.tstep, jy, ACCURACY_LIMIT);
	}

	public void testFourBoundaryMovesForce() {
		// Positive charge
		//bottom up
		int charge = 1;
		testMoveForce(4, 4, 0.09, 0.01, -0.1, 0.1, charge, "four boundary: x=const");
	}

//	Initializes one particle and moves it in a random direction
//	This test may fail with a "wrong sign" assertion if "distance" is chosen gerater than
//	either cell width or cell height. This also happens if the forces are too strong.
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

	/**
	 * Executes a move with applied constant forces
	 * @param x1 start x-position
	 * @param y1 start y-position
	 * @param vx start x-velocity
	 * @param vy start y-velocity
	 * @param ex E-field in x-direction
	 * @param bz B-field in z direction
	 * @param charge particle charge
	 * @param text
	 */
	private void testMoveForce(double x1, double y1, double vx, double vy, double ex, double bz, double charge, String text) {
		Settings stt = GridTestCommon.getCommonSettings();
		stt.setInterpolator(new ChargeConservingAreaWeighting());
		stt.setGridSolver(new YeeSolver());

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

		assertAlmostEquals(text + ", jx", charge * (p.getX() - sx) / s.tstep, jx, ACCURACY_LIMIT);
		assertAlmostEquals(text + ", jy", charge * (p.getY() - sy) / s.tstep, jy, ACCURACY_LIMIT);
	}

}
