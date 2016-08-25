package org.openpixi.pixi.physics.initial.CGC;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.GridFunctions;

/**
 * This class solves the transverse Poisson equation for a three-dimensional (Lorenz gauge) charge density
 * 'sheet by sheet' in the longitudinal direction and then initializes the fields in the temporal gauge.
 */
public class LightConePoissonSolver implements ICGCPoissonSolver {

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

		// Longitudinal and transverse lattice spacing
		double aL = s.grid.getLatticeSpacing(direction);
		double aT = s.grid.getLatticeSpacing((direction + 1) % s.getNumberOfDimensions());

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
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			V[i] = s.grid.getElementFactory().groupIdentity();
		}

		// Is the multiplication with orientation correct?
		double gaugeFactor = - s.getCouplingConstant() * aL;
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

		// Compute deltaphi to switch from t = -at / 2 to t = at / 2.
		// (Here we assume that the charge within a cell is uniformly distributed.
		// A more sophisticated method to determine deltaphi would correspond
		// to a charge distribution as assumed after charge refinement.)
		deltaphi = new AlgebraElement[s.grid.getTotalNumberOfCells()];
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {

			double transportRatio = s.getTimeStep() / aL;

			// deltaphi(x) -> phi(x) * at/as
			deltaphi[i] = phi0[i].mult(transportRatio);
		}

		// Compute V at t = at / 2 by adjusting V at t = - at / 2 slightly
		for (int k = 0; k < longitudinalNumCells; k++) {
			int z = (orientation < 0) ? k : (longitudinalNumCells - k - 1);
			for (int i = 0; i < totalTransverseCells; i++) {
				// Current position
				int[] transGridPos = GridFunctions.getCellPos(i, transverseNumCells);
				int[] gridPos = GridFunctions.insertGridPos(transGridPos, direction, z);
				int index = s.grid.getCellIndex(gridPos);

				// Last position in longitudinal direction at same transverse position
				int indexL = s.grid.shift(index, direction, orientation);

				GroupElement deltaV = deltaphi[index].mult(gaugeFactor).getLink();

				// Adjust V by slightly adding a contribution from the next longitudinal position
				V[indexL] = V[indexL].mult(deltaV);
			}
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

	public AlgebraElement getGaussViolation(int index) {
		return this.gaussViolation[index];
	}

	public AlgebraElement[] getGaussViolation() {
		return this.gaussViolation;
	}

	public GroupElement[] getV() {return  this.VT; }
}
