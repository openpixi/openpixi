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

	private enum CalculationAccuracy {
		NAIVE,
		SIMPLE,
		INTERPOLATED,
		YEE_ENERGY
	}
//	private CalculationAccuracy accuracy = CalculationAccuracy.NAIVE;
	private CalculationAccuracy accuracy = CalculationAccuracy.SIMPLE;
//	private CalculationAccuracy accuracy = CalculationAccuracy.INTERPOLATED;
//	private CalculationAccuracy accuracy = CalculationAccuracy.YEE_ENERGY;

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
	private AlgebraElement[][] EAveragecurrent;
	private AlgebraElement[][] EAverageold;
	private Double[][] Scurrent;
	private Double[][] Sold;
	private Double[] JEcurrent;
	private Double[] JEold;

	private double integratedDivS;
	private double integratedBrotEminusErotB;
	private double integratedJE;
	private boolean currentDivSCalculated;
	private boolean currentBrotEminusErotBCalculated;
	private boolean currentJECalculated;
	private double currentDivS;
	private double currentBrotEminusErotB;
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
		integratedDivS = 0;
		integratedBrotEminusErotB = 0;
		integratedJE = 0;
		currentDivSCalculated = false;
		currentBrotEminusErotBCalculated = false;
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

		currentDivSCalculated = false;
		currentBrotEminusErotBCalculated = false;
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
			currentEnergyDensity[i] = getEnergyDensity(i);
		}
	}

	private void resetEJ() {
		Ecurrent = null;
		Eold = null;
		Jcurrent = null;
		Jold = null;
		RotEcurrent = null;
		RotEold = null;
		EAveragecurrent = null;
		EAverageold = null;
		Scurrent = null;
		Sold = null;
		JEcurrent = null;
		JEold = null;
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
			// Move current values to old values
			Eold[i] = Ecurrent[i];
			Jold[i] = Jcurrent[i];
			RotEold[i] = RotEcurrent[i];

			// Reset new values
			Ecurrent[i] = new AlgebraElement[dimensions];
			Jcurrent[i] = new AlgebraElement[dimensions];
			RotEcurrent[i] = new AlgebraElement[dimensions];
			// Fields are copied when they are used
		}
		if (accuracy == CalculationAccuracy.INTERPOLATED) {
			// Additional fields are required:
			if (EAveragecurrent == null) {
				EAveragecurrent = new AlgebraElement[cells][];
				EAverageold = new AlgebraElement[cells][];
				Scurrent = new Double[cells][];
				Sold = new Double[cells][];
				JEcurrent = new Double[cells];
				JEold = new Double[cells];
			}
			for (int i = 0; i < cells; i++) {
				// Move current values to old values
				EAverageold[i] = EAveragecurrent[i];
				Sold[i] = Scurrent[i];

				// Reset new values
				EAveragecurrent[i] = new AlgebraElement[dimensions];
				Scurrent[i] = new Double[dimensions];
				// Fields are copied when they are used
			}
			JEold = JEcurrent;
			JEcurrent = new Double[cells];
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

	public double getEnergyDensity(int index) {
		switch (accuracy) {
		case NAIVE:
			return getEnergyDensity1(index);

		default:
		case SIMPLE:
			return getEnergyDensity2(index);

		case INTERPOLATED:
			return getEnergyDensity3(index);

		case YEE_ENERGY:
			return getEnergyDensity4(index);
		}
	}

	@Deprecated
	private double getEnergyDensity1(int index) {
		double value = 0;
		for (int w = 0; w < s.getNumberOfDimensions(); w++) {
			double unitFactor = Math.pow(s.grid.getLatticeUnitFactor(w), -2);
			value += s.grid.getEsquaredFromLinks(index, w) * unitFactor;
			// Time averaging for B field.
			value += 0.5 * s.grid.getBsquaredFromLinks(index, w, 0) * unitFactor;
			value += 0.5 * s.grid.getBsquaredFromLinks(index, w, 1) * unitFactor;
		};
		return value / 2;
	}

	/**
	 * Calculates the energy density at the time of the E-field in physical units.
	 * correctly through order O(t^2).
	 * @param index cell index
	 * @return energy density
	 */
	private double getEnergyDensity2(int index) {
		double value = 0;
		for (int w = 0; w < s.getNumberOfDimensions(); w++) {
			double unitFactor = Math.pow(s.grid.getLatticeUnitFactor(w), -2);
			value += s.grid.getE(index, w).square() * unitFactor;
			// Time averaging for B field.
			AlgebraElement B = s.grid.getB(index, w, 0);
			AlgebraElement Bnext = s.grid.getB(index, w, 1);
			value += (B.add(Bnext)).mult(0.5).square() * unitFactor;
		};
		return value / 2;
	}

	/**
	 * Calculates the energy density in physical units at the time of the E-field
	 * correctly through order O(x^2) and O(t^2) at the corner
	 * of the cell at time t = dt/2.
	 * @param index cell index
	 * @return energy density
	 */
	private double getEnergyDensity3(int index) {
		double value = 0;
		for (int w = 0; w < s.getNumberOfDimensions(); w++) {
			/*
			value += s.grid.getE(index, w).square();
			// Time averaging for B field.
			AlgebraElement B = s.grid.getB(index, w, 0);
			AlgebraElement Bnext = s.grid.getB(index, w, 1);
			value += (B.add(Bnext)).mult(0.5).square();
			 */
			double unitFactor = Math.pow(s.grid.getLatticeUnitFactor(w), -2);
			AlgebraElement E = getEHalfShifted(index, w, 0, 0, -1);
			AlgebraElement B = getBHalfShifted(index, w, 0, 0, -1);
			value += (E.square() + B.square()) * unitFactor;
		};
		return value / 2;
	}

	/**
	 * Get E at particular location within the lattice cell in lattice units.
	 * @param index Lattice index of the electric field
	 * @param direction Index of the component
	 * @param shiftDirection Direction in which the electric field is shifted by half a time step
	 * @param shiftOrientation Orientation of shift
	 * @param shiftTime Shift in time direction: -1: half time step in past; 0: current time.
	 * @return
	 */
	private AlgebraElement getEHalfShifted(int index, int direction, int shiftDirection, int shiftOrientation, int shiftTime) {
		if (shiftTime == -1 && shiftOrientation == 0) {
			// shift in time direction, average in spatial direction
			AlgebraElement E1 = s.grid.getE(index, direction);
			AlgebraElement E2 = getEShifted(index, direction, direction, -1);
			AlgebraElement Eaverage = (E1.add(E2)).mult(0.5);

			// Form the time average if available:
			if (EAveragecurrent != null) {
				EAveragecurrent[index][direction] = Eaverage.copy();
				if (EAverageold != null && EAverageold[index] != null && EAverageold[index][direction] != null) {
					// Form average
					Eaverage = (Eaverage.add(EAverageold[index][direction])).mult(0.5);
				}
			}
			return Eaverage;
		} else if (shiftTime == 0 && shiftOrientation != 0) {
			// Form average over 4 points:
			AlgebraElement E1 = s.grid.getE(index, direction);
			AlgebraElement E2 = getEShifted(index, direction, shiftDirection, shiftOrientation);
			AlgebraElement E3 = getEShifted(index, direction, direction, -1);
			// For the last points, there are 2 possible ways for parallel transport, so we form the average of both:
			AlgebraElement E4a = getEDoubleShifted(index, direction, shiftDirection, shiftOrientation, direction, -1);
			AlgebraElement E4b = getEDoubleShifted(index, direction, direction, -1, shiftDirection, shiftOrientation);
			AlgebraElement E4 = (E4a.add(E4b)).mult(0.5);

			AlgebraElement Eaverage = (((E1.add(E2)).add(E3)).add(E4)).mult(0.25);
			return Eaverage;
		} else {
			System.out.println("Error in PoyntingTheoremBuffer: Direction not specified yet");
			return null;
		}
	}

	/**
	 * Obtain a shifted and properly parallel transported E-vector component in lattice units.
	 * @param index
	 * @param direction
	 * @param shiftDirection
	 * @param shiftOrientation
	 * @return
	 */
	private AlgebraElement getEShifted(int index, int direction, int shiftDirection, int shiftOrientation) {
		int indexShifted = s.grid.shift(index, shiftDirection, shiftOrientation);
		AlgebraElement E = s.grid.getE(indexShifted, direction);
		E = E.act(s.grid.getLink(index, shiftDirection, shiftOrientation, 0));
		return E;
	}

	/**
	 * Obtain a shifted parallel transported E-vector along 2 path segments in lattice units.
	 * @param index
	 * @param direction
	 * @param shiftDirection1
	 * @param shiftOrientation1
	 * @param shiftDirection2
	 * @param shiftOrientation2
	 * @return
	 */
	private AlgebraElement getEDoubleShifted(int index, int direction, int shiftDirection1, int shiftOrientation1,
			int shiftDirection2, int shiftOrientation2) {
		int indexShifted1 = s.grid.shift(index, shiftDirection1, shiftOrientation1);
		int indexShifted12 = s.grid.shift(indexShifted1, shiftDirection2, shiftOrientation2);

		AlgebraElement E = s.grid.getE(indexShifted12, direction);

		// Parallel transport back along the path:
		E = E.act(s.grid.getLink(indexShifted1, shiftDirection2, shiftOrientation2, 0));
		E = E.act(s.grid.getLink(index, shiftDirection1, shiftOrientation1, 0));
		return E;
	}
	/**
	 * Get E at particular location within the lattice cell in lattice units.
	 * @param index Lattice index of the electric field
	 * @param direction Index of the component
	 * @param shiftDirection Direction in which the electric field is shifted by half a time step
	 * @param shiftOrientation Orientation of shift
	 * @param shiftTime Shift in time direction: -1: half time step in past; 0: current time.
	 * @return
	 */
	private AlgebraElement getBHalfShifted(int index, int direction, int shiftDirection, int shiftOrientation, int shiftTime) {
		if (shiftTime == -1 && shiftOrientation == 0) {
			// shift in time direction, average in spatial direction
			// Form average over 4 points:
			AlgebraElement B1 = s.grid.getB(index, direction, 0);
			AlgebraElement B2 = getBShifted(index, direction, shiftDirection, shiftOrientation, 0);
			AlgebraElement B3 = getBShifted(index, direction, direction, -1, 0);
			// For the last points, there are 2 possible ways for parallel transport, so we form the average of both:
			AlgebraElement B4a = getBDoubleShifted(index, direction, shiftDirection, shiftOrientation, direction, -1, 0);
			AlgebraElement B4b = getBDoubleShifted(index, direction, direction, -1, shiftDirection, shiftOrientation, 0);
			AlgebraElement B4 = (B4a.add(B4b)).mult(0.5);

			AlgebraElement Baverage = (((B1.add(B2)).add(B3)).add(B4)).mult(0.25);
			return Baverage;
		} else if (shiftTime == 0 && shiftOrientation != 0 && direction != shiftDirection) {
			// Orthogonal direction:
			int directionOrthogonal = (3 - direction - shiftDirection) % 3;

			// Average over next time slice
			AlgebraElement B1 = s.grid.getB(index, direction, 1);
			AlgebraElement B2 = getBShifted(index, direction, directionOrthogonal, -1, 1);

			// Average over previous time slice
			AlgebraElement B3 = s.grid.getB(index, direction, 0);
			AlgebraElement B4 = getBShifted(index, direction, directionOrthogonal, -1, 0);

			AlgebraElement Baverage = (((B1.add(B2)).add(B3)).add(B4)).mult(0.25);
			return Baverage;
		} else {
			System.out.println("Error in PoyntingTheoremBuffer: Direction not specified yet");
			return null;
		}
	}

	/**
	 * Obtain a shifted and properly parallel transported B-vector component in lattice units.
	 * @param index
	 * @param direction
	 * @param shiftDirection
	 * @param shiftOrientation
	 * @param timeIndex
	 * @return
	 */
	private AlgebraElement getBShifted(int index, int direction, int shiftDirection, int shiftOrientation, int timeIndex) {
		int indexShifted = s.grid.shift(index, shiftDirection, shiftOrientation);
		AlgebraElement B = s.grid.getB(indexShifted, direction, timeIndex);
		B = B.act(s.grid.getLink(index, shiftDirection, shiftOrientation, timeIndex));
		return B;
	}

	/**
	 * Obtain a shifted parallel transported E-vector along 2 path segments in lattice units.
	 * @param index
	 * @param direction
	 * @param shiftDirection1
	 * @param shiftOrientation1
	 * @param shiftDirection2
	 * @param shiftOrientation2
	 * @return
	 */
	private AlgebraElement getBDoubleShifted(int index, int direction, int shiftDirection1, int shiftOrientation1,
			int shiftDirection2, int shiftOrientation2, int timeIndex) {
		int indexShifted1 = s.grid.shift(index, shiftDirection1, shiftOrientation1);
		int indexShifted12 = s.grid.shift(indexShifted1, shiftDirection2, shiftOrientation2);

		AlgebraElement B = s.grid.getB(indexShifted12, direction, timeIndex);

		// Parallel transport back along the path:
		B = B.act(s.grid.getLink(indexShifted1, shiftDirection2, shiftOrientation2, timeIndex));
		B = B.act(s.grid.getLink(index, shiftDirection1, shiftOrientation1, timeIndex));
		return B;
	}

	/**
	 * Calculates the energy density as Yee energy in physical units
	 * E^2(t+dt/2) + B(t+dt)*B(t)
	 * of the cell at time t = dt/2.
	 * @param index cell index
	 * @return energy density
	 */
	private double getEnergyDensity4(int index) {
		double value = 0;
		// Lattice spacing and coupling constant
		for (int w = 0; w < s.getNumberOfDimensions(); w++) {
			double unitFactor = Math.pow(s.grid.getLatticeUnitFactor(w), -2);
			value += s.grid.getE(index, w).square() * unitFactor;
			// Geometric time averaging for B field.
			AlgebraElement B = s.grid.getB(index, w, 0);
			AlgebraElement Bnext = s.grid.getB(index, w, 1);
			value += B.mult(Bnext) * unitFactor;
		};
		return value / 2;
	}

	/**
	 * Calculates the derivative of the energy density in physical units
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

	public double getDivPoyntingVector(int index) {
		switch (accuracy) {
		case NAIVE:
			return getDivPoyntingVector1(index);

		default:
		case SIMPLE:
		case INTERPOLATED:
			return getDivPoyntingVector4(index);
		}
	}

	public double getBrotEminusErotB(int index) {
		switch (accuracy) {
		case NAIVE:
			return getBrotEminusErotB1(index);

		default:
		case SIMPLE:
		case INTERPOLATED:
			return getBrotEminusErotB2(index);
		}
	}

	@Deprecated
	private double getDivPoyntingVector1(int index) {
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
			value += (getPoyntingVector1(indexShifted1, direction)
					- getPoyntingVector1(indexShifted2, direction)) / s.grid.getLatticeSpacing(direction);
		}
		return value / 2;
	}

	@Deprecated
	private double getPoyntingVector1(int index, int direction) {
		// Indices for cross product:
		int dir1 = (direction + 1) % 3;
		int dir2 = (direction + 2) % 3;
		double unitFactor1 = 1.0 / s.grid.getLatticeUnitFactor(dir1);
		double unitFactor2 = 1.0 / s.grid.getLatticeUnitFactor(dir2);

		// fields at same time:
		AlgebraElement E1 = s.grid.getE(index, dir1).mult(unitFactor1);
		AlgebraElement E2 = s.grid.getE(index, dir2).mult(unitFactor2);
		// time averaged B-field:
		AlgebraElement B1 = s.grid.getB(index, dir1, 0).add(s.grid.getB(index, dir1, 1)).mult(0.5).mult(unitFactor1);
		AlgebraElement B2 = s.grid.getB(index, dir2, 0).add(s.grid.getB(index, dir2, 1)).mult(0.5).mult(unitFactor2);
		double S = E1.mult(B2) - E2.mult(B1);
		return S;
	}

	/**
	 * Calculates the divergence of the Poynting vector on the lattice in physical units.
	 * Actually, B rot E - E rot B is calculated.
	 * @param index cell index
	 * @return B rot E - E rot B
	 */
	@Deprecated
	private double getBrotEminusErotB1(int index) {
		storeOldEJ = true;

		double value = 0;
		if (s.getNumberOfDimensions() != 3) {
			throw new RuntimeException("Dimension other than 3 has not been implemented yet.");
			// TODO: Implement for arbitrary dimensions
			// return 0;
		}
		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			double unitFactor = 1.0 / s.grid.getLatticeUnitFactor(direction);
			AlgebraElement rotE = s.grid.getRotE(index, direction);
			AlgebraElement rotB0 = s.grid.getRotB(index, direction, 0);
			AlgebraElement rotB1 = s.grid.getRotB(index, direction, 1);

			AlgebraElement E = s.grid.getE(index, direction).mult(unitFactor);
			// time averaged B-fields:
			AlgebraElement B = (s.grid.getB(index, direction, 0).add(s.grid.getB(index, direction, 1))).mult(0.5 * unitFactor);
			AlgebraElement rotB = (rotB0.add(rotB1)).mult(0.5);

			value += B.mult(rotE) - E.mult(rotB);
		}
		return value;
	}

	/**
	 * Calculates the divergence of the Poynting vector on the lattice in physical units
	 * at the time of the B-field correctly through order O(t^2).
	 * Actually, B rot E - E rot B is calculated.
	 * @param index cell index
	 * @return B rot E - E rot B
	 */
	private double getBrotEminusErotB2(int index) {
		storeOldEJ = true;

		double value = 0;
		if (s.getNumberOfDimensions() != 3) {
			throw new RuntimeException("Dimension other than 3 has not been implemented yet.");
			// TODO: Implement for arbitrary dimensions
			// return 0;
		}
		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			double unitFactor = 1.0 / s.grid.getLatticeUnitFactor(direction);
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
			E = E.mult(unitFactor);

			AlgebraElement rotB = s.grid.getRotB(index, direction, 0);
			AlgebraElement B = s.grid.getB(index, direction, 0).mult(unitFactor);

			value += B.mult(rotE) - E.mult(rotB);
		}
		return value;
	}

	/**
	 * Calculates the divergence of the Poynting vector on the lattice in physical units
	 * at the time of the B-field correctly through order O(t^2).
	 * @param index cell index
	 * @return divergence of Poynting vector
	 */
	private double getDivPoyntingVector4(int index) {
		double value = 0;
		if (s.getNumberOfDimensions() != 3) {
			throw new RuntimeException("Dimension other than 3 has not been implemented yet.");
			// TODO: Implement for arbitrary dimensions
			// return 0;
		}
		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			int indexShifted1 = s.grid.shift(index, direction, -1);
			switch (accuracy) {
			case NAIVE:
				value += (getPoyntingVector2(index, direction)
								 - getPoyntingVector2(indexShifted1, direction)) / s.grid.getLatticeSpacing(direction);
				break;

			default:
			case SIMPLE:
				value += (getPoyntingVector3(index, direction)
								 - getPoyntingVector3(indexShifted1, direction)) / s.grid.getLatticeSpacing(direction);
				break;

			case INTERPOLATED:
				value += (getPoyntingVector4(index, direction)
								 - getPoyntingVector4(indexShifted1, direction)) / s.grid.getLatticeSpacing(direction);
				break;
			}
		}
		return value;
	}

	@Deprecated
	private double getPoyntingVector2(int index, int direction) {
		// Indices for cross product:
		int dir1 = (direction + 1) % 3;
		int dir2 = (direction + 2) % 3;
		double unitFactor1 = 1.0 / s.grid.getLatticeUnitFactor(dir1);
		double unitFactor2 = 1.0 / s.grid.getLatticeUnitFactor(dir2);

		int indexShifted1 = s.grid.shift(index, dir1, 1);
		int indexShifted2 = s.grid.shift(index, dir2, 1);

		// fields at same time:
		AlgebraElement E1 = s.grid.getE(indexShifted2, dir1).mult(unitFactor1);
		AlgebraElement E2 = s.grid.getE(indexShifted1, dir2).mult(unitFactor2);
		// time averaged B-field:
		AlgebraElement B1 = s.grid.getB(index, dir1, 0).add(s.grid.getB(index, dir1, 1)).mult(unitFactor1 * 0.5);
		AlgebraElement B2 = s.grid.getB(index, dir2, 0).add(s.grid.getB(index, dir2, 1)).mult(unitFactor2 * 0.5);
		double S = E1.mult(B2) - E2.mult(B1);
		return S;
	}

	private double getPoyntingVector3(int index, int direction) {
		// Indices for cross product:
		int dir1 = (direction + 1) % 3;
		int dir2 = (direction + 2) % 3;
		double unitFactor1 = 1.0 / s.grid.getLatticeUnitFactor(dir1);
		double unitFactor2 = 1.0 / s.grid.getLatticeUnitFactor(dir2);

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

		E1 = E1.mult(unitFactor1);
		E2 = E2.mult(unitFactor2);

		// time averaged B-field:
//		AlgebraElement B1 = s.grid.getB(index, dir1, 0).add(s.grid.getB(index, dir1, 1)).mult(0.5);
//		AlgebraElement B2 = s.grid.getB(index, dir2, 0).add(s.grid.getB(index, dir2, 1)).mult(0.5);

		//indexShifted1 = s.grid.shift(index, dir1, 1);
		//indexShifted2 = s.grid.shift(index, dir2, 1);
		indexShifted1 = index;
		indexShifted2 = index;

		// B-field:
		AlgebraElement B1 = s.grid.getB(indexShifted2, dir1, 0).mult(unitFactor1);
		AlgebraElement B2 = s.grid.getB(indexShifted1, dir2, 0).mult(unitFactor2);
		double S = E1.mult(B2) - E2.mult(B1);
		return S;
	}

	private double getPoyntingVector4(int index, int direction) {
		// Indices for cross product:
		// direction -> x
		int dir1 = (direction + 1) % 3; // dir1 -> y
		int dir2 = (direction + 2) % 3; // dir2 -> z
		double unitFactor1 = 1.0 / s.grid.getLatticeUnitFactor(dir1);
		double unitFactor2 = 1.0 / s.grid.getLatticeUnitFactor(dir2);

		// fields at same time:
		AlgebraElement E1 = getEHalfShifted(index, dir1, direction, 1, 0).mult(unitFactor1);
		AlgebraElement E2 = getEHalfShifted(index, dir2, direction, 1, 0).mult(unitFactor2);

		// B-field:
		AlgebraElement B1 = getBHalfShifted(index, dir1, direction, 1, 0).mult(unitFactor1);
		AlgebraElement B2 = getBHalfShifted(index, dir2, direction, 1, 0).mult(unitFactor2);
		double S = E1.mult(B2) - E2.mult(B1);

		// Return the Poynting vector of the previous time step if available:
		if (Scurrent != null) {
			// Store value of current time step
			Scurrent[index][direction] = S;
			if (Sold != null && Sold[index] != null && Sold[index][direction] != null) {
				// Use value of previous time step
				S = Sold[index][direction];
			}
		}
		return S;
	}

	public double getCurrentElectricField(int index) {
		switch (accuracy) {
		case NAIVE:
			return getCurrentElectricField1(index);

		default:
		case SIMPLE:
			return getCurrentElectricField2(index);

		case INTERPOLATED:
			return getCurrentElectricField3(index);
		}
	}

	@Deprecated
	public double getCurrentElectricField1(int index) {
		double value = 0;

		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			double unitFactor = s.grid.getLatticeUnitFactor(direction);
			AlgebraElement J = s.grid.getJ(index, direction);
			AlgebraElement E = s.grid.getE(index, direction).mult(unitFactor);
			value += J.mult(E);
		}
		return value;
	}

	/**
	 * Calculates current times the electric field at the time of the
	 * B-field through order O(t^2) in physical units.
	 * @param index cell index
	 * @return current times electric field J*E
	 */
	private double getCurrentElectricField2(int index) {
		storeOldEJ = true;

		double value = 0;

		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			double unitFactor = s.grid.getLatticeUnitFactor(direction);
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
			E = E.mult(unitFactor);
			value += J.mult(E);
		}
		return value;
	}

	/**
	 * Calculates current times the electric field at the time of the
	 * E-field at the origin through order O(t^2) in physical units.
	 * @param index cell index
	 * @return current times electric field J*E
	 */
	private double getCurrentElectricField3(int index) {
		storeOldEJ = true;

		double value = 0;

		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			double unitFactor = s.grid.getLatticeUnitFactor(direction);
			AlgebraElement J1 = s.grid.getJ(index, direction);
			AlgebraElement J2 = getJShifted(index, direction, direction, -1);
			AlgebraElement J = (J1.add(J2)).mult(0.5);

			if (Jcurrent != null) {
				Jcurrent[index][direction] = J1.copy();
				if (Jold != null && Jold[index] != null && Jold[index][direction] != null) {
					// Use previous value
					AlgebraElement J3 = Jold[index][direction];
					AlgebraElement J4 = getJOldShifted(index, direction, direction, -1);
					AlgebraElement J34 = (J3.add(J4)).mult(0.5);
					J = (J.add(J34)).mult(0.5);
				}
			}

			AlgebraElement E1 = s.grid.getE(index, direction);
			AlgebraElement E2 = getEShifted(index, direction, direction, -1);
			AlgebraElement E = (E1.add(E2)).mult(0.5);

			value += J.mult(E.mult(unitFactor));
		}

		// Return the J*E of the previous time step if available:
		if (JEcurrent != null) {
			// Store value of current time step
			JEcurrent[index] = value;
			if (JEold != null && JEold[index] != null) {
				// Use value of previous time step
				value = JEold[index];
			}
		}

		return value;
	}

	/**
	 * Obtain a shifted and properly parallel transported J-vector component in physical units.
	 * @param index
	 * @param direction
	 * @param shiftDirection
	 * @param shiftOrientation
	 * @return
	 */
	private AlgebraElement getJShifted(int index, int direction, int shiftDirection, int shiftOrientation) {
		int indexShifted = s.grid.shift(index, shiftDirection, shiftOrientation);
		AlgebraElement J = s.grid.getJ(indexShifted, direction);
		J = J.act(s.grid.getLink(index, shiftDirection, shiftOrientation, 1));
		return J;
	}

	/**
	 * Obtain a shifted and properly parallel transported J-vector component in physical units.
	 * @param index
	 * @param direction
	 * @param shiftDirection
	 * @param shiftOrientation
	 * @return
	 */
	private AlgebraElement getJOldShifted(int index, int direction, int shiftDirection, int shiftOrientation) {
		int indexShifted = s.grid.shift(index, shiftDirection, shiftOrientation);
		AlgebraElement J;
		if (Jold != null && Jold[index] != null && Jold[index][direction] != null) {
			// Use previous value
			J = Jold[index][direction];
		} else {
			J = s.grid.getJ(indexShifted, direction);
		}
		J = J.act(s.grid.getLink(index, shiftDirection, shiftOrientation, 0));
		return J;
	}

	public double getTotalEnergyDensity() {
		double result = 0;
		int evaluatableCells = 0;
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			if (s.grid.isEvaluatable(i)) {
				result += getEnergyDensity(i);
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
	 *  Calculate div S in physical units
	 *  @return div S */
	public double getTotalDivS() {
		if (!currentDivSCalculated) {
			double result = 0;
			int evaluatableCells = 0;
			for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
				if (s.grid.isEvaluatable(i)) {
					result += getDivPoyntingVector(i);
					evaluatableCells++;
				}
			}
			//double norm = evaluatableCells;
			double norm = s.grid.getTotalNumberOfCells();
			currentDivS = result / norm;
			integratedDivS += currentDivS * s.tstep;
			currentDivSCalculated = true;
		}
		return currentDivS;
	}

	/**
	 * Calculate B rot E - E rot B in physical units
	 * @return B rot E - E rot B
	 */
	public double getTotalBrotEminusErotB() {
		if (!currentBrotEminusErotBCalculated) {
			double result = 0;
			int evaluatableCells = 0;
			for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
				if (s.grid.isEvaluatable(i)) {
					result += getBrotEminusErotB(i);
					evaluatableCells++;
				}
			}
			//double norm = evaluatableCells;
			double norm = s.grid.getTotalNumberOfCells();
			currentBrotEminusErotB = result / norm;
			integratedBrotEminusErotB += currentBrotEminusErotB * s.tstep;
			currentBrotEminusErotBCalculated = true;
		}
		return currentBrotEminusErotB;
	}

	public double getTotalJE() {
		if (!currentJECalculated) {
			double result = 0;
			int evaluatableCells = 0;
			for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
				if (s.grid.isEvaluatable(i)) {
					result += getCurrentElectricField(i);
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

	public double getIntegratedTotalDivS() {
		if (!currentDivSCalculated) {
			getTotalDivS();
		}
		return integratedDivS;
	}

	public double getIntegratedTotalBrotEminusErotB() {
		if (!currentBrotEminusErotBCalculated) {
			getTotalBrotEminusErotB();
		}
		return integratedBrotEminusErotB;
	}

	public double getIntegratedTotalJE() {
		if (!currentJECalculated) {
			getTotalJE();
		}
		return integratedJE;
	}
}
