package org.openpixi.pixi.physics.initial.CGC;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.GridFunctions;

/**
 * This class solves the transverse Poisson equation for a three-dimensional (Lorenz gauge) charge density
 * 'sheet by sheet' in the longitudinal direction and then initializes the fields in the temporal gauge.
 * It implements an improved method of computing the Wilson lines which reduces spurious longitudinal fields.
 */
public class LightConePoissonSolverImproved implements ICGCPoissonSolver {

	Simulation s;
	AlgebraElement[] gaussViolation;

	/**
	 * Initializes the LightConePoissonSolver. Used to solve the transverse Poisson equation 'sheer by sheet'.
	 * @param s Reference to the Simulation object
	 */
	public void initialize(Simulation s) {

		this.s = s;
	}

	/**
	 * Solves the Poisson equation in the transverse plane for a given 3D charge density distribution. Initializes all
	 * fields (U, Unext and E) and computes the Gauss constraint, which is used to spawn particles.
	 *
	 * @param chargeDensity Reference to an IInitialChargeDensity object.
	 */
	public void solve(IInitialChargeDensity chargeDensity) {
		AlgebraElement[] phi0;
/*		AlgebraElement[] phi1;*/
		AlgebraElement[] deltaphi;
		GroupElement[] V;

		int direction = chargeDensity.getDirection();
		int orientation = chargeDensity.getOrientation();

		int longitudinalNumCells = s.grid.getNumCells(direction);
		int[] transverseNumCells = GridFunctions.reduceGridPos(s.grid.getNumCells(), direction);
		int totalTransverseCells = GridFunctions.getTotalNumberOfCells(transverseNumCells);
		int numberOfColors = s.getNumberOfColors();
		int numberOfComponents = (numberOfColors > 1) ? numberOfColors * numberOfColors - 1 : 1;

		// Solve for phi at t = - at/2 'sheet by sheet'
		phi0 = new AlgebraElement[s.grid.getTotalNumberOfCells()];
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			phi0[i] = s.grid.getElementFactory().algebraZero();
		}

		for (int z = 0; z < longitudinalNumCells; z++) {
			for (int c = 0; c < numberOfComponents; c++) {
				// Prepare 2D charge density.
				double[] rho2D = new double[totalTransverseCells];
				for (int i = 0; i < totalTransverseCells; i++) {
					int[] transGridPos = GridFunctions.getCellPos(i, transverseNumCells);
					int[] gridPos = GridFunctions.insertGridPos(transGridPos, direction, z);
					int index = s.grid.getCellIndex(gridPos);

					rho2D[i] = chargeDensity.getChargeDensity(index).get(c);
				}

				// Solve Poisson equation
				double[] phi2D = FourierFunctions.solvePoisson2D(rho2D, transverseNumCells, s.grid.getLatticeSpacing());

				// Put result into phi0.
				for (int i = 0; i < totalTransverseCells; i++) {
					int[] transGridPos = GridFunctions.getCellPos(i, transverseNumCells);
					int[] gridPos = GridFunctions.insertGridPos(transGridPos, direction, z);
					int index = s.grid.getCellIndex(gridPos);

					// Already include gauge factor for Wilson line here.
					phi0[index].set(c, phi2D[i]);
				}
			}
		}

		// Compute V at t = - at/2 by constructing the Wilson line from gauge links.
		V = new GroupElement[s.grid.getTotalNumberOfCells()];
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			V[i] = s.grid.getElementFactory().groupIdentity();
		}

		// Is the multiplication with orientation correct?
		double gaugeFactor = orientation * s.getCouplingConstant() * s.grid.getLatticeSpacing();
		for (int k = 0; k < longitudinalNumCells; k++) {
			int z = (orientation < 0) ? k : (longitudinalNumCells - k - 1);
			for (int i = 0; i < totalTransverseCells; i++) {
				// Current position
				int[] transGridPos = GridFunctions.getCellPos(i, transverseNumCells);
				int[] gridPos = GridFunctions.insertGridPos(transGridPos, direction, z);
				int index = s.grid.getCellIndex(gridPos);

				// Last position in longitudinal direction at same transverse position
				int indexL = s.grid.shift(index, direction, orientation);

				// Compute V from V directly behind it in the longitudinal direction.
				GroupElement gaugeLink = V[indexL].copy();
				gaugeLink.multAssign(phi0[index].mult(gaugeFactor).getLink());
				V[index] = gaugeLink;
			}
		}

		// Make a copy of the grid. Ugly, but needed for Gauss constraint calculation.
		Grid gridCopy = new Grid(s.grid);
		//gridCopy.createGrid();

		// Set gauge links at t = - at/2
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			for (int d = 0; d < s.getNumberOfDimensions(); d++) {
				if(d != direction) {
					int is = s.grid.shift(i, d, 1);
					GroupElement V1 = V[i];
					GroupElement V2 = V[is];

					GroupElement U = s.grid.getU(i, d);
					// U_x,i = V_x V_{x+i}^t
					s.grid.setU(i, d, V1.mult(U).mult(V2.adj()));
					// Also write to copy of the grid.
					gridCopy.setU(i, d, V1.mult(V2.adj()));
				}
			}
		}

		// Compute V at at/2 from V at -at/2 using an approximation of the time evolution operator and the fact that
		// V(z,t) only depends on the single argument z - t.
		int M = 100;
		int N = 3;

		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			int j = s.grid.shift(i, direction, -orientation);
			int iL = s.grid.shift(j, direction, orientation);
			int iR = s.grid.shift(j, direction, -orientation);
			GroupElement W = s.grid.getElementFactory().groupIdentity();
			AlgebraElement[] P = new AlgebraElement[3];
			P[0] = phi0[j];
			P[1] = phi0[iR].sub(phi0[iL]);
			P[1].multAssign(1.0 / (2 * s.grid.getLatticeSpacing()));
			P[2] = phi0[iL].add(phi0[iR]).sub(phi0[j].mult(2.0));
			P[2].multAssign(1.0 / Math.pow(s.grid.getLatticeSpacing(), 2));

			for (int m = 0; m < M; m++) {
				AlgebraElement w = w(m, M, N, s.grid.getTemporalSpacing(), s.getCouplingConstant(), P);
				W.multAssign(w.getLink());
			}

			V[i] = W.mult(V[i]);
			//V[i].multAssign(W);
		}

		// Set gauge links at t = at/2
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			for (int d = 0; d < s.getNumberOfDimensions(); d++) {
				if(d != direction) {
					int is = s.grid.shift(i, d, 1);
					GroupElement V1 = V[i];
					GroupElement V2 = V[is];

					GroupElement U = s.grid.getUnext(i, d);
					// U_x,i = V_x V_{x+i}^t
					s.grid.setUnext(i, d, V1.mult(U).mult(V2.adj()));
					// Also write to copy of the grid.
					gridCopy.setUnext(i, d, V1.mult(V2.adj()));
				}
			}
		}

		// Compute electric field at t = 0
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			for (int j = 0; j < s.getNumberOfDimensions(); j++) {
				s.grid.setE(i, j, s.grid.getEFromLinks(i, j));
				gridCopy.setE(i, j, gridCopy.getEFromLinks(i, j));
			}
		}

		// Compute Gauss constraint from grid copy
		gaussViolation = new AlgebraElement[s.grid.getTotalNumberOfCells()];
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			if(gridCopy.isActive(i)) {
				this.gaussViolation[i] = gridCopy.getGaussConstraint(i);
			} else {
				this.gaussViolation[i] = gridCopy.getElementFactory().algebraZero();
			}
		}
	}

	/**
	 * Computes a part of the time evolution operator at time step m for a total of M steps up to spatial order N.
	 *
	 * @param m     fractional time step
	 * @param M     total fractional time steps
	 * @param N     order of spatial approximation
	 * @param at    time step
	 * @param P     array of algebra elements containing finite differences of phi to order N.
	 * @return      algebra element of the time evolution operator
	 */
	private AlgebraElement w(int m, int M, int N, double at, double gaugeFactor, AlgebraElement[] P) {
		AlgebraElement w = P[0].copy();
		double dt = at / (1.0 * M);
		double fact = 1.0;
		for (int n = 1; n < N; n++) {
			int sign = (n % 2 == 0) ? 1 : -1;
			fact *= n;
			double tau = m * dt;
			w.addAssign(P[n].mult(sign * Math.pow(tau, n) / fact));
		}
		w.multAssign(dt * gaugeFactor);
		return w;
	}

	public AlgebraElement getGaussViolation(int index) {
		return this.gaussViolation[index];
	}

	public AlgebraElement[] getGaussViolation() {
		return this.gaussViolation;
	}
}
