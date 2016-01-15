package org.openpixi.pixi.diagnostics.methods;

import java.io.IOException;
import java.util.ArrayList;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;

/**
 * Shared buffer that stores previous values of the grid for calculating pieces of
 * the Poynting Theorem.
 */
public class PoyntingTheoremBuffer implements Diagnostics {

	Simulation s;
	private boolean calculateEnergyDensityDerivative = false;
	private int oldTime;
	private int currentTime;
	private double[] oldEnergyDensity;
	private double[] currentEnergyDensity;

	private boolean storeOldEJ = false;
	private AlgebraElement[][] Ecurrent;
	private AlgebraElement[][] Eold;
	private AlgebraElement[][] Jcurrent;
	private AlgebraElement[][] Jold;
	private AlgebraElement[][] RotEcurrent;
	private AlgebraElement[][] RotEold;

	private double integratedDivS1;
	private double integratedDivS2;
	private double integratedJE;
	private boolean currentDivS1Calculated;
	private boolean currentDivS2Calculated;
	private boolean currentJECalculated;
	private double currentDivS1;
	private double currentDivS2;
	private double currentJE;

	PoyntingTheoremBuffer(Simulation s) {
		this.s = s;
	}

	@Override
	public void initialize(Simulation s) {
		this.s = s;
		resetEnergyDensityDerivative();
		calculateEnergyDensityDerivative = true;
		resetEJ();
		storeOldEJ = true;
		integratedDivS1 = 0;
		integratedDivS2 = 0;
		integratedJE = 0;
		currentDivS1Calculated = false;
		currentDivS2Calculated = false;
		currentJECalculated = false;
	}

	@Override
	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps)
			throws IOException {
		if (calculateEnergyDensityDerivative) {
			updateEnergyDensityDerivative();
			if (s.totalSimulationSteps > 2) {
				// Stop calculating the derivative unless the method getEnergyDensityDerivative()
				// is explicitly called below after each iteration
				calculateEnergyDensityDerivative = false;
			}
		} else {
			resetEnergyDensityDerivative();
		}

		if (storeOldEJ) {
			updateEJ();
			if (s.totalSimulationSteps > 2) {
				storeOldEJ = false;
			}
		} else {
			resetEJ();
		}

		currentDivS1Calculated = false;
		currentDivS2Calculated = false;
		currentJECalculated = false;
	}

	private void resetEnergyDensityDerivative() {
		oldTime = -1;
		currentTime = -1;
		oldEnergyDensity = null;
		currentEnergyDensity = null;
	}

	private void updateEnergyDensityDerivative() {
		if (oldEnergyDensity == null) {
			// Initialize arrays
			int cells = s.grid.getTotalNumberOfCells();
			oldTime = -1;
			currentTime = -1;
			oldEnergyDensity = new double[cells];
			currentEnergyDensity = new double[cells];
		}
		oldTime = currentTime;
		currentTime = s.totalSimulationSteps;
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			oldEnergyDensity[i] = currentEnergyDensity[i];
			currentEnergyDensity[i] = getEnergyDensity2(i);
		}
	}

	private void resetEJ() {
		Ecurrent = null;
		Eold = null;
		Jcurrent = null;
		Jold = null;
		RotEcurrent = null;
		RotEold = null;
	}

	private void updateEJ() {
		int cells = s.grid.getTotalNumberOfCells();
		int dimensions = s.grid.getNumberOfDimensions();
		if (Ecurrent == null) {
			// Initialize arrays
			Ecurrent = new AlgebraElement[cells][];
			Eold = new AlgebraElement[cells][];
			Jcurrent = new AlgebraElement[cells][];
			Jold = new AlgebraElement[cells][];
			RotEcurrent = new AlgebraElement[cells][];
			RotEold = new AlgebraElement[cells][];
		}
		for (int i = 0; i < cells; i++) {
			Eold[i] = Ecurrent[i];
			Jold[i] = Jcurrent[i];
			RotEold[i] = RotEcurrent[i];
			Ecurrent[i] = new AlgebraElement[dimensions];
			Jcurrent[i] = new AlgebraElement[dimensions];
			RotEcurrent[i] = new AlgebraElement[dimensions];
			// Fields are copied when they are used
		}
	}

	/**
	 * Gets an instance of PoyntingTheoremBuffer from the list of diagnostics
	 * objects of the simulation object. If no instance existed previously,
	 * a new instance is created and added to the list of diagnostics objects.
	 * @param s Simulation object
	 * @return Existing or new PoyntingTheoremBuffer
	 */
	public static PoyntingTheoremBuffer getOrAppendInstance(Simulation s) {
		ArrayList<Diagnostics> diagnostics = s.getDiagnosticsList();
		for (Diagnostics d : diagnostics) {
			if (d instanceof PoyntingTheoremBuffer) {
				return (PoyntingTheoremBuffer) d;
			}
		}
		PoyntingTheoremBuffer p = new PoyntingTheoremBuffer(s);
		diagnostics.add(p);
		return p;
	}

	@Deprecated
	private double getEnergyDensity(int index) {
		double value = 0;

		// Lattice spacing and coupling constant
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		for (int w = 0; w < s.getNumberOfDimensions(); w++) {
			value += s.grid.getEsquaredFromLinks(index, w);
			// Time averaging for B field.
			value += 0.5 * s.grid.getBsquaredFromLinks(index, w, 0);
			value += 0.5 * s.grid.getBsquaredFromLinks(index, w, 1);
		};
		return value / (as * g * as * g) / 2;
	}

	/**
	 * Calculates the energy density at the time of the E-field
	 * correctly through order O(t^2).
	 * @param index cell index
	 * @return energy density
	 */
	public double getEnergyDensity2(int index) {
		double value = 0;

		// Lattice spacing and coupling constant
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		for (int w = 0; w < s.getNumberOfDimensions(); w++) {
			value += s.grid.getE(index, w).square();
			// Time averaging for B field.
			AlgebraElement B = s.grid.getB(index, w, 0);
			AlgebraElement Bnext = s.grid.getB(index, w, 1);
			value += (B.add(Bnext)).mult(0.5).square();
		};
		return value / (as * g * as * g) / 2;
	}

	/**
	 * Calculates the derivative of the energy density
	 * at the time of the B-field correctly through order O(t^2).
	 * @param index cell index
	 * @return derivative of energy density
	 */
	public double getEnergyDensityDerivative(int index) {
		double value = 0;
		calculateEnergyDensityDerivative = true;
		if (currentEnergyDensity == null) {
			// Buffer has not been initialized yet
			//updateEnergyDensityDerivative();
			return 0;
		}
		if (oldTime >= 0) {
			// Calculate derivative
			double deltaTime = (currentTime - oldTime) * s.tstep;
			value = (currentEnergyDensity[index] - oldEnergyDensity[index]) / deltaTime;
		}
		return value;
	}

	@Deprecated
	public double getDivPoyntingVector(int index) {
		double as = s.grid.getLatticeSpacing();

		double value = 0;
		if (s.getNumberOfDimensions() != 3) {
			throw new RuntimeException("Dimension other than 3 has not been implemented yet.");
			// TODO: Implement for arbitrary dimensions
			// return 0;
		}
		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			int indexShifted1 = s.grid.shift(index, direction, 1);
			if (!s.grid.isEvaluatable(indexShifted1)) {
				return 0;
			}
			int indexShifted2 = s.grid.shift(index, direction, -1);
			if (!s.grid.isEvaluatable(indexShifted2)) {
				return 0;
			}
			value += getPoyntingVector(indexShifted1, direction)
					- getPoyntingVector(indexShifted2, direction);
		}
		return value / (2*as);
	}

	@Deprecated
	public double getPoyntingVector(int index, int direction) {
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		// Indices for cross product:
		int dir1 = (direction + 1) % 3;
		int dir2 = (direction + 2) % 3;

		// fields at same time:
		AlgebraElement E1 = s.grid.getE(index, dir1);
		AlgebraElement E2 = s.grid.getE(index, dir2);
		// time averaged B-field:
		AlgebraElement B1 = s.grid.getB(index, dir1, 0).add(s.grid.getB(index, dir1, 1)).mult(0.5);
		AlgebraElement B2 = s.grid.getB(index, dir2, 0).add(s.grid.getB(index, dir2, 1)).mult(0.5);
		double S = E1.mult(B2) - E2.mult(B1);
		return S / (as * g * as * g);
	}

	/**
	 * Calculates the divergence of the Poynting vector on the lattice.
	 * Actually, B rot E - E rot B is calculated.
	 * @param index cell index
	 * @return B rot E - E rot B
	 */
	@Deprecated
	public double getDivPoyntingVector2(int index) {
		storeOldEJ = true;
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		double value = 0;
		if (s.getNumberOfDimensions() != 3) {
			throw new RuntimeException("Dimension other than 3 has not been implemented yet.");
			// TODO: Implement for arbitrary dimensions
			// return 0;
		}
		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			AlgebraElement rotE = s.grid.getRotE(index, direction);
			AlgebraElement rotB0 = s.grid.getRotB(index, direction, 0);
			AlgebraElement rotB1 = s.grid.getRotB(index, direction, 1);

			AlgebraElement E = s.grid.getE(index, direction);
			// time averaged B-fields:
			AlgebraElement B = (s.grid.getB(index, direction, 0).add(s.grid.getB(index, direction, 1))).mult(0.5);
			AlgebraElement rotB = (rotB0.add(rotB1)).mult(0.5);

			value += B.mult(rotE) - E.mult(rotB);
		}
		return value / (as * g * as * g);
	}

	/**
	 * Calculates the divergence of the Poynting vector on the lattice
	 * at the time of the B-field correctly through order O(t^2).
	 * Actually, B rot E - E rot B is calculated.
	 * @param index cell index
	 * @return B rot E - E rot B
	 */
	public double getDivPoyntingVector3(int index) {
		storeOldEJ = true;
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		double value = 0;
		if (s.getNumberOfDimensions() != 3) {
			throw new RuntimeException("Dimension other than 3 has not been implemented yet.");
			// TODO: Implement for arbitrary dimensions
			// return 0;
		}
		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			// Time-averaged at time of B-field
			AlgebraElement rotE = s.grid.getRotE(index, direction);
			if (RotEcurrent != null) {
				RotEcurrent[index][direction] = rotE.copy();
				if (RotEold != null && RotEold[index] != null && RotEold[index][direction] != null) {
					// Form average
					rotE = (rotE.add(RotEold[index][direction])).mult(0.5);
				}
			}

			AlgebraElement E = s.grid.getE(index, direction);
			if (Ecurrent != null) {
				Ecurrent[index][direction] = E.copy();
				if (Eold != null && Eold[index] != null && Eold[index][direction] != null) {
					// Form average
					E = (E.add(Eold[index][direction])).mult(0.5);
				}
			}

			AlgebraElement rotB = s.grid.getRotB(index, direction, 0);
			AlgebraElement B = s.grid.getB(index, direction, 0);

			value += B.mult(rotE) - E.mult(rotB);
		}
		return value / (as * g * as * g);
	}

	/**
	 * Calculates the divergence of the Poynting vector on the lattice
	 * at the time of the B-field correctly through order O(t^2).
	 * @param index cell index
	 * @return divergence of Poynting vector
	 */
	public double getDivPoyntingVector4(int index) {
		double as = s.grid.getLatticeSpacing();

		double value = 0;
		if (s.getNumberOfDimensions() != 3) {
			throw new RuntimeException("Dimension other than 3 has not been implemented yet.");
			// TODO: Implement for arbitrary dimensions
			// return 0;
		}
		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			int indexShifted1 = s.grid.shift(index, direction, -1);
			value += getPoyntingVector3(index, direction)
					- getPoyntingVector3(indexShifted1, direction);
		}
		return value / (as);
	}

	@Deprecated
	public double getPoyntingVector2(int index, int direction) {
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		// Indices for cross product:
		int dir1 = (direction + 1) % 3;
		int dir2 = (direction + 2) % 3;

		int indexShifted1 = s.grid.shift(index, dir1, 1);
		int indexShifted2 = s.grid.shift(index, dir2, 1);

		// fields at same time:
		AlgebraElement E1 = s.grid.getE(indexShifted2, dir1);
		AlgebraElement E2 = s.grid.getE(indexShifted1, dir2);
		// time averaged B-field:
		AlgebraElement B1 = s.grid.getB(index, dir1, 0).add(s.grid.getB(index, dir1, 1)).mult(0.5);
		AlgebraElement B2 = s.grid.getB(index, dir2, 0).add(s.grid.getB(index, dir2, 1)).mult(0.5);
		double S = E1.mult(B2) - E2.mult(B1);
		return S / (as * g * as * g);
	}

	public double getPoyntingVector3(int index, int direction) {
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		// Indices for cross product:
		int dir1 = (direction + 1) % 3;
		int dir2 = (direction + 2) % 3;

		//int indexShifted1 = s.grid.shift(index, dir1, -1);
		//int indexShifted2 = s.grid.shift(index, dir2, -1);
		int indexShifted1 = index;
		int indexShifted2 = index;

		// fields at same time:
		AlgebraElement E1 = s.grid.getE(indexShifted2, dir1);
		AlgebraElement E2 = s.grid.getE(indexShifted1, dir2);

		// Get time-averaged E-field
		if (Ecurrent != null) {
			Ecurrent[indexShifted2][dir1] = E1.copy();
			if (Eold != null && Eold[indexShifted2] != null && Eold[indexShifted2][dir1] != null) {
				// Form average
				E1 = (E1.add(Eold[indexShifted2][dir1])).mult(0.5);
			}
			Ecurrent[indexShifted1][dir2] = E2.copy();
			if (Eold != null && Eold[indexShifted1] != null && Eold[indexShifted1][dir2] != null) {
				// Form average
				E2 = (E2.add(Eold[indexShifted1][dir2])).mult(0.5);
			}
		}

		// time averaged B-field:
//		AlgebraElement B1 = s.grid.getB(index, dir1, 0).add(s.grid.getB(index, dir1, 1)).mult(0.5);
//		AlgebraElement B2 = s.grid.getB(index, dir2, 0).add(s.grid.getB(index, dir2, 1)).mult(0.5);

		//indexShifted1 = s.grid.shift(index, dir1, 1);
		//indexShifted2 = s.grid.shift(index, dir2, 1);
		indexShifted1 = index;
		indexShifted2 = index;

		// B-field:
		AlgebraElement B1 = s.grid.getB(indexShifted2, dir1, 0);
		AlgebraElement B2 = s.grid.getB(indexShifted1, dir2, 0);
		double S = E1.mult(B2) - E2.mult(B1);
		return S / (as * g * as * g);
	}

	@Deprecated
	public double getCurrentElectricField(int index) {
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		double value = 0;

		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			AlgebraElement J = s.grid.getJ(index, direction);
			AlgebraElement E = s.grid.getE(index, direction);
			value += J.mult(E);
		}
		return value / (as * g * as * g);
	}

	/**
	 * Calculates current times the electric field at the time of the
	 * B-field through order O(t^2).
	 * @param index cell index
	 * @return current times electric field J*E
	 */
	public double getCurrentElectricField2(int index) {
		storeOldEJ = true;
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		double value = 0;

		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			AlgebraElement J = s.grid.getJ(index, direction);
			if (Jcurrent != null) {
				Jcurrent[index][direction] = J.copy();
				if (Jold != null && Jold[index] != null && Jold[index][direction] != null) {
					// Use previous value
					J = Jold[index][direction];
				}
			}
			AlgebraElement E = s.grid.getE(index, direction);
			if (Ecurrent != null) {
				Ecurrent[index][direction] = E.copy();
				if (Eold != null && Eold[index] != null && Eold[index][direction] != null) {
					// Form average
					E = (E.add(Eold[index][direction])).mult(0.5);
				}
			}
			value += J.mult(E);
		}
		return value / (as * g * as * g);
	}

	public double getTotalEnergyDensity() {
		double result = 0;
		int evaluatableCells = 0;
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			if (s.grid.isEvaluatable(i)) {
				result += getEnergyDensity2(i);
				evaluatableCells++;
			}
		}
		// TODO: Use "evaluatableCells" here and below, but only if it
		// is also used in physics.measurements.FieldMeasurements.
		//double norm = evaluatableCells;
		double norm = s.grid.getTotalNumberOfCells();
		return result / norm;
	}

	public double getTotalEnergyDensityDerivative() {
		double result = 0;
		int evaluatableCells = 0;
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			if (s.grid.isEvaluatable(i)) {
				result += getEnergyDensityDerivative(i);
				evaluatableCells++;
			}
		}
		//double norm = evaluatableCells;
		double norm = s.grid.getTotalNumberOfCells();
		return result / norm;
	}

	/**
	 *  Calculate DivS1 via div S
	 *  @return div S */
	public double getTotalDivS1() {
		if (!currentDivS1Calculated) {
			double result = 0;
			int evaluatableCells = 0;
			for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
				if (s.grid.isEvaluatable(i)) {
					result += getDivPoyntingVector4(i);
					evaluatableCells++;
				}
			}
			//double norm = evaluatableCells;
			double norm = s.grid.getTotalNumberOfCells();
			currentDivS1 = result / norm;
			integratedDivS1 += currentDivS1 * s.tstep;
			currentDivS1Calculated = true;
		}
		return currentDivS1;
	}

	/**
	 * Calculate DivS2 via B rot E - E rot B
	 * @return B rot E - E rot B
	 */
	public double getTotalDivS2() {
		if (!currentDivS2Calculated) {
			double result = 0;
			int evaluatableCells = 0;
			for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
				if (s.grid.isEvaluatable(i)) {
					result += getDivPoyntingVector3(i);
					evaluatableCells++;
				}
			}
			//double norm = evaluatableCells;
			double norm = s.grid.getTotalNumberOfCells();
			currentDivS2 = result / norm;
			integratedDivS2 += currentDivS2 * s.tstep;
			currentDivS2Calculated = true;
		}
		return currentDivS2;
	}

	public double getTotalJE() {
		if (!currentJECalculated) {
			double result = 0;
			int evaluatableCells = 0;
			for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
				if (s.grid.isEvaluatable(i)) {
					result += getCurrentElectricField2(i);
					evaluatableCells++;
				}
			}
			//double norm = evaluatableCells;
			double norm = s.grid.getTotalNumberOfCells();
			currentJE = result / norm;
			integratedJE += currentJE * s.tstep;
			currentJECalculated = true;
		}
		return currentJE;
	}

	public double getIntegratedTotalDivS1() {
		if (!currentDivS1Calculated) {
			getTotalDivS1();
		}
		return integratedDivS1;
	}

	public double getIntegratedTotalDivS2() {
		if (!currentDivS2Calculated) {
			getTotalDivS2();
		}
		return integratedDivS2;
	}

	public double getIntegratedTotalJE() {
		if (!currentJECalculated) {
			getTotalJE();
		}
		return integratedJE;
	}
}
