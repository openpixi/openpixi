package org.openpixi.pixi.physics.solver.relativistic;

import org.openpixi.pixi.physics.ConstantsSI;
import org.openpixi.pixi.physics.solver.SolverTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for LeapFrog.
 */
public class LeapFrogRelativisticTest extends SolverTest {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public LeapFrogRelativisticTest(String testName) {
		super(testName);

		solver = new LeapFrogRelativistic(ConstantsSI.c);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(LeapFrogRelativisticTest.class);
	}
}
