package org.openpixi.pixi.physics.fields.currentgenerators;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;

import java.util.ArrayList;

/**
 * Created by dmueller on 9/1/15.
 */
public class NewLightConeCurrent implements ICurrentGenerator {

	private int direction;
	private int orientation;
	private int surfaceIndex;
	private double longitudinalWidth;

	private ArrayList<PointCharge> charges;
	private int[] transversalNumCells;
	private AlgebraElement[] transversalChargeDensity;

	private int numberOfColors;
	private int numberOfComponents;
	private double as;

	private int[] numCells;

	public NewLightConeCurrent(int direction, int orientation, int surfaceIndex, double longitudinalWidth){
		this.direction = direction;
		this.orientation = orientation;
		this.surfaceIndex = surfaceIndex;
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

		// 1) Initialize transversal charge density grid using the charges array.
		numCells = s.grid.getNumCells();
		transversalNumCells = getEffectiveNumCells(numCells);
		int size = getTotalNumberOfCells(transversalNumCells);
		transversalChargeDensity = new AlgebraElement[size];

		// Iterate over (point) charges, round them to the nearest grid point and add them to the transversal charge density.
		for (int i = 0; i < charges.size(); i++) {
			PointCharge c = charges.get(i);
			AlgebraElement chargeAmplitude = s.grid.getElementFactory().algebraZero(s.getNumberOfColors());
			for (int j = 0; j < numberOfComponents; j++) {
				chargeAmplitude.set(j, c.colorDirection[j] * c.magnitude);
			}
			transversalChargeDensity[getCellIndex(roundGridPos(c.location, as), transversalNumCells)] = chargeAmplitude;
		}

		// 2) Interpolate grid charge and current density.
		applyCurrent(s);

		// 3) Initialize the NewLightConePoissonSolver with the transversal charge density and solve for the fields U and E.

		// You're done: charge density, current density and the fields are set up correctly.
	}

	public void applyCurrent(Simulation s) {
		// a) Interpolate transversal charge density to grid charge density with a Gauss profile.

		// b) Compute gird current density in a charge conserving manner.
	}

	/*
		Utility methods, mainly used for the transversal grid.
	 */

	private int getEffectiveNumberOfDimensions(int[] numCells) {
		int count = 0;
		for (int i = 0; i < numCells.length; i++) {
			if(numCells[i] > 1) {
				count++;
			}
		}
		return count;
	}

	private int[] getEffectiveNumCells(int[] numCells) {
		int effDim = getEffectiveNumberOfDimensions(numCells);
		int[] effNumCells = new int[effDim];
		int count = 0;
		for (int i = 0; i < numCells.length; i++) {
			if(numCells[i] > 1) {
				effNumCells[count] = numCells[i];
				count++;
			}
		}
		return effNumCells;
	}

	private int getTotalNumberOfCells(int[] numCells) {
		int count = 1;
		for (int i = 0; i < numCells.length; i++) {
			count *= numCells[i];
		}
		return count;
	}

	private int[] roundGridPos(double[] pos, double as) {
		int[] roundedGridPosition = new int[pos.length];
		for (int i = 0; i < pos.length; i++) {
			roundedGridPosition[i] = (int) Math.rint(pos[i] / as);
		}
		return roundedGridPosition;
	}

	private int getCellIndex(int[] coordinates, int[] numCells) {
		int cellIndex;
		// Make periodic
		int[] periodicCoordinates = new int[coordinates.length];
		System.arraycopy(coordinates, 0, periodicCoordinates, 0, coordinates.length);
		for (int i = 0; i < numCells.length; i++) {
			periodicCoordinates[i] = (coordinates[i] + numCells[i]) % numCells[i];
		}
		// Compute cell index
		cellIndex = periodicCoordinates[0];
		for (int i = 0; i < coordinates.length; i++) {
			cellIndex *= numCells[i];
			cellIndex += periodicCoordinates[i];
		}
		return cellIndex;
	}

	class PointCharge {
		public double[] location;
		public double[] colorDirection;
		double magnitude;

		public PointCharge(double[] location, double[] colorDirection, double magnitude) {
			this.location = location;
			this.colorDirection = colorDirection;
			this.magnitude = magnitude;
		}
	}
}
