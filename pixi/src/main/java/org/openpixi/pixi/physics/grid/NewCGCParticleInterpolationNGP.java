package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.particles.CGCParticle;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.physics.util.GridFunctions;

/**
 * This interpolation algorithm is used for CGC simulations in the lab frame. The particles in this type of simulation
 * merely act as 'static' sources for the current. It is assumed that this kind of particle moves along a grid axis such
 * that there is no ambiguity in defining parallel transport for the color charges of the particles.
 */
public class NewCGCParticleInterpolationNGP implements  InterpolatorAlgorithm {
	public void interpolateToGrid(IParticle p, Grid g) {
		CGCParticle P = (CGCParticle) p;
		GroupElement identity = g.getElementFactory().groupIdentity();

		double at = g.getTemporalSpacing();
		double as = g.getLatticeSpacing();
		int direction = P.direction;

		// Particle positions
		double[] oldPosition = P.pos0;
		double[] newPosition = P.pos1;

		// check if one cell or two cell move
		int[] ngpOld = GridFunctions.nearestGridPoint(oldPosition, as);
		int[] ngpNew = GridFunctions.nearestGridPoint(newPosition, as);

		if(ngpOld[direction] == ngpNew[direction]) {
			// same point move
		} else {
			// new point move
			int cellIndexOld = g.getCellIndex(ngpOld);
			int cellIndexNew = g.getCellIndex(ngpNew);
			//AlgebraElement Q = P.Q0.add(P.Q1).mult(0.5);
			//Q.actAssign(P.U);

			if(P.vel[direction] > 0) {
				AlgebraElement J = P.Q0.mult(as / at);
				g.addJ(cellIndexOld, direction, J);
			} else {
				AlgebraElement J = P.Q1.mult(- as / at);
				g.addJ(cellIndexNew, direction, J);
			}
		}
	}

	public void interpolateChargedensity(IParticle p, Grid g) {
		CGCParticle P = (CGCParticle) p;

		double as = g.getLatticeSpacing();
		int direction = P.direction;

		// Nearest grid point of the particle
		int[] gridPosOld = GridFunctions.nearestGridPoint(P.pos0, as);

		// Cell indices
		int cellIndexOld = g.getCellIndex(gridPosOld);

		g.addRho(cellIndexOld, P.Q0);

	}

	public void interpolateToParticle(IParticle p, Grid g) {
		// Compute parallel transport for the particle.
		CGCParticle P = (CGCParticle) p;
		GroupElement identity = g.getElementFactory().groupIdentity();

		double at = g.getTemporalSpacing();
		double as = g.getLatticeSpacing();
		int direction = P.direction;

		// Particle positions
		double[] oldPosition = P.pos0;
		double[] newPosition = P.pos1;

		// check if one cell or two cell move
		int[] ngpOld = GridFunctions.nearestGridPoint(oldPosition, as);
		int[] ngpNew = GridFunctions.nearestGridPoint(newPosition, as);

		if(ngpOld[direction] == ngpNew[direction]) {
			// same point move
			 P.U = identity;
		} else {
			// new point move
			int cellIndexOld = g.getCellIndex(ngpOld);
			int cellIndexNew = g.getCellIndex(ngpNew);

			if(P.vel[direction] > 0) {
				P.U = g.getUnext(cellIndexOld, direction);
			} else {
				P.U = g.getUnext(cellIndexNew, direction).adj();
			}
		}
	}
}
