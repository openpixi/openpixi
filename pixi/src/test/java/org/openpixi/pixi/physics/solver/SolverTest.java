package org.openpixi.pixi.physics.solver;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openpixi.pixi.physics.Particle2D;
import org.openpixi.pixi.physics.force.ConstantForce;

/**
 * Unit test for Solver.
 */
public class SolverTest extends TestCase {

	Solver solver;

	//double ACCURACY_LIMIT = 1.e-16;
	double ACCURACY_LIMIT = 1.e-15;

	void assertAlmostEquals(String text, double x, double y) {
		if (Math.abs(x - y) / Math.abs(x + y) > ACCURACY_LIMIT) {
			assertTrue(text + " expected:<" + x + "> but was:<" + y + ">", false);
		}
	}

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public SolverTest(String testName) {
		super(testName);

		solver = new Euler();
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(SolverTest.class);
	}

	/**
	 * Test if prepare and complete return to the same initial conditions
	 */
	public void testPrepareComplete() {
		Particle2D p = new Particle2D();
		ConstantForce f = new ConstantForce();
		double step = 1.0;

		f.ex = 1.234;
		f.ey = 3.456;
		f.bz = 7.890;
		f.gx = 2.468;
		f.gy = 3.579;
		f.drag = 5.432;

		p.x = 23.456;
		p.y = 35.689;
		p.radius = 13.579;
		p.vx = 12.345;
		p.vy = 76.543;
		p.mass = 7.654;
		p.charge = 5.432;

		Particle2D pcopy = new Particle2D(p);

		solver.prepare(p, f, step);
		solver.complete(p, f, step);

		assertAlmostEquals("x", pcopy.x, p.x);
		assertAlmostEquals("y", pcopy.y, p.y);
		assertAlmostEquals("vx", pcopy.vx, p.vx);
		assertAlmostEquals("vy", pcopy.vy, p.vy);

		for (int i=0; i<1000; i++)
		{
			solver.prepare(p, f, step);
			solver.complete(p, f, step);
		}

		assertAlmostEquals("x", pcopy.x, p.x);
		assertAlmostEquals("y", pcopy.y, p.y);
		assertAlmostEquals("vx", pcopy.vx, p.vx);
		assertAlmostEquals("vy", pcopy.vy, p.vy);
	}
}
