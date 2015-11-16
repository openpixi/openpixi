package org.openpixi.pixi.physics.fields.currentgenerators;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.ArrayList;

/**
 * A simple current generator for point-like charges based on ParticleLCCurrent.
 */
public class PointChargeLCCurrent implements ICurrentGenerator {

	/**
	 * Direction of movement of the charge density. Values range from 0 to numberOfDimensions-1.
	 */
	private int direction;

	/**
	 * Orientation of movement. Values are -1 or 1.
	 */
	private int orientation;

	/**
	 * Longitudinal location of the initial charge density in the simulation box.
	 */
	private double location;

	/**
	 * Longitudinal width of the charge density.
	 */
	private double longitudinalWidth;

	/**
	 * Option whether to remove the monopole moment or not.
	 */
	private boolean useMonopoleRemoval;

	/**
	 * Option whether to remove the dipole moment or not.
	 */
	private boolean useDipoleRemoval;

	/**
	 * List of point charges to use as intial conditions.
	 */
	private ArrayList<PointCharge> charges;

	/**
	 * Array containing the size of the transversal grid.
	 */
	private int[] transversalNumCells;

	/**
	 * Transversal charge density.
	 */
	private AlgebraElement[] transversalChargeDensity;

	/**
	 * Total number of cells in the transversal grid.
	 */
	private int totalTransversalCells;

	/**
	 * Number of colors used in the simulation.
	 */
	private int numberOfColors;

	/**
	 * Number of components associated with the number of colors Nc. For Nc > 1 it is Nc^2-1.
	 */
	private int numberOfComponents;

	/**
	 * Lattice spacing of the grid.
	 */
	private double as;

	/**
	 * Time step used in the simulation.
	 */
	private double at;

	/**
	 * Coupling constant used in the simulation.
	 */
	private double g;


	/**
	 * ParticleLCCurrent which is called to interpolate charges and currents.
	 */
	private ParticleLCCurrent particleLCCurrent;

	/**
	 * Standard constructor.
	 *
	 * @param direction
	 * @param orientation
	 * @param location
	 * @param longitudinalWidth
	 */
	public PointChargeLCCurrent(int direction, int orientation, double location, double longitudinalWidth, boolean useMonopoleRemoval, boolean useDipoleRemoval) {
		this.direction = direction;
		this.orientation = orientation;
		this.location = location;
		this.longitudinalWidth = longitudinalWidth;
		this.useMonopoleRemoval = useMonopoleRemoval;
		this.useDipoleRemoval = useDipoleRemoval;

		this.charges = new ArrayList<PointCharge>();
		this.particleLCCurrent = new ParticleLCCurrent(direction, orientation, location, longitudinalWidth);
	}

	/**
	 * Adds a point charge at a certain position. This is mainly used to specify initial conditions.
	 *
	 * @param location
	 * @param colorDirection
	 * @param magnitude
	 */
	public void addCharge(double[] location, double[] colorDirection, double magnitude) {
		// This method should be called from the YAML object to add the charges for the current generator.
		this.charges.add(new PointCharge(location, colorDirection, magnitude));
	}

	public void initializeCurrent(Simulation s, int dummy) {
		// 0) Define some variables.
		numberOfColors = s.getNumberOfColors();
		numberOfComponents = s.grid.getElementFactory().numberOfComponents;
		as = s.grid.getLatticeSpacing();
		at = s.getTimeStep();
		g = s.getCouplingConstant();

		// 1) Initialize transversal charge density grid using the charges array.
		transversalNumCells = GridFunctions.reduceGridPos(s.grid.getNumCells(), direction);
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
			int[] gridPos = GridFunctions.nearestGridPoint(c.location, as);
			interpolateChargeToGridCIC(s, c.location, chargeAmplitude);
		}

		if(useMonopoleRemoval) {
			removeMonopoleMoment(s);
		}

		if(useDipoleRemoval) {
			removeDipoleMoment(s);
		}

		particleLCCurrent.setTransversalChargeDensity(transversalChargeDensity);
		particleLCCurrent.initializeCurrent(s, dummy);
	}


	/**
	 *
	 * @param s
	 */
	public void applyCurrent(Simulation s) {
		particleLCCurrent.applyCurrent(s);
	}


	/**
	 * Interpolates a point charge to the transversal grid. The charge is smeared according to the cloud-in-cell shape.
	 *
	 * @param s
	 * @param chargePosition
	 * @param charge
	 */
	private void interpolateChargeToGridCIC(Simulation s, double[] chargePosition, AlgebraElement charge) {

		int effNumberOfDimensions = transversalNumCells.length;

		// Add all relevant points of the hypercube defining the cell.
		ArrayList<int[]> listOfPoints = new ArrayList<int[]>();
		int[] gridPos0 = GridFunctions.flooredGridPoint(chargePosition, as);
		listOfPoints.add(gridPos0);
		for (int i = 0; i < effNumberOfDimensions; i++) {
			ArrayList<int[]> newPoints = new ArrayList<int[]>();
			for (int j = 0; j < listOfPoints.size(); j++) {
				int[] currentPoint = listOfPoints.get(j).clone();
				currentPoint[i] += 1;
				newPoints.add(currentPoint);
			}
			listOfPoints.addAll(newPoints);
		}

		// Interpolate to each grid point of the hypercube.
		for(int[] p : listOfPoints) {
			double weight = 1.0;
			for (int i = 0; i < effNumberOfDimensions; i++) {
				double dist = 1.0 - Math.abs(p[i] - chargePosition[i] / as);
				weight *= dist;
			}
			transversalChargeDensity[GridFunctions.getCellIndex(p, transversalNumCells)].addAssign(charge.mult(weight));
		}
	}

	/**
	 * Interpolates a point charge to the transversal grid. The charge is "smeared" (not really) according to the nearest-grid-point method.
	 *
	 * @param s
	 * @param chargePosition
	 * @param charge
	 */
	private void interpolateChargeToGridNGP(Simulation s, double[] chargePosition, AlgebraElement charge) {
		int[] gridPos0 = GridFunctions.nearestGridPoint(chargePosition, as);
		transversalChargeDensity[GridFunctions.getCellIndex(gridPos0, transversalNumCells)].addAssign(charge);
	}

	/**
	 * Removes the monopole moment by subtracting a constant charge at each lattice site of the transversal charge density.
	 * This is not a good way to do this, so make sure the initial conditions are colorless at initialization.
	 *
	 * @param s
	 */
	private void removeMonopoleMoment(Simulation s) {
		AlgebraElement totalCharge = computeTotalCharge(s);
		totalCharge  = totalCharge.mult(1.0 / totalTransversalCells);
		for (int i = 0; i < totalTransversalCells; i++) {
			transversalChargeDensity[i].sub(totalCharge);
		}
	}

	/**
	 * Removes the dipole moment by adding dipoles for each color component. These dipoles cancel the total dipole moment.
	 *
	 * @param s
	 */
	private void removeDipoleMoment(Simulation s) {
		for (int c = 0; c < numberOfComponents; c++) {
			// Position of dipole
			double[] centerOfAbsCharge = computeCenterOfAbsCharge(c);
			// Distance of dipole charges
			double averageDist = computeAverageDistance(s, c);

			double dipoleCharge = 0.0;
			double[] dipoleVector = new double[transversalNumCells.length];
			for (int i = 0; i < totalTransversalCells; i++) {
				int[] gridPos = GridFunctions.getCellPos(i, transversalNumCells);
				double charge = transversalChargeDensity[i].get(c);
				double dist = 0.0;
				for (int j = 0; j < transversalNumCells.length; j++) {
					dist += Math.pow(gridPos[j] * as - centerOfAbsCharge[j], 2);
					dipoleVector[j] += charge * (gridPos[j] * as - centerOfAbsCharge[j]);
				}
				dist = Math.sqrt(dist);
				dipoleCharge += charge * dist / averageDist;
			}
			for (int j = 0; j < transversalNumCells.length; j++) {
				dipoleVector[j] /= dipoleCharge * averageDist;
			}

			// Add two charges to cancel dipole moment at center of abs. charge
			double[] dipoleChargePos1 = centerOfAbsCharge.clone();
			double[] dipoleChargePos2 = centerOfAbsCharge.clone();

			for (int j = 0; j < transversalNumCells.length; j++) {
				dipoleChargePos1[j] += dipoleVector[j] * averageDist / 2.0;
				dipoleChargePos2[j] -= dipoleVector[j] * averageDist / 2.0;
			}

			// Charges of the dipoles
			AlgebraElement dipoleCharge1 = s.grid.getElementFactory().algebraZero();
			AlgebraElement dipoleCharge2 = s.grid.getElementFactory().algebraZero();
			dipoleCharge1.set(c, -dipoleCharge);
			dipoleCharge2.set(c, dipoleCharge);

			interpolateChargeToGridCIC(s, dipoleChargePos1, dipoleCharge1);
			interpolateChargeToGridCIC(s, dipoleChargePos2, dipoleCharge2);
		}
	}

	/**
	 * Computes the total charge on the transversal lattice.
	 *
	 * @param s
	 * @return
	 */
	private AlgebraElement computeTotalCharge(Simulation s) {
		AlgebraElement totalCharge =  s.grid.getElementFactory().algebraZero();
		for (int i = 0; i < totalTransversalCells; i++) {
			totalCharge.addAssign(transversalChargeDensity[i]);
		}
		return totalCharge;
	}

	/**
	 * Computes the "center of charge" for a given component on the transversal lattice. The center is computed from the
	 * absolute values of the charges.
	 *
	 * @param component
	 * @return
	 */
	private double[] computeCenterOfAbsCharge(int component) {
		double[] center = new double[transversalNumCells.length];
		for (int j = 0; j < transversalNumCells.length; j++) {
			center[j] = 0.0;
		}

		double totalCharge = 0.0;
		for (int i = 0; i < totalTransversalCells; i++) {
			int[] gridPos = GridFunctions.getCellPos(i, transversalNumCells);
			double charge = Math.abs(transversalChargeDensity[i].get(component));
			totalCharge += charge;
			for (int j = 0; j < transversalNumCells.length; j++) {
				center[j] += charge * gridPos[j] * as;
			}
		}

		for (int j = 0; j < transversalNumCells.length; j++) {
			center[j] /= totalCharge;
		}

		return center;
	}

	/**
	 * Computes the average (weighted) distance of the charges of a certain component to the center of charge.
	 * This can be used to estimate the size of the charge distribution.
	 *
	 * @param s
	 * @param component
	 * @return
	 */
	private double computeAverageDistance(Simulation s, int component) {
		double averageDistance = 0.0;
		double[] centerOfCharge = computeCenterOfAbsCharge(component);
		double totalAbsCharge = 0.0;
		for (int i = 0; i < totalTransversalCells; i++) {
			int[] gridPos = GridFunctions.getCellPos(i, transversalNumCells);
			double charge = Math.abs(transversalChargeDensity[i].get(component));
			totalAbsCharge += charge;
			double dist = 0.0;
			for (int j = 0; j < transversalNumCells.length; j++) {
				dist += Math.pow(gridPos[j] * as - centerOfCharge[j], 2);
			}
			averageDistance += charge * Math.sqrt(dist);
		}
		return averageDistance / totalAbsCharge;
	}

	/**
	 * Computes the center of charge using the invariant charge (tr(Q^2))^0.5.
	 *
	 * @return
	 */
	private double[] computeCenterOfInvariantCharge() {
		double[] center = new double[transversalNumCells.length];
		for (int j = 0; j < transversalNumCells.length; j++) {
			center[j] = 0.0;
		}

		double totalInvCharge = 0.0;
		for (int i = 0; i < totalTransversalCells; i++) {
			int[] gridPos = GridFunctions.getCellPos(i, transversalNumCells);
			double invCharge = Math.sqrt(transversalChargeDensity[i].square());
			totalInvCharge += invCharge;
			for (int j = 0; j < transversalNumCells.length; j++) {
				center[j] += invCharge * gridPos[j] * as;
			}
		}

		for (int j = 0; j < transversalNumCells.length; j++) {
			center[j] /= totalInvCharge;
		}

		return center;
	}


	/**
	 * Utility class to deal with point charges. Only used to specify the initial conditions.
	 */
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
