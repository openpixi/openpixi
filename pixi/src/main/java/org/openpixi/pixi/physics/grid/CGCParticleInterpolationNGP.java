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
public class CGCParticleInterpolationNGP implements  InterpolatorAlgorithm {
	public void interpolateToGrid(IParticle p, Grid g) {
		CGCParticle P = (CGCParticle) p;

		double at = g.getTemporalSpacing();
		int direction = P.direction;

		// Particle positions
		double[] oldPosition = P.pos0;
		double[] newPosition = P.pos1;

		// check if one cell or two cell move
		int[] ngpOld, ngpNew;
		if(g.useUnevenGrid) {
			ngpOld = GridFunctions.nearestGridPoint(oldPosition, g.getLatticeSpacings());
			ngpNew = GridFunctions.nearestGridPoint(newPosition, g.getLatticeSpacings());
		} else {
			ngpOld = GridFunctions.nearestGridPoint(oldPosition, g.getLatticeSpacing());
			ngpNew = GridFunctions.nearestGridPoint(newPosition, g.getLatticeSpacing());
		}

		if(ngpOld[direction] == ngpNew[direction]) {
			// one cell move
		} else {
			// two cell move
			int cellIndexOld = g.getCellIndex(ngpOld);
			int cellIndexNew = g.getCellIndex(ngpNew);

			if(P.vel[direction] > 0) {
				AlgebraElement J = P.Q0.mult(g.getLatticeSpacing(direction) / at);
				g.addJ(cellIndexOld, direction, J);
			} else {
				AlgebraElement J = P.Q1.mult(- g.getLatticeSpacing(direction) / at);
				g.addJ(cellIndexNew, direction, J);
			}
		}
	}

	public void interpolateChargedensity(IParticle p, Grid g) {
		CGCParticle P = (CGCParticle) p;

		// Nearest grid points of the particle
		int[] gridPosOld;
		if(g.useUnevenGrid) {
			gridPosOld = GridFunctions.nearestGridPoint(P.pos0, g.getLatticeSpacings());
		} else {
			gridPosOld = GridFunctions.nearestGridPoint(P.pos0, g.getLatticeSpacing());
		}

		// Cell indices
		int cellIndexOld = g.getCellIndex(gridPosOld);

		g.addRho(cellIndexOld, P.Q0);

	}

	public void interpolateToParticle(IParticle p, Grid g) {
		// Compute parallel transport for the particle.
		CGCParticle P = (CGCParticle) p;
		int direction = P.direction;

		// Particle positions
		double[] oldPosition = P.pos0;
		double[] newPosition = P.pos1;

		// check if one cell or two cell move
		int[] ngpOld, ngpNew;
		if(g.useUnevenGrid) {
			ngpOld = GridFunctions.nearestGridPoint(oldPosition, g.getLatticeSpacings());
			ngpNew = GridFunctions.nearestGridPoint(newPosition, g.getLatticeSpacings());
		} else {
			ngpOld = GridFunctions.nearestGridPoint(oldPosition, g.getLatticeSpacing());
			ngpNew = GridFunctions.nearestGridPoint(newPosition, g.getLatticeSpacing());
		}

		if(ngpOld[direction] == ngpNew[direction]) {
			// one cell move: do nothing!
			//P.U = identity;
		} else {
			// two cell move
			int cellIndexOld = g.getCellIndex(ngpOld);
			int cellIndexNew = g.getCellIndex(ngpNew);

			if(P.vel[direction] > 0) {
				P.U = g.getUnext(cellIndexOld, direction);
			} else {
				P.U = g.getUnext(cellIndexNew, direction).adj();
			}
			P.updateCharge = true;
		}
	}
}
