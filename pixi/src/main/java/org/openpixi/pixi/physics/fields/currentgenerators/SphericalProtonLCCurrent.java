package org.openpixi.pixi.physics.fields.currentgenerators;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.ArrayList;
import java.util.Random;

/**
 * A simple current generator for point-like charges based on ParticleLCCurrent.
 */
public class SphericalProtonLCCurrent implements ICurrentGenerator {

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
	private ArrayList<GaussianCharge> charges;

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
	public SphericalProtonLCCurrent(int direction, int orientation, double location, double longitudinalWidth, boolean useMonopoleRemoval, boolean useDipoleRemoval) {
		this.direction = direction;
		this.orientation = orientation;
		this.location = location;
		this.longitudinalWidth = longitudinalWidth;
		this.useMonopoleRemoval = useMonopoleRemoval;
		this.useDipoleRemoval = useDipoleRemoval;

		this.charges = new ArrayList<GaussianCharge>();
		this.particleLCCurrent = new ParticleLCCurrent(direction, orientation, location, longitudinalWidth);
	}

	/**
	 * Adds a point charge at a certain position. This is mainly used to specify initial conditions.
	 *
	 * @param location
	 * @param width
	 */
	public void addCharge(double[] location, double width) {
		// This method should be called from the YAML object to add the charges for the current generator.
		this.charges.add(new GaussianCharge(location, width));
	}

	public void initializeCurrent(Simulation s, int dummy) {
		// 0) Define some variables.
		numberOfColors = s.getNumberOfColors();
		numberOfComponents = s.grid.getElementFactory().numberOfComponents;
		as = s.grid.getLatticeSpacing();
		at = s.getTimeStep();
		g = s.getCouplingConstant();
		Random rand = new Random();

		// 1) Initialize transversal charge density grid using the charges array.
		transversalNumCells = GridFunctions.reduceGridPos(s.grid.getNumCells(), direction);
		totalTransversalCells = GridFunctions.getTotalNumberOfCells(transversalNumCells);
		transversalChargeDensity = new AlgebraElement[totalTransversalCells];
		double[] transversalWidths = new double[totalTransversalCells];
		for (int i = 0; i < totalTransversalCells; i++) {
			transversalChargeDensity[i] = s.grid.getElementFactory().algebraZero();
		}

		// Iterate over (point) charges, create a Gaussian charge distribution around them and add them to the transversal charge density.
		for (int i = 0; i < charges.size(); i++) {
			GaussianCharge c = charges.get(i);
			for (int k = 0; k < totalTransversalCells; k++) {
				double distance = getDistance(c.location, GridFunctions.getCellPos(k, transversalNumCells), as);
				transversalWidths[k] += shapeFunction(distance, c.width)/Math.pow(c.width*Math.sqrt(2*Math.PI), transversalNumCells.length)/charges.size();
			}
		}

		for (int k = 0; k < totalTransversalCells; k++) {
			AlgebraElement chargeAmplitude = s.grid.getElementFactory().algebraZero(s.getNumberOfColors());
			for (int j = 0; j < numberOfComponents; j++) {
				chargeAmplitude.set(j, rand.nextGaussian()*transversalWidths[k] / Math.pow(as, s.getNumberOfDimensions() - 1));
			}
			transversalChargeDensity[k].addAssign(chargeAmplitude);
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
			double weigth = 1.0;
			for (int i = 0; i < effNumberOfDimensions; i++) {
				double dist = 1.0 - Math.abs(p[i] - chargePosition[i] / as);
				weigth *= dist;
			}
			transversalChargeDensity[GridFunctions.getCellIndex(p, transversalNumCells)].addAssign(charge.mult(weigth));
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
		GaussianCharge c = charges.get(0);
		for (int i = 0; i < totalTransversalCells; i++) {
			double distance = getDistance(c.location, GridFunctions.getCellPos(i, transversalNumCells), as);
			transversalChargeDensity[i].sub(totalCharge.mult(shapeFunction(distance, c.width)/Math.pow(c.width*Math.sqrt(2*Math.PI), transversalNumCells.length)));
		}
	}

	/**
	 * Removes the dipole moment by adding dipoles for each color component. These dipoles cancel the total dipole moment.
	 *
	 * @param s
	 */
	private void removeDipoleMoment(Simulation s) {
		AlgebraElement[] dipoleVector = new AlgebraElement[transversalNumCells.length];
		for (int i = 0; i < transversalNumCells.length; i++) {
			dipoleVector[i] = s.grid.getElementFactory().algebraZero(s.getNumberOfColors());
		}
		GaussianCharge parton = charges.get(0);
		for (int c = 0; c < transversalNumCells.length; c++) {
			for (int i = 0; i < totalTransversalCells; i++) {
				int[] gridPos = GridFunctions.getCellPos(i, transversalNumCells);
				dipoleVector[c].addAssign(transversalChargeDensity[i].mult(gridPos[c] * as - parton.location[c]));
			}
		}

		for (int c = 0; c < transversalNumCells.length; c++) {
			for (int i = 0; i < totalTransversalCells; i++) {
				int[] gridPos1 = GridFunctions.getCellPos(i, transversalNumCells);
				int[] gridPos2 = GridFunctions.getCellPos(i, transversalNumCells);
				gridPos2[c]++;
				double distance1 = getDistance(parton.location, gridPos1, as);
				double distance2 = getDistance(parton.location, gridPos2, as);
				transversalChargeDensity[i].add(dipoleVector[c].mult(shapeFunction(distance2, parton.width) / Math.pow(parton.width * Math.sqrt(2 * Math.PI), transversalNumCells.length) / as));
				transversalChargeDensity[i].sub(dipoleVector[c].mult(shapeFunction(distance1, parton.width) / Math.pow(parton.width * Math.sqrt(2 * Math.PI), transversalNumCells.length) / as));
			}
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
	 * Utility class to deal with Gaussian charges. Only used to specify the initial conditions.
	 */
	class GaussianCharge {
		public double[] location;
		double width;

		public GaussianCharge(double[] location, double width) {
			this.location = location;
			this.width = width;
		}
	}

	private double getDistance(double[] center, int[] position, double spacing) {
		double distance = 0.0;
		for (int j = 0; j < position.length; j++) {
			distance += Math.pow(center[j] - spacing*position[j], 2);
		}
		return Math.sqrt(distance);
	}

	private double shapeFunction(double z, double width) {
		Gaussian gauss = new Gaussian(1.0, 0.0, width);
		return gauss.value(z);
	}
}
