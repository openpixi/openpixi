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
		double a = as / dt;
		int direction = P.direction;

		// "Floored" grid points of the particle
		int[] gridPosOld = GridFunctions.flooredGridPoint(P.pos0, as);
		int[] gridPosNew = GridFunctions.flooredGridPoint(P.pos1, as);

		// Cell indices
		int cellIndexOld = g.getCellIndex(gridPosOld);
		int cellIndexNew = g.getCellIndex(gridPosNew);

		// Longitudinal indices
		int longitudinalIndexOld = (int) (P.pos0[direction] / as);
		int longitudinalIndexNew = (int) (P.pos1[direction] / as);


		if(longitudinalIndexOld == longitudinalIndexNew) {
			// One-cell move
			AlgebraElement J;
			// Parallel transport to the left cell.
			double d = P.pos0[direction] / as - longitudinalIndexOld;
			GroupElement U = g.getUnext(cellIndexOld, direction).pow(d);

			// This works only for particles moving at c.
			if(P.pos1[direction] > P.pos0[direction]) {
				J = P.Q0.act(U);
			} else {
				J = P.Q0.act(U).mult(-1.0);
			}
			g.addJ(cellIndexNew, direction, J);
		} else {
			if(longitudinalIndexOld < longitudinalIndexNew) {
				// Two-cell move to the right
				// Parallel transport to the left cell.
				double d0 = Math.abs(longitudinalIndexOld - P.pos0[direction] / as);
				GroupElement U0 = g.getUnext(cellIndexOld, direction).pow(d0);
				// Parallel transport to the right cell.
				double d1 = 1 - d0;
				GroupElement U1 = g.getUnext(cellIndexOld, direction).pow(d1).adj();

				// Shape factors
				double sold = Math.abs(longitudinalIndexNew - P.pos0[direction] / as);
				double snew = Math.abs(longitudinalIndexNew - P.pos1[direction] / as);

				// Induced currents
				AlgebraElement Jold = P.Q0.act(U0).mult(a * sold); // current at old position
				AlgebraElement Jnew = P.Q0.act(U1).mult(a * snew); // current at new position
				g.addJ(cellIndexOld, direction, Jold);
				g.addJ(cellIndexNew, direction, Jnew);

			} else {
				// Two-cell move to the left
				// Parallel transport to the left cell.
				double d0 = Math.abs(longitudinalIndexNew - P.pos1[direction] / as);
				GroupElement U0 = g.getUnext(cellIndexNew, direction).pow(d0);
				// Parallel transport to the right cell.
				double d1 = 1 - d0;
				GroupElement U1 = g.getUnext(cellIndexNew, direction).pow(d1).adj();

				// Shape factors
				double sold = Math.abs(longitudinalIndexOld - P.pos0[direction] / as);
				double snew = Math.abs(longitudinalIndexOld - P.pos1[direction] / as);

				// Induced currents
				AlgebraElement J0 = P.Q1.act(U1).mult(- a * sold); // current at old position
				AlgebraElement J1 = P.Q1.act(U0).mult(- a * snew); // current at new position
				g.addJ(cellIndexOld, direction, J0);
				g.addJ(cellIndexNew, direction, J1);
			}
		}

		/*

		// Relative distances to the lattice sites
		double d0New = P.pos1[direction] / as - gridPosNew[direction];
		double d1New = 1 - d0New;
		double d0Old = P.pos0[direction] / as - gridPosOld[direction];
		double d1Old = 1 - d0Old;

		// Links at old and new position
		GroupElement UOld = g.getUnext(cellIndex0Old, direction);
		GroupElement UNew = g.getUnext(cellIndex0New, direction);

		// Interpolated gauge links
		GroupElement U0New = UNew.pow(d0New);
		GroupElement U1New = UNew.pow(d1New).adj();

		// Charge interpolation to neighbouring lattice sites
		AlgebraElement Q0New = P.Q1.act(U0New).mult(d1New);
		AlgebraElement Q1New = P.Q1.act(U1New).mult(d0New);

		int longitudinalIndexOld = (int) (P.pos0[direction] / as);
		int longitudinalIndexNew = (int) (P.pos1[direction] / as);

		if(longitudinalIndexNew == longitudinalIndexOld) {
			// One-cell move
			GroupElement U0Old = UOld.pow(d0Old);
			AlgebraElement Q0Old = P.Q0.act(U0Old);
			AlgebraElement J;

			// This is only correct for particles moving at c.
			if(P.pos1[direction] - P.pos0[direction] > 0) {
				J = Q0Old;
			} else {
				J = Q0Old.mult(-1.0);
			}
			g.addJ(cellIndex0New, direction, J);

		} else {
			GroupElement U0Old = UOld.pow(d0Old);
			GroupElement U1Old = UOld.pow(d1Old).adj();
			AlgebraElement Q0Old = P.Q0.act(U0Old).mult(d1Old);
			AlgebraElement Q1Old = P.Q0.act(U1Old).mult(d0Old);
			if(longitudinalIndexNew > longitudinalIndexOld) {
				// Two-cell move right
				AlgebraElement JOld = Q0Old.mult(a);
				AlgebraElement JNew = JOld.act(g.getU(cellIndex0Old,direction).adj());
				JNew.addAssign(Q0New.sub(Q1Old).mult(-a));

				g.addJ(cellIndex0Old, direction, JOld);
				g.addJ(cellIndex0New, direction, JNew);
			} else {
				// Two-cell move left
				AlgebraElement JNew = Q0New.mult(-a);
				AlgebraElement JOld = JNew.act(UNew.adj());
				JOld.addAssign(Q1New.sub(Q0Old).mult(-a));

				g.addJ(cellIndex0Old, direction, JOld);
				g.addJ(cellIndex0New, direction, JNew);
			}

		}
		*/
	}

	public void interpolateChargedensity(IParticle p, Grid g) {
		CGCParticle P = (CGCParticle) p;

		double as = g.getLatticeSpacing();
		int direction = P.direction;

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
		GroupElement U0 = U.pow(d0).adj();
		GroupElement U1 = U.pow(d1);

		// Charge interpolation to neighbouring lattice sites
		AlgebraElement rho0 = P.Q0.act(U0).mult(d1);
		AlgebraElement rho1 = P.Q0.act(U1).mult(d0);

		g.addRho(cellIndex0, rho0);
		g.addRho(cellIndex1, rho1);
	}

	public void interpolateToParticle(IParticle p, Grid g) {
		// Compute parallel transport for the particle.
		CGCParticle P = (CGCParticle) p;

		double at = g.getTemporalSpacing();
		double as = g.getLatticeSpacing();
		int direction = P.direction;

		// Particle positions
		double[] oldPosition = P.pos0;
		double[] newPosition = P.pos1;

		// check if one cell or two cell move
		int longitudinalIndexOld = (int) (oldPosition[direction] / as);
		int longitudinalIndexNew = (int) (newPosition[direction] / as);


		if(longitudinalIndexOld == longitudinalIndexNew) {
			// one cell move
			int cellIndex = g.getCellIndex(GridFunctions.flooredGridPoint(oldPosition, as));
			double d = Math.abs(P.vel[direction] * at / as);
			GroupElement U;
			if(P.vel[direction] > 0) {
				U = g.getUnext(cellIndex, direction).pow(d);
			} else {
				U = g.getUnext(cellIndex, direction).pow(d).adj();
			}
			P.U = U;
		} else {
			// two cell move
			int cellIndexOld = g.getCellIndex(GridFunctions.flooredGridPoint(oldPosition, as));
			int cellIndexNew = g.getCellIndex(GridFunctions.flooredGridPoint(newPosition, as));

			if(longitudinalIndexOld < longitudinalIndexNew) {
				// right move
				// path is split into two parts
				double d0 = Math.abs(longitudinalIndexNew - oldPosition[direction] / as);
				double d1 = Math.abs(longitudinalIndexNew - newPosition[direction] / as);

				GroupElement U0 = g.getUnext(cellIndexOld, direction).pow(d0);
				GroupElement U1 = g.getUnext(cellIndexNew, direction).pow(d1);
				GroupElement U = U0.mult(U1);

				P.U = U;
			} else {
				// left move
				// path is split into two parts
				double d0 = Math.abs(longitudinalIndexOld - oldPosition[direction] / as);
				double d1 = Math.abs(longitudinalIndexOld - newPosition[direction] / as);

				GroupElement U0 = g.getUnext(cellIndexOld, direction).pow(d0).adj();
				GroupElement U1 = g.getUnext(cellIndexNew, direction).pow(d1).adj();
				GroupElement U = U0.mult(U1);

				P.U = U;
			}
		}
	}
}
