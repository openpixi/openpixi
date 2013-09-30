package org.openpixi.pixi.physics.util;

import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.Particle;

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

	public void compare(List<Particle> expectedParticles, List<Particle> actualParticles) {
		compareParticleLists(expectedParticles, actualParticles, TOLERANCE);
	}

	private void compareParticleLists(
			List<Particle> expectedParticles, List<Particle> actualParticles, double tolerance) {

		if (expectedParticles.size() < actualParticles.size()) {
			fail("There are more actual particles than expected!");
		}
		if (expectedParticles.size() > actualParticles.size()) {
			fail("There are less actual particles than expected!");
		}

		for (Particle p : expectedParticles) {
			if (!findParticle(p, actualParticles, tolerance)) {
				fail("Could not find particle " + p + " in the list of actual particles!");
			}
		}
	}

	private boolean findParticle(
			Particle p, List<Particle> particles, Double tolerance) {
		boolean retval = false;
		for (Particle p2 : particles) {
			if (compareParticles(p, p2, tolerance)) {
				return true;
			}
		}
		return retval;
	}

	/**
	 * Compares just the position.
	 */
	private boolean compareParticles(Particle p1, Particle p2, Double tolerance) {
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
