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
public class ConstituentProtonLCCurrent implements ICurrentGenerator {

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
	 * Transversal location of the initial charge density in the simulation box.
	 */
	private double[] locationTransverse;

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
	 * Profile of the transversal charge density.
	 */
	private double[] transversalWidths;

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
	 * Random generator.
	 */
	private Random rand;


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
<<<<<<< HEAD
	public ConstituentProtonLCCurrent(int direction, int orientation, double location, double longitudinalWidth, double[] locationTransverse, boolean useMonopoleRemoval, boolean useDipoleRemoval, Random rand) {
=======
	public ConstituentProtonLCCurrent(int direction, int orientation, double location, double longitudinalWidth, double[] locationTransverse, boolean useMonopoleRemoval, boolean useDipoleRemoval, Random ranGen) {
>>>>>>> origin/YM
		this.direction = direction;
		this.orientation = orientation;
		this.location = location;
		this.longitudinalWidth = longitudinalWidth;
		this.locationTransverse = locationTransverse;
		this.useMonopoleRemoval = useMonopoleRemoval;
		this.useDipoleRemoval = useDipoleRemoval;
<<<<<<< HEAD
		this.rand = rand;
=======
		this.rand = ranGen;
>>>>>>> origin/YM

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

		// 1) Initialize transversal charge density grid using the charges array.
		transversalNumCells = GridFunctions.reduceGridPos(s.grid.getNumCells(), direction);
		totalTransversalCells = GridFunctions.getTotalNumberOfCells(transversalNumCells);
		transversalChargeDensity = new AlgebraElement[totalTransversalCells];
		transversalWidths = new double[totalTransversalCells];
		for (int i = 0; i < totalTransversalCells; i++) {
			transversalChargeDensity[i] = s.grid.getElementFactory().algebraZero();
		}

		// Iterate over (point) charges, create a Gaussian charge distribution around them and add them to the transversal charge density.
		double norm = 0.0;
		for (int i = 0; i < charges.size(); i++) {
			GaussianCharge c = charges.get(i);
			for (int k = 0; k < totalTransversalCells; k++) {
				double distance = getDistance(c.location, GridFunctions.getCellPos(k, transversalNumCells), as);
				transversalWidths[k] += Math.abs(shapeFunction(distance, c.width)/Math.pow(c.width*Math.sqrt(2*Math.PI), transversalNumCells.length));
				norm += Math.abs(shapeFunction(distance, c.width)/Math.pow(c.width*Math.sqrt(2*Math.PI), transversalNumCells.length));
			}
		}
		for (int k = 0; k < totalTransversalCells; k++) {
			transversalWidths[k] /= norm;
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

	public AlgebraElement[] computeChargeDensity(Simulation s) {
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
		transversalWidths = new double[totalTransversalCells];
		for (int i = 0; i < totalTransversalCells; i++) {
			transversalChargeDensity[i] = s.grid.getElementFactory().algebraZero();
		}

		// Iterate over (point) charges, create a Gaussian charge distribution around them and add them to the transversal charge density.
		double norm = 0.0;
		for (int i = 0; i < charges.size(); i++) {
			GaussianCharge c = charges.get(i);
			for (int k = 0; k < totalTransversalCells; k++) {
				double distance = getDistance(c.location, GridFunctions.getCellPos(k, transversalNumCells), as);
				transversalWidths[k] += Math.abs(shapeFunction(distance, c.width)/Math.pow(c.width*Math.sqrt(2*Math.PI), transversalNumCells.length));
				norm += Math.abs(shapeFunction(distance, c.width)/Math.pow(c.width*Math.sqrt(2*Math.PI), transversalNumCells.length));
			}
		}
		for (int k = 0; k < totalTransversalCells; k++) {
			transversalWidths[k] /= norm;
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

		return transversalChargeDensity;
	}


	/**
	 *
	 * @param s
	 */
	public void applyCurrent(Simulation s) {
		particleLCCurrent.applyCurrent(s);
	}

	/**
	 * Removes the monopole moment by subtracting a constant charge at each lattice site of the transversal charge density.
	 * This is not a good way to do this, so make sure the initial conditions are colorless at initialization.
	 *
	 * @param s
	 */
	private void removeMonopoleMoment(Simulation s) {
		AlgebraElement totalCharge = computeTotalCharge(s);
		/*for (int i = 0; i < numberOfComponents; i++) {
			System.out.println(totalCharge.get(i));
		}*/
		double check = 0.0;
		for (int i = 0; i < totalTransversalCells; i++) {
			transversalChargeDensity[i].addAssign(totalCharge.mult(-1.0*transversalWidths[i]));
			check += transversalWidths[i];
		}
		//System.out.println(check);
		/*
		totalCharge = computeTotalCharge(s);
		for (int i = 0; i < numberOfComponents; i++) {
			System.out.println(totalCharge.get(i));
		}
		*/
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
		for (int c = 0; c < transversalNumCells.length; c++) {
			for (int i = 0; i < totalTransversalCells; i++) {
				int[] gridPos = GridFunctions.getCellPos(i, transversalNumCells);
				dipoleVector[c].addAssign(transversalChargeDensity[i].mult(gridPos[c] * as - locationTransverse[c]));
			}
		}
		/*
		for (int c = 0; c < transversalNumCells.length; c++) {
			for (int i = 0; i < numberOfComponents; i++) {
				System.out.println(dipoleVector[c].get(i));
			}
		}
		*/
		for (int c = 0; c < transversalNumCells.length; c++) {
			for (int i = 0; i < totalTransversalCells; i++) {
				int[] gridPos = GridFunctions.getCellPos(i, transversalNumCells);
				gridPos[c]++;
				int z = GridFunctions.getCellIndex(gridPos, transversalNumCells);
				transversalChargeDensity[i].addAssign(dipoleVector[c].mult(transversalWidths[z] / as));
				transversalChargeDensity[i].addAssign(dipoleVector[c].mult(-1.0 * transversalWidths[i] / as));
			}
		}

		/*
		AlgebraElement[] checkDipoleVector = new AlgebraElement[transversalNumCells.length];
		for (int i = 0; i < transversalNumCells.length; i++) {
			checkDipoleVector[i] = s.grid.getElementFactory().algebraZero(s.getNumberOfColors());
		}
		for (int c = 0; c < transversalNumCells.length; c++) {
			for (int i = 0; i < totalTransversalCells; i++) {
				int[] gridPos = GridFunctions.getCellPos(i, transversalNumCells);
				checkDipoleVector[c].addAssign(transversalChargeDensity[i].mult(gridPos[c] * as - locationTransverse[c]));
			}
		}

		for (int c = 0; c < transversalNumCells.length; c++) {
			for (int i = 0; i < numberOfComponents; i++) {
				System.out.println(checkDipoleVector[c].get(i));
			}
		}
		*/
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
		Gaussian gauss = new Gaussian(0.0, width);
		return gauss.value(z);
	}
}
