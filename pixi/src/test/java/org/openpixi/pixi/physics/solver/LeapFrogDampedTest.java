package org.openpixi.pixi.physics.solver;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for LeapFrogDamped.
 */
public class LeapFrogDampedTest extends SolverTest {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public LeapFrogDampedTest(String testName) {
		super(testName);

		solver = new LeapFrogDamped();
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(LeapFrogDampedTest.class);
	}
}
