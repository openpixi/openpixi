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

	void assertAlmostEquals(String text, double x, double y, double limit) {
		if (Math.abs(x - y) / Math.abs(x + y) > limit) {
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

		assertAlmostEquals("x", pcopy.x, p.x, ACCURACY_LIMIT);
		assertAlmostEquals("y", pcopy.y, p.y, ACCURACY_LIMIT);
		assertAlmostEquals("vx", pcopy.vx, p.vx, ACCURACY_LIMIT);
		assertAlmostEquals("vy", pcopy.vy, p.vy, ACCURACY_LIMIT);

		for (int i=0; i<1000; i++)
		{
			solver.prepare(p, f, step);
			solver.complete(p, f, step);
		}

		assertAlmostEquals("x", pcopy.x, p.x, ACCURACY_LIMIT);
		assertAlmostEquals("y", pcopy.y, p.y, ACCURACY_LIMIT);
		assertAlmostEquals("vx", pcopy.vx, p.vx, ACCURACY_LIMIT);
		assertAlmostEquals("vy", pcopy.vy, p.vy, ACCURACY_LIMIT);
	}

	/**
	 * Test if solver solves similar to Euler
	 */
	public void testCompareWithEuler() {
		Particle2D p = new Particle2D();
		ConstantForce f = new ConstantForce();
		double step = 0.00001d;
		Solver solver2 = new Euler();

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
		Particle2D p2 = new Particle2D(p);

		solver.prepare(p, f, step);
		solver2.prepare(p2, f, step);

		for (int i=0; i<100; i++)
		{
			solver.step(p, f, step);
			solver2.step(p2, f, step);
		}

		solver.complete(p, f, step);
		solver2.complete(p2, f, step);

		System.out.println("" + this.getClass().getSimpleName());
		System.out.println("x: " + pcopy.x + " -> " + p2.x + " vs. " + p.x);
		System.out.println("y: " + pcopy.y + " -> " + p2.y + " vs. " + p.y);

		double accuracy = 0.001d;
		assertAlmostEquals("x", p2.x, p.x, accuracy);
		assertAlmostEquals("y", p2.y, p.y, accuracy);
		assertAlmostEquals("vx", p2.vx, p.vx, accuracy);
		assertAlmostEquals("vy", p2.vy, p.vy, accuracy);
	}
}
