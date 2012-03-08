package org.openpixi.pixi.physics.solver;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for LeapFrog.
 */
public class LeapFrogTest extends SolverTest {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public LeapFrogTest(String testName) {
		super(testName);

		solver = new LeapFrog();
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(LeapFrogTest.class);
	}
}
