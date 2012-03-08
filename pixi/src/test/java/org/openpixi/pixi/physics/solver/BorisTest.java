package org.openpixi.pixi.physics.solver;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for Boris.
 */
public class BorisTest extends SolverTest {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public BorisTest(String testName) {
		super(testName);

		solver = new Boris();
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(BorisTest.class);
	}
}
