package org.openpixi.pixi.physics.initial.CGC;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.diagnostics.methods.TadpoleInitialAveraged;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.io.IOException;

/**
 * This class solves the transverse Poisson equation for a three-dimensional (Lorenz gauge) charge density
 * 'sheet by sheet' in the longitudinal direction and then initializes the fields and particles in the temporal gauge.
 */
public class LightConePoissonSolverTadpole implements ICGCPoissonSolver {

	Simulation s;
	AlgebraElement[] gaussViolation;
	TadpoleInitialAveraged computeTadpole;

	/**
	 * Initializes the LightConePoissonSolver. Used to solve the transverse Poisson equation 'sheer by sheet'.
	 * @param s Reference to the Simulation object
	 */
	public void initialize(Simulation s) {

		this.s = s;
		computeTadpole = new TadpoleInitialAveraged("tadpole",0);
		computeTadpole.initialize(s);
	}

	/**
	 * Solves the Poisson equation in the transverse plane for a given 3D charge density distribution. Initializes all
	 * fields (U, Unext and E) and computes the Gauss constraint, which is used to spawn particles.
	 *
	 * @param chargeDensity Reference to an IInitialChargeDensity object.
	 */
	public void solve(IInitialChargeDensity chargeDensity) {
		AlgebraElement[] phi0;
		AlgebraElement[] phi1;
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
		gridCopy.createGrid();

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
			s.grid.setUnext(i,0,V[i]);						//Attention, the Unext matrices are overwritten later!!!
		}

		// Calculate the trace of the tadpole and write it to a file.
		try {
			computeTadpole.setDirection(direction);
			computeTadpole.setOrientation(orientation);
			computeTadpole.setRegulator(chargeDensity.getRegulator());
			computeTadpole.calculate(s.grid, s.particles, 0);
		} catch (IOException ex) {
			System.out.println("TadpoleInitialAveraged Error: Could not write to file tadpole.");
		}

		// Compute phi at t = at/2 from faked charge density movement
		phi1 = new AlgebraElement[s.grid.getTotalNumberOfCells()];
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			phi1[i] = s.grid.getElementFactory().algebraZero();
		}
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			int is = s.grid.shift(i, direction, -orientation);
			double transportRatio = s.getTimeStep() / s.grid.getLatticeSpacing();
			// phi(x) -> phi(x) * (1 - at/as) + phi(x+d) * at/as
			phi1[i] = phi0[i].mult(1.0 - transportRatio).add(phi0[is].mult(transportRatio));
		}

		// Compute V at t = at / 2
		// Reset V
		V = new GroupElement[s.grid.getTotalNumberOfCells()];
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			V[i] = s.grid.getElementFactory().groupIdentity();
		}
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
				gaugeLink.multAssign(phi1[index].mult(gaugeFactor).getLink());
				V[index] = gaugeLink;
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
}
