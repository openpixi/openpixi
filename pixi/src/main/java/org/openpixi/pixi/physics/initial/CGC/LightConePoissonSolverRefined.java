package org.openpixi.pixi.physics.initial.CGC;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.GridFunctions;

/**
 * This class solves the transverse Poisson equation for a three-dimensional (Lorenz gauge) charge density
 * 'sheet by sheet' in the longitudinal direction and then initializes the fields in the temporal gauge.
 * It implements an method of computing the Wilson lines using the charge refinement algorithm.
 */
public class LightConePoissonSolverRefined implements ICGCPoissonSolver {

	Simulation s;
	AlgebraElement[] gaussViolation;

	/**
	 * This array stores the values of the Wilson line V at the longitudinal boundary behind the nucleus.
	 * It is used to computed the tadpole and dipole expectation value.
	 */
	GroupElement[] VT;

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
		GroupElement[] V;
		GroupElement[] Vn;

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

		// Longitudinal and transverse lattice spacing
		double aL = s.grid.getLatticeSpacing(direction);
		double aT = s.grid.getLatticeSpacing((direction + 1) % s.getNumberOfDimensions());

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
				double[] phi2D = FourierFunctions.solvePoisson2D(rho2D, transverseNumCells, aT);

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
		Vn = new GroupElement[s.grid.getTotalNumberOfCells()];
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			V[i] = s.grid.getElementFactory().groupIdentity();
			Vn[i] = s.grid.getElementFactory().groupIdentity();
		}

		double gaugeFactor = - s.getCouplingConstant() * aL;
		int pointsPerCell = 8;
		int refinementSteps = 100;
		int n = pointsPerCell * longitudinalNumCells;
		for (int i = 0; i < totalTransverseCells; i++) {
			// Refinement code for phi. Phi is now assumed to be defined at the lattice sites.
			// Similar to the charge refinement procedure, we refine phi in order to more accurately compute the Wilson
			// line and the time evolution operator.

			// Initialize the array to be refined.
			AlgebraElement[] phiR = new AlgebraElement[n];
			for (int j = 0; j < n; j++) {
				int ngpIndex = (int) Math.round((j) / ((double) pointsPerCell));
				int[] transGridPos = GridFunctions.getCellPos(i, transverseNumCells);
				int[] gridPos = GridFunctions.insertGridPos(transGridPos, direction, ngpIndex);
				int index = s.grid.getCellIndex(gridPos);

				phiR[j] = phi0[index].mult(1.0 / ((double) pointsPerCell));
			}

			// Refinement to second order
			for (int l = 0; l < refinementSteps; l++) {
				for (int j = 0; j < n; j++) {
					// Refinement function
					int jmod = j % pointsPerCell;
					// Refinement can not be applied to the last charge in an NGP cell.
					if(jmod >= 0 && jmod < pointsPerCell-1)
					{
						int i0 = p(j - 1, n);
						int i1 = p(j + 0, n);
						int i2 = p(j + 1, n);
						int i3 = p(j + 2, n);

						AlgebraElement Q0 = phiR[i0];
						AlgebraElement Q1 = phiR[i1];
						AlgebraElement Q2 = phiR[i2];
						AlgebraElement Q3 = phiR[i3];

						AlgebraElement DQ = Q0.mult(-1);
						DQ.addAssign(Q1.mult(3));
						DQ.addAssign(Q2.mult(-3));
						DQ.addAssign(Q3.mult(1));
						DQ.multAssign(1.0 / 4.0);

						Q1.addAssign(DQ.mult(-1.0));
						Q2.addAssign(DQ.mult(1.0));
					}
				}
			}

			// Refinement to quartic order
			for (int l = 0; l < refinementSteps; l++) {
				for (int j = 0; j < n; j++) {
					// Refinement function
					int jmod = j % pointsPerCell;
					// Refinement can not be applied to the last charge in an NGP cell.
					if (jmod >= 0 && jmod < pointsPerCell - 1) {
						int i0 = p(j - 2, n);
						int i1 = p(j - 1, n);
						int i2 = p(j + 0, n);
						int i3 = p(j + 1, n);
						int i4 = p(j + 2, n);
						int i5 = p(j + 3, n);

						AlgebraElement Q0 = phiR[i0];
						AlgebraElement Q1 = phiR[i1];
						AlgebraElement Q2 = phiR[i2];
						AlgebraElement Q3 = phiR[i3];
						AlgebraElement Q4 = phiR[i4];
						AlgebraElement Q5 = phiR[i5];

						AlgebraElement DQ = Q0.mult(+1);
						DQ.addAssign(Q1.mult(-5));
						DQ.addAssign(Q2.mult(+10));
						DQ.addAssign(Q3.mult(-10));
						DQ.addAssign(Q4.mult(+5));
						DQ.addAssign(Q5.mult(-1));
						DQ.multAssign(1.0 / 12.0);

						Q2.addAssign(DQ.mult(-1.0));
						Q3.addAssign(DQ.mult(1.0));
					}
				}
			}

			// Compute V (at t=-at/2) and Vn (at t=at/2) from refined phiR and sublattice gauge links.
			for (int j = 0; j < longitudinalNumCells; j++) {
				// Wilson line calculation depends on orientation of the nucleus movment: it always starts in front of
				// the nucleus.
				int z = (orientation < 0) ? j : (longitudinalNumCells - j - 1);
				int[] transGridPos = GridFunctions.getCellPos(i, transverseNumCells);
				int[] gridPos = GridFunctions.insertGridPos(transGridPos, direction, z);
				int index = s.grid.getCellIndex(gridPos);

				// Last position in longitudinal direction at same transverse position.
				int indexL = s.grid.shift(index, direction, orientation);

				// Wilson line from one lattice site to the next. Built from sub-lattice gauge links.
				GroupElement W = s.grid.getElementFactory().groupIdentity();
				for (int k = 0; k < pointsPerCell; k++) {
					int rIndex = p(pointsPerCell * (z + orientation) - orientation * k, n);
					W.multAssign(phiR[rIndex].mult(gaugeFactor).getLink());
				}

				// Full Wilson line at t = - at/2.
				GroupElement gaugeLink = V[indexL].copy();
				gaugeLink.multAssign(W);
				V[index] = gaugeLink;

				// Wilson line for next step. Built from sub-lattice gauge links as well, but for a shorter path.
				// This is equivalent to the computing the time evolution operator for the Wilson line.
				W = s.grid.getElementFactory().groupIdentity();
				int extraSteps = (int) (pointsPerCell * s.grid.getTemporalSpacing() / aL);
				for (int k = 0; k < extraSteps; k++) {
					int rIndex = p(pointsPerCell * z - orientation * k, n);
					W.multAssign(phiR[rIndex].mult(gaugeFactor).getLink());
				}

				// Full Wilson line at t = at/2.
				gaugeLink = V[index].copy();
				gaugeLink.multAssign(W);
				Vn[index] = gaugeLink;


			}
		}

		// Store V at longitudinal boundary behind nucleus.
		VT = new GroupElement[totalTransverseCells];
		for (int i = 0; i < totalTransverseCells; i++) {
			// Longitudinal coordinate of transverse plane "far behind" nucleus.
			int z = (orientation > 0) ? 0 : (longitudinalNumCells - 1);
			int[] transPos = GridFunctions.getCellPos(i, transverseNumCells);
			int[] gridPos = GridFunctions.insertGridPos(transPos, direction, z);
			int index = s.grid.getCellIndex(gridPos);

			VT[i] = V[index].copy();
		}

		// Make a copy of the grid. Ugly, but needed for Gauss constraint calculation.
		Grid gridCopy = new Grid(s.grid);
		//gridCopy.createGrid();

		// Set gauge links at t = - at/2
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			for (int d = 0; d < s.getNumberOfDimensions(); d++) {
				if(d != direction) {
					int is = s.grid.shift(i, d, 1);
					// Gauge links at t = -at/2
					GroupElement V1 = V[i];
					GroupElement V2 = V[is];

					GroupElement U = s.grid.getU(i, d);
					// U_x,i = V_x V_{x+i}^t
					s.grid.setU(i, d, V1.mult(U).mult(V2.adj()));
					// Also write to copy of the grid.
					gridCopy.setU(i, d, V1.mult(V2.adj()));

					// Gauge links at t = +at/2
					V1 = Vn[i];
					V2 = Vn[is];

					U = s.grid.getUnext(i, d);
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
	 * Applies periodic boundary conditions to the index i.
	 * @param i index
	 * @param n total lattice points
	 * @return  index wrapped around the periodic boundary
	 */
	private int p(int i, int n) {
		return (i % n + n) % n;
	}

	public AlgebraElement getGaussViolation(int index) {
		return this.gaussViolation[index];
	}

	public AlgebraElement[] getGaussViolation() {
		return this.gaussViolation;
	}

	public GroupElement[] getV() {return  this.VT; }
}
