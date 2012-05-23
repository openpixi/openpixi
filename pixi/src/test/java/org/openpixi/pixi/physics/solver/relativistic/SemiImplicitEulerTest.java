package org.openpixi.pixi.physics.solver.relativistic;

import org.openpixi.pixi.physics.ConstantsSI;
import org.openpixi.pixi.physics.solver.SolverTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test BorisDamped.
 */
public class SemiImplicitEulerTest extends SolverTest {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public SemiImplicitEulerTest(String testName) {
		super(testName);

		solver = new SemiImplicitEulerRelativistic(ConstantsSI.c);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(SemiImplicitEulerTest.class);
	}
}
