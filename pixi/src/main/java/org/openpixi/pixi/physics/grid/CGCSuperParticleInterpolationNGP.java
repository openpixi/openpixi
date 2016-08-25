package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.particles.CGCSuperParticle;
import org.openpixi.pixi.physics.particles.IParticle;

/**
 * This interpolation algorithm is used for the optimized version of CGC simulations in the lab frame. The particles
 * in this type of simulation merely act as 'static' sources for the current. It is assumed that this kind of particle
 * moves along a grid axis such that there is no ambiguity in defining parallel transport for the color charges of the
 * particles. The super particle classes encapsulate larger collections of particles whose relative positions are fixed
 * during the simulation and whose charges are updated at the same time when they cross into other cells.
 */
public class CGCSuperParticleInterpolationNGP implements InterpolatorAlgorithm {

	private boolean useOffset = false;

	public void interpolateToGrid(IParticle p, Grid g) {
		double at = g.getTemporalSpacing();
		double as = g.getLatticeSpacing(0);
		int totalNumberOfCells = g.getTotalNumberOfCells();

		CGCSuperParticle P = (CGCSuperParticle) p;
		if (P.needsUpdate(g.getSimulationSteps())) {
			int indexOffset = P.getCurrentOffset(g.getSimulationSteps());

	        /*
	         * Set limits for iterating over particle charges inside the super particle:
	         * As a super particle moves closer to the boundary of the simulation box, some of the particle charges
	         * associated with that super particle may already lie outside the grid. Setting the start and end of the
	         * for loop makes sure that the interpolation routines do not create ArrayIndexOutOfBounds exceptions.
	         *
	         * Furthermore we can introduce an offset depending on the sub lattice shift of the super particle. This
	         * shifts the order in which particle charges are interpolated on the grid. As a result different threads
	         * do not write to the same cell at the same time, which should improve multithreading performance.
	         */
			int imin = (indexOffset < 0) ? -indexOffset : 0;
			int imax = Math.max(Math.min(indexOffset + P.numberOfParticles, totalNumberOfCells) - indexOffset, 0);
			int ireg = imax - imin;
			int offset = 0;
			if (useOffset) {
				offset = ireg * P.subLatticeShift / P.particlePerCell;
			}
			if (P.orientation > 0) {
				for (int i = 0; i < ireg; i++) {
					int j = (i + offset) % ireg + imin;
					int index = indexOffset + j;
					AlgebraElement J = P.Q[j].mult(as / at);
					g.addJ(index, 0, J); // Optimizations only work for x-direction!
					GroupElement U = g.getUnext(index, 0);
					P.Q[j].actAssign(U.adj());
				}
			} else {
				for (int i = 0; i < ireg; i++) {
					int j = (i + offset) % ireg + imin;
					int index = indexOffset + j;
					GroupElement U = g.getUnext(index, 0);
					P.Q[j].actAssign(U);
					AlgebraElement J = P.Q[j].mult(-as / at);
					g.addJ(index, 0, J); // Optimizations only work for x-direction!
				}
			}
		}
	}

	public void interpolateChargedensity(IParticle p, Grid g) {
		CGCSuperParticle P = (CGCSuperParticle) p;
		int indexOffset = P.getCurrentNGPOffset(g.getSimulationSteps());
		int totalNumberOfCells = g.getTotalNumberOfCells();

		// Set the limits of the for loop. See comment in interpolateToGrid() for explanation.
		int imin = (indexOffset < 0) ? -indexOffset : 0;
		int imax = Math.max(Math.min(indexOffset + P.numberOfParticles, totalNumberOfCells) - indexOffset, 0);
		int ireg = imax - imin;
		int offset = 0;
		if (useOffset) {
			offset = ireg * P.subLatticeShift / P.particlePerCell;
		}
		for (int i = 0; i < ireg; i++) {
			int j = (i + offset) % ireg + imin;
			int index = indexOffset + j;
			g.addRho(index, P.Q[j]);
		}
	}

	public void interpolateToParticle(IParticle p, Grid g) {
	    /*
		Usually this method would tell the particles what gauge links are currently acting on them. In the case of
		the optimized super particle classes, parallel transport is taken care of by interpolateToGrid().
		 */
	}
}
