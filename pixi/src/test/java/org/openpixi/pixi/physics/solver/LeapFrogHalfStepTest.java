package org.openpixi.pixi.physics.solver;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for LeapFrogDamped.
 */
public class LeapFrogHalfStepTest extends SolverTest {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public LeapFrogHalfStepTest(String testName) {
		super(testName);

		solver = new LeapFrogDamped();
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(LeapFrogHalfStepTest.class);
	}
}
