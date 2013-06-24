package org.openpixi.pixi.physics.solver;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openpixi.pixi.physics.force.ConstantForce;
import org.openpixi.pixi.physics.particles.Particle;
import org.openpixi.pixi.physics.particles.ParticleFull;

/**
 * Unit test for Solver.
 */
public class SolverTest extends TestCase {

	boolean VERBOSE = false;

	protected Solver solver;

	//double ACCURACY_LIMIT = 1.e-16;
	double ACCURACY_LIMIT = 1.e-13;

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
		Particle p = new ParticleFull();
		ConstantForce f = new ConstantForce();
		double step = 1.0;

		f.ex = 1.234;
		f.ey = 3.456;
		f.bz = 7.890;
		f.gx = 2.468;
		f.gy = 3.579;
		f.drag = 5.432;

		p.setX(23.456);
		p.setY(35.689);
		p.setRadius(13.579);
		p.setVx(12.345);
		p.setVy(76.543);
		p.setMass(7.654);
		p.setCharge(5.432);

		Particle pcopy = p.copy();

		solver.prepare(p, f, step);
		solver.complete(p, f, step);

		assertAlmostEquals("x", pcopy.getX(), p.getX(), ACCURACY_LIMIT);
		assertAlmostEquals("y", pcopy.getY(), p.getY(), ACCURACY_LIMIT);
		assertAlmostEquals("vx", pcopy.getVx(), p.getVx(), ACCURACY_LIMIT);
		assertAlmostEquals("vy", pcopy.getVy(), p.getVy(), ACCURACY_LIMIT);

		for (int i=0; i<1000; i++)
		{
			solver.prepare(p, f, step);
			solver.complete(p, f, step);
		}

		assertAlmostEquals("x", pcopy.getX(), p.getX(), ACCURACY_LIMIT);
		assertAlmostEquals("y", pcopy.getY(), p.getY(), ACCURACY_LIMIT);
		assertAlmostEquals("vx", pcopy.getVx(), p.getVx(), ACCURACY_LIMIT);
		assertAlmostEquals("vy", pcopy.getVy(), p.getVy(), ACCURACY_LIMIT);
	}

	/**
	 * Test if solver solves similar to Euler
	 */
	public void testCompareWithEuler() {
		Particle p = new ParticleFull();
		ConstantForce f = new ConstantForce();
		double step = 0.00001d;
		Solver solver2 = new Euler();

		f.ex = 1.234;
		f.ey = 3.456;
		f.bz = 7.890;
		f.gx = 2.468;
		f.gy = 3.579;
		f.drag = 5.432;

		p.setX(23.456);
		p.setY(35.689);
		p.setRadius(13.579);
		p.setVx(12.345);
		p.setVy(76.543);
		p.setMass(7.654);
		p.setCharge(5.432);

		Particle pcopy = p.copy();
		Particle p2 = p.copy();

		solver.prepare(p, f, step);
		solver2.prepare(p2, f, step);

		for (int i=0; i<100; i++)
		{
			solver.step(p, f, step);
			solver2.step(p2, f, step);
		}

		solver.complete(p, f, step);
		solver2.complete(p2, f, step);

		if (VERBOSE) {
			System.out.println("" + this.getClass().getSimpleName());
			System.out.println("x: " + pcopy.getX() + " -> " + p2.getX() + " vs. " + p.getX());
			System.out.println("y: " + pcopy.getY() + " -> " + p2.getY() + " vs. " + p.getY());
		}

		double accuracy = 0.001d;
		assertAlmostEquals("x", p2.getX(), p.getX(), accuracy);
		assertAlmostEquals("y", p2.getY(), p.getY(), accuracy);
		assertAlmostEquals("vx", p2.getVx(), p.getVx(), accuracy);
		assertAlmostEquals("vy", p2.getVy(), p.getVy(), accuracy);
	}
}
