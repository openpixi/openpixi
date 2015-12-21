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
public class CGCParticleInterpolationCIC implements  InterpolatorAlgorithm {
	public void interpolateToGrid(IParticle p, Grid g) {
		CGCParticle P = (CGCParticle) p;

		double as = g.getLatticeSpacing();
		double dt = g.getTemporalSpacing();
		double a = as / dt;
		int direction = P.direction;
		GroupElement identity = g.getElementFactory().groupIdentity();
		AlgebraElement Q = P.Q0.add(P.Q1).mult(0.5);

		// "Floored" grid points of the particle
		int[] gridPosOld = GridFunctions.flooredGridPoint(P.pos0, as);
		int[] gridPosNew = GridFunctions.flooredGridPoint(P.pos1, as);

		// Cell indices
		int cellIndexOld = g.getCellIndex(gridPosOld);
		int cellIndexNew = g.getCellIndex(gridPosNew);

		// Longitudinal indices
		int longitudinalIndexOld = (int) (P.pos0[direction] / as);
		int longitudinalIndexNew = (int) (P.pos1[direction] / as);
		int[] ngpOld = GridFunctions.nearestGridPoint(P.pos0, as);
		int[] ngpNew = GridFunctions.nearestGridPoint(P.pos1, as);


		if(longitudinalIndexOld == longitudinalIndexNew) {
			// One-cell move
			oneCellMove(P, g, longitudinalIndexOld, cellIndexOld);
		} else {
			if(longitudinalIndexOld < longitudinalIndexNew) {
				// Two-cell move to the right
				CGCParticle first = (CGCParticle) P.copy();
				CGCParticle second = (CGCParticle) P.copy();
				first.pos1[direction] = longitudinalIndexNew*as;
				second.pos0[direction] = longitudinalIndexNew*as;
				second.Q0.set(P.Q1.copy());

				//oneCellMove(first, g, longitudinalIndexOld, cellIndexOld);
				//oneCellMove(second, g, longitudinalIndexNew, cellIndexNew);

				// Parallel transport to the left cell.
				double d0 = longitudinalIndexNew - (P.pos0[direction] / as);
				GroupElement U0 = g.getUnext(cellIndexOld, direction);
				// Parallel transport to the right cell.
				double d1 = (P.pos1[direction] / as) - longitudinalIndexNew;
				GroupElement U1 = identity;

				// Induced currents
				AlgebraElement Jold = P.Q0.act(U0).mult(a * d0); // current at old position
				AlgebraElement Jnew = P.Q1.act(U1).mult(a * d1); // current at new position
				g.addJ(cellIndexOld, direction, Jold);
				g.addJ(cellIndexNew, direction, Jnew);

			} else {
				// Two-cell move to the left
				CGCParticle first = (CGCParticle) P.copy();
				CGCParticle second = (CGCParticle) P.copy();
				first.pos1[direction] = longitudinalIndexOld*as;
				second.pos0[direction] = longitudinalIndexOld*as;
				first.Q1.set(P.Q0.copy());

				//oneCellMove(first, g, longitudinalIndexOld, cellIndexOld);
				//oneCellMove(second, g, longitudinalIndexNew, cellIndexNew);

				// Parallel transport to the right cell.
				double d0 = (P.pos0[direction] / as) - longitudinalIndexOld;
				GroupElement U0 = identity;
				// Parallel transport to the left cell.
				double d1 = longitudinalIndexOld - (P.pos1[direction] / as);
				GroupElement U1 = g.getUnext(cellIndexNew, direction);

				// Induced currents
				AlgebraElement J0 = P.Q0.act(U0).mult(- a * d0); // current at old position
				AlgebraElement J1 = P.Q1.act(U1).mult(- a * d1); // current at new position
				g.addJ(cellIndexOld, direction, J0);
				g.addJ(cellIndexNew, direction, J1);

			}
		}
	}

	public void interpolateChargedensity(IParticle p, Grid g) {
		CGCParticle P = (CGCParticle) p;

		double as = g.getLatticeSpacing();
		int direction = P.direction;
		GroupElement identity = g.getElementFactory().groupIdentity();

		// "Floored" grid points of the particle
		int[] gridPosOld = GridFunctions.flooredGridPoint(P.pos0, as);

		// Cell indices
		int cellIndex0 = g.getCellIndex(gridPosOld);
		int cellIndex1 = g.shift(cellIndex0, direction, 1);

		// Relative distances to the lattice sites
		double d0 = P.pos0[direction] / as - gridPosOld[direction];
		double d1 = 1 - d0;

		// Links (averaged)
		AlgebraElement Aold = g.getU(cellIndex0, direction).getAlgebraElement();
		AlgebraElement Anew = g.getUnext(cellIndex0, direction).getAlgebraElement();
		GroupElement U = Aold.add(Anew).mult(0.5).getLink();

		// Interpolated gauge links
		//GroupElement U0 = U.pow(d0);
		//GroupElement U1 = U.pow(d1).adj();

		// Charge interpolation to neighbouring lattice sites
		AlgebraElement rho0, rho1;
		if(d0 > d1) {
			rho0 = P.Q0.act(U).mult(d1);
			rho1 = P.Q0.mult(d0);
		} else {
			rho0 = P.Q0.mult(d1);
			rho1 = P.Q0.act(U.adj()).mult(d0);
		}

		g.addRho(cellIndex0, rho0);
		g.addRho(cellIndex1, rho1);
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

	private void oneCellMove(CGCParticle P, Grid g, int longitudinalIndexOld, int cellIndexOld) {
		double as = g.getLatticeSpacing();
		double dt = g.getTemporalSpacing();
		double a = as / dt;
		int direction = P.direction;
		double dist = Math.abs(P.pos1[direction] - P.pos0[direction])/as;
		GroupElement identity = g.getElementFactory().groupIdentity();
		AlgebraElement J;
		GroupElement U0, U1;
		// Parallel transport to the left cell.
		double d0 = P.pos0[direction] / as - longitudinalIndexOld;
		if(d0 > 0.5) {
			U0 = g.getUnext(cellIndexOld, direction);
		} else {
			U0 = identity;
		}
		double d1 = P.pos1[direction] / as - longitudinalIndexOld;
		if(d1 > 0.5) {
			U1 = g.getUnext(cellIndexOld, direction);
		} else {
			U1 = identity;
		}

		// This works only for particles moving at c.
		if(P.pos1[direction] > P.pos0[direction]) {
			J = P.Q0.act(U0).mult(a*dist);
		} else {
			J = P.Q1.act(U1).mult(-a*dist);
		}
		g.addJ(cellIndexOld, direction, J);
	}
}
