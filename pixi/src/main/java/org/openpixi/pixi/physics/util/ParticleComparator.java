package org.openpixi.pixi.physics.util;

import org.openpixi.pixi.physics.particles.IParticle;

import java.util.List;

/**
 * Compares the results of two simulations. In case of failure throws
 * ComparisonFailedException.
 */
public class ParticleComparator {

	private static final Double TOLERANCE = 1e-10;
	private static final int NO_STEP_TRACKING = -1;
	private int stepNo = NO_STEP_TRACKING;

	public ParticleComparator() {
	}

	/**
	 * In case of failure outputs also the step number in which the failure
	 * occurred.
	 */
	public ParticleComparator(int stepNo) {
		this.stepNo = stepNo;
	}

	public void compare(List<IParticle> expectedParticles, List<IParticle> actualParticles) {
		compareParticleLists(expectedParticles, actualParticles, TOLERANCE);
	}

	private void compareParticleLists(
			List<IParticle> expectedParticles, List<IParticle> actualParticles, double tolerance) {

		if (expectedParticles.size() < actualParticles.size()) {
			fail("There are more actual particles than expected!");
		}
		if (expectedParticles.size() > actualParticles.size()) {
			fail("There are less actual particles than expected!");
		}

		for (IParticle p : expectedParticles) {
			if (!findParticle(p, actualParticles, tolerance)) {
				fail("Could not find particle " + p + " in the list of actual particles!");
			}
		}
	}

	private boolean findParticle(
			IParticle p, List<IParticle> particles, Double tolerance) {
		boolean retval = false;
		for (IParticle p2 : particles) {
			if (compareParticles(p, p2, tolerance)) {
				return true;
			}
		}
		return retval;
	}

	/**
	 * Compares just the position.
	 */
	private boolean compareParticles(IParticle p1, IParticle p2, Double tolerance) {
		if (Math.abs(p1.getX() - p2.getX()) > tolerance) {
			return false;
		}
		if (Math.abs(p1.getY() - p2.getY()) > tolerance) {
			return false;
		}
		return true;
	}

	private void fail(String msg) {
		StringBuilder finalMsg = new StringBuilder(msg);
		if (stepNo != NO_STEP_TRACKING) {
			finalMsg.append(" STEP NUMBER: " + stepNo);
		}
		finalMsg.append(", COMPARISON FAILED !!! ");
		throw new ComparisonFailedException(finalMsg.toString());
	}
}
