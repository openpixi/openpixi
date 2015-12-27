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
 *
 */
public class PoyntingTheoremBuffer implements Diagnostics {

	Simulation s;
	private double[] oldEnergyDensity;
	private int[] oldTime;
	private double[] currentEnergyDensity;
	private int[] currentTime;

	PoyntingTheoremBuffer(Simulation s) {
		this.s = s;
	}

	@Override
	public void initialize(Simulation s) {
		System.out.println("initialize");
		this.s = s;
		// Initialize arrays
		int cells = s.grid.getTotalNumberOfCells();
		oldEnergyDensity = new double[cells];
		oldTime = new int[cells];
		currentEnergyDensity = new double[cells];
		currentTime = new int[cells];
	}

	@Override
	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps)
			throws IOException {
		// TODO Auto-generated method stub
		System.out.println("calculate");

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

	public double getEnergyDensityDerivative(int index) {
		double value = 0;
		if (currentTime == null) {
			// Buffer has not been initialized yet
			return 0;
		}
		if (currentTime[index] > s.totalSimulationSteps) {
			// Reset values (in case of a simulation reset)
			oldEnergyDensity[index] = 0;
			oldTime[index] = 0;
			currentEnergyDensity[index] = getEnergyDensity(index);
			currentTime[index] = s.totalSimulationSteps;
		}
		if (currentTime[index] < s.totalSimulationSteps) {
			// Update values
			oldEnergyDensity[index] = currentEnergyDensity[index];
			oldTime[index] = currentTime[index];
			currentEnergyDensity[index] = getEnergyDensity(index);
			currentTime[index] = s.totalSimulationSteps;
		}
		if (oldTime[index] != 0) {
			// Calculate derivative
			double deltaTime = (currentTime[index] - oldTime[index]) * s.tstep;
			value = (currentEnergyDensity[index] - oldEnergyDensity[index]) / deltaTime;
		}
		return value;
	}

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

	public double getDivPoyntingVector2(int index) {
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		double value = 0;
		if (s.getNumberOfDimensions() != 3) {
			throw new RuntimeException("Dimension other than 3 has not been implemented yet.");
			// TODO: Implement for arbitrary dimensions
			// return 0;
		}
		if (!s.grid.isRotBEvaluatable(index) || !s.grid.isRotBEvaluatable(index)) {
			// One of the neighbouring cells is not evaluatable.
			return 0;
		}
		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			AlgebraElement rotE = s.grid.getRotE(index, direction);
			AlgebraElement rotB0 = s.grid.getRotB(index, direction, 0);
			AlgebraElement rotB1 = s.grid.getRotB(index, direction, 1);

			AlgebraElement E = s.grid.getE(index, direction);
			// time averaged B-fields:
			AlgebraElement B = (s.grid.getB(index, direction, 0).add(s.grid.getB(index, direction, 0))).mult(0.5);
			AlgebraElement rotB = (rotB0.add(rotB1)).mult(0.5);

			value += B.mult(rotE) - E.mult(rotB);
		}
		return value / (as * g * as * g);
	}

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

}
