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
public class CGCParticleInterpolation implements  InterpolatorAlgorithm {
	public void interpolateToGrid(IParticle p, Grid g) {
		CGCParticle P = (CGCParticle) p;

		double as = g.getLatticeSpacing();
		double dt = g.getTemporalSpacing();
		double c = as / dt;
		int direction = P.direction;

		// "Floored" grid points of the particle
		int[] gridPosOld = GridFunctions.flooredGridPoint(P.pos0, as);
		int[] gridPosNew = GridFunctions.flooredGridPoint(P.pos1, as);

		// Cell indices
		int cellIndex0Old = g.getCellIndex(gridPosOld);
		int cellIndex0New = g.getCellIndex(gridPosNew);

		// Relative distances to the lattice sites
		double d0New = P.pos1[direction] / as - gridPosNew[direction];
		double d1New = 1 - d0New;
		double d0Old = P.pos0[direction] / as - gridPosOld[direction];
		double d1Old = 1 - d0Old;

		// Links at old and new position
		GroupElement UOld = g.getUnext(cellIndex0Old, direction);
		GroupElement UNew = g.getU(cellIndex0New, direction);

		// Interpolated gauge links
		GroupElement U0New = UNew.getAlgebraElement().mult(d0New).getLink();
		GroupElement U1New = UNew.getAlgebraElement().mult(d1New).getLink().adj();

		// Charge interpolation to neighbouring lattice sites
		AlgebraElement Q0New = P.Q1.act(U0New).mult(d1New);
		AlgebraElement Q1New = P.Q1.act(U1New).mult(d0New);

		int longitudinalIndexOld = (int) (P.pos0[direction] / as);
		int longitudinalIndexNew = (int) (P.pos1[direction] / as);

		if(longitudinalIndexNew == longitudinalIndexOld) {
			// One-cell move
			GroupElement U0Old = UOld.getAlgebraElement().mult(d0Old).getLink();
			AlgebraElement Q0Old = P.Q0.act(U0Old).mult(d1Old);

			AlgebraElement J = Q0New.sub(Q0Old).mult(-c);
			g.addJ(cellIndex0New, direction, J);

		} else {
			GroupElement U0Old = UOld.getAlgebraElement().mult(d0Old).getLink();
			GroupElement U1Old = UOld.getAlgebraElement().mult(d1Old).getLink().adj();
			AlgebraElement Q0Old = P.Q0.act(U0Old).mult(d1Old);
			AlgebraElement Q1Old = P.Q0.act(U1Old).mult(d0Old);
			if(longitudinalIndexNew > longitudinalIndexOld) {
				// Two-cell move right
				AlgebraElement JOld = Q0Old.mult(c);
				AlgebraElement JNew = JOld.act(g.getU(cellIndex0Old,direction).adj());
				JNew.addAssign(Q0New.sub(Q1Old).mult(-c));

				g.addJ(cellIndex0Old, direction, JOld);
				g.addJ(cellIndex0New, direction, JNew);
			} else {
				// Two-cell move left
				AlgebraElement JNew = Q0New.mult(-c);
				AlgebraElement JOld = JNew.act(UNew.adj());
				JOld.addAssign(Q1New.sub(Q0Old).mult(-c));

				g.addJ(cellIndex0Old, direction, JOld);
				g.addJ(cellIndex0New, direction, JNew);
			}

		}
	}

	public void interpolateChargedensity(IParticle p, Grid g) {
		CGCParticle P = (CGCParticle) p;

		double as = g.getLatticeSpacing();
		int direction = P.direction;

		// "Floored" grid points of the particle
		int[] gridPosNew = GridFunctions.flooredGridPoint(P.pos1, as);

		// Cell indices
		int cellIndex0New = g.getCellIndex(gridPosNew);
		int cellIndex1New = g.shift(cellIndex0New, direction, 1);

		// Relative distances to the lattice sites
		double d0New = P.pos1[direction] / as - gridPosNew[direction];
		double d1New = 1 - d0New;

		// Links
		GroupElement UNew = g.getU(cellIndex0New, direction);

		// Interpolated gauge links
		GroupElement U0New = UNew.getAlgebraElement().mult(d0New).getLink();
		GroupElement U1New = UNew.getAlgebraElement().mult(d1New).getLink().adj();

		// Charge interpolation to neighbouring lattice sites
		AlgebraElement Q0New = P.Q1.act(U0New).mult(d1New);
		AlgebraElement Q1New = P.Q1.act(U1New).mult(d0New);

		g.addRho(cellIndex0New, Q0New);
		g.addRho(cellIndex1New, Q1New);
	}

	public void interpolateToParticle(IParticle p, Grid g) {
		// Compute parallel transport for the particle.
		CGCParticle P = (CGCParticle) p;

		double at = g.getTemporalSpacing();
		double as = g.getLatticeSpacing();
		int direction = P.direction;

		// check if one cell or two cell move
		int longitudinalIndexOld = (int) (P.pos0[direction] / as);
		int longitudinalIndexNew = (int) (P.pos1[direction] / as);


		if(longitudinalIndexOld == longitudinalIndexNew) {
			// one cell move
			int cellIndexNew = g.getCellIndex(GridFunctions.flooredGridPoint(P.pos0, as));
			double d = Math.abs(P.vel[direction] * at / as);
			GroupElement U;
			if(P.vel[direction] > 0) {
				U = g.getU(cellIndexNew, direction).getAlgebraElement().mult(d).getLink();
			} else {
				U = g.getU(cellIndexNew, direction).getAlgebraElement().mult(d).getLink().adj();
			}
			P.U = U;
		} else {
			// two cell move
			int cellIndexOld = g.getCellIndex(GridFunctions.flooredGridPoint(P.pos0, as));
			int cellIndexNew = g.getCellIndex(GridFunctions.flooredGridPoint(P.pos1, as));

			if(longitudinalIndexOld < longitudinalIndexNew) {
				// right move
				// path is split into two parts
				double d0 = Math.abs(longitudinalIndexNew - P.pos0[direction] / as);
				double d1 = Math.abs(longitudinalIndexNew - P.pos1[direction] / as);

				GroupElement U0 = g.getU(cellIndexOld, direction).getAlgebraElement().mult(d0).getLink();
				GroupElement U1 = g.getU(cellIndexNew, direction).getAlgebraElement().mult(d1).getLink();
				GroupElement U = U0.mult(U1);

				P.U = U;
			} else {
				// left move
				// path is split into two parts
				double d0 = Math.abs(longitudinalIndexOld - P.pos0[direction] / as);
				double d1 = Math.abs(longitudinalIndexOld - P.pos1[direction] / as);

				GroupElement U0 = g.getU(cellIndexOld, direction).getAlgebraElement().mult(d0).getLink();
				GroupElement U1 = g.getU(cellIndexNew, direction).getAlgebraElement().mult(d1).getLink();
				GroupElement U = U0.mult(U1);

				P.U = U;
			}
		}
	}
}
