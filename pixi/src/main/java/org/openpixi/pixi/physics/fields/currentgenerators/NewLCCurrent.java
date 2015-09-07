package org.openpixi.pixi.physics.fields.currentgenerators;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.fields.NewLCPoissonSolver;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.ArrayList;

/**
 * Created by dmueller on 9/1/15.
 */
public class NewLCCurrent implements ICurrentGenerator {

	private int direction;
	private int orientation;
	private double location;
	private double longitudinalWidth;

	private ArrayList<PointCharge> charges;
	private int[] transversalNumCells;
	private AlgebraElement[] transversalChargeDensity;
	private int totalTransversalCells;

	private int numberOfColors;
	private int numberOfComponents;
	private double as;
	private double at;
	private double g;

	private int[] numCells;

	public NewLCCurrent(int direction, int orientation, double location, double longitudinalWidth){
		this.direction = direction;
		this.orientation = orientation;
		this.location = location;
		this.longitudinalWidth = longitudinalWidth;

		this.charges = new ArrayList<PointCharge>();
	}

	public void addCharge(double[] location, double[] colorDirection, double magnitude) {
		// This method should be called from the YAML object to add the charges for the current generator.
		this.charges.add(new PointCharge(location, colorDirection, magnitude));
	}

	public void initializeCurrent(Simulation s, int totalInstances) {
		// 0) Define some variables.
		numberOfColors = s.getNumberOfColors();
		numberOfComponents = numberOfColors * numberOfColors - 1;
		as = s.grid.getLatticeSpacing();
		at = s.getTimeStep();
		g = s.getCouplingConstant();

		// 1) Initialize transversal charge density grid using the charges array.
		numCells = s.grid.getNumCells();
		transversalNumCells = GridFunctions.reduceGridPos(numCells, direction);
		totalTransversalCells = GridFunctions.getTotalNumberOfCells(transversalNumCells);
		transversalChargeDensity = new AlgebraElement[totalTransversalCells];
		for (int i = 0; i < totalTransversalCells; i++) {
			transversalChargeDensity[i] = s.grid.getElementFactory().algebraZero();
		}

		// Iterate over (point) charges, round them to the nearest grid point and add them to the transversal charge density.
		for (int i = 0; i < charges.size(); i++) {
			PointCharge c = charges.get(i);
			AlgebraElement chargeAmplitude = s.grid.getElementFactory().algebraZero(s.getNumberOfColors());
			for (int j = 0; j < numberOfComponents; j++) {
				chargeAmplitude.set(j, c.colorDirection[j] * c.magnitude / Math.pow(as, s.getNumberOfDimensions() - 1));
			}
			transversalChargeDensity[GridFunctions.getCellIndex(GridFunctions.nearestGridPoint(c.location, as), transversalNumCells)].addAssign(chargeAmplitude);
		}

		// 2) Interpolate grid charge and current density.
		applyCurrent(s);

		// 3) Initialize the NewLightConePoissonSolver with the transversal charge density and solve for the fields U and E.
		NewLCPoissonSolver poissonSolver = new NewLCPoissonSolver(direction, orientation, location, longitudinalWidth,
				transversalChargeDensity, transversalNumCells);
		poissonSolver.initialize(s);
		poissonSolver.solve(s);

		// You're done: charge density, current density and the fields are set up correctly.
	}

	public void applyCurrent(Simulation s) {
		int maxDirection = numCells[direction];
		double t = s.totalSimulationTime;

		AlgebraElement[] lastCurrents = new AlgebraElement[totalTransversalCells];
		for (int i = 0; i < totalTransversalCells; i++) {
			lastCurrents[i] =s.grid.getElementFactory().algebraZero();
		}
		for (int i = 0; i < maxDirection; i++) {
			double z = i * as - location;

			double s0 = g * as * shapeFunction(z, t - at, orientation, longitudinalWidth);  // shape at t times g*as
			double s1 = g * as * shapeFunction(z, t, orientation, longitudinalWidth);  // shape at t+dt times g*as
			double s2 = g * as * shapeFunction(z, t - at/2, orientation, longitudinalWidth);  // shape at t+dt times g*as
			double ds = (s1 - s0)/at; // time derivative of the shape function

			for (int j = 0; j < totalTransversalCells; j++) {
				int[] transversalGridPos = GridFunctions.getCellPos(j, transversalNumCells);
				int[] gridPos = GridFunctions.insertGridPos(transversalGridPos, direction, i);
				int cellIndex = s.grid.getCellIndex(gridPos);

				// a) Interpolate transversal charge density to grid charge density with a Gauss profile (at t).
				s.grid.addRho(cellIndex, transversalChargeDensity[j].mult(s1));

				// b) Compute gird current density in a charge conserving manner at (t-dt/2).

				// Method A: Sampling the analytical result on the grid (not charge conserving)
				//s.grid.addJ(cellIndex, direction, transversalChargeDensity[j].mult(s2*orientation));

				// Method B: Setting the current according to the continuity equation (charge conserving)
				if(Math.abs(ds * as) > 0.00000001) {
					lastCurrents[j].addAssign(transversalChargeDensity[j].mult(-ds * as));
					s.grid.addJ(cellIndex, direction,
							lastCurrents[j]
					);
				}



			}
		}
	}

	private double shapeFunction(double z, double t, int o, double width) {
		Gaussian gauss = new Gaussian(0.0, width);
		return gauss.value(z - o * t);
	}

	class PointCharge {
		public double[] location;
		public double[] colorDirection;
		double magnitude;

		public PointCharge(double[] location, double[] colorDirection, double magnitude) {
			this.location = location;
			this.colorDirection = normalize(colorDirection);
			this.magnitude = magnitude;
		}

		private double[] normalize(double[] v) {
			double norm = 0.0;
			for (int i = 0; i < v.length; i++) {
				norm += v[i] * v[i];
			}
			norm = Math.sqrt(norm);
			double[] result = new double[v.length];
			for (int i = 0; i < v.length; i++) {
				result[i] = v[i] / norm;
			}
			return result;
		}
	}
}
