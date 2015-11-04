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
public class NucleusLCCurrent implements ICurrentGenerator {

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
	 * Option whether to use the constituent quark model or not. In the latter case spherical proton model is used!!
	 */
	private boolean useConstituentQuarks;

	/**
	 * List of point charges to use as intial conditions.
	 */
	private ArrayList<NucleonCharge> charges;

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
	 * SphericalProtonLCCurrent for the spherical proton model.
	 */
	private SphericalProtonLCCurrent sphericalProtonLCCurrent;

	/**
	 * ConstituentProtonLCCurrent for the constituent quark proton model.
	 */
	private ConstituentProtonLCCurrent constituentProtonLCCurrent;

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
	public NucleusLCCurrent(int direction, int orientation, double location, double longitudinalWidth, double[] locationTransverse, boolean useMonopoleRemoval, boolean useDipoleRemoval, boolean useConstituentQuarks, Random rand) {
		this.direction = direction;
		this.orientation = orientation;
		this.location = location;
		this.longitudinalWidth = longitudinalWidth;
		this.locationTransverse = locationTransverse;
		this.useMonopoleRemoval = useMonopoleRemoval;
		this.useDipoleRemoval = useDipoleRemoval;
		this.useConstituentQuarks = useConstituentQuarks;
		this.rand = rand;

		this.charges = new ArrayList<NucleonCharge>();
		this.particleLCCurrent = new ParticleLCCurrent(direction, orientation, location, longitudinalWidth);
	}

	/**
	 * Adds a point charge at a certain position. This is mainly used to specify initial conditions.
	 *
	 * @param location
	 * @param width
	 */
	public void addNucleon(double[] location, double width, double partonWidth) {
		// This method should be called from the YAML object to add the nucleons for the current generator.
		this.charges.add(new NucleonCharge(location, width, partonWidth));
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

		// Iterate over nucleons, create a charge distribution around them and add them to the transversal charge density.
		for (int i = 0; i < charges.size(); i++) {
			AlgebraElement[] nucleonChargeDensity;
			NucleonCharge c = charges.get(i);

			if (useConstituentQuarks) {

				constituentProtonLCCurrent = new ConstituentProtonLCCurrent(direction, orientation, location, longitudinalWidth, locationTransverse, useMonopoleRemoval, useDipoleRemoval, rand);
				for (int j = 0; j < 3; j++) {
					double[] protonLocation = new double[c.location.length];
					for (int k = 0; k < c.location.length; k++) {
						protonLocation[k] = c.location[k] + rand.nextGaussian() * c.width;
					}
					constituentProtonLCCurrent.addCharge(protonLocation, c.partonWidth);
				}
				nucleonChargeDensity = constituentProtonLCCurrent.computeChargeDensity(s);

			} else {

				sphericalProtonLCCurrent = new SphericalProtonLCCurrent(direction, orientation, location, longitudinalWidth, useMonopoleRemoval, useDipoleRemoval, rand);
				for (int j = 0; j < 3; j++) {
					sphericalProtonLCCurrent.addCharge(c.location, c.width);
				}
				nucleonChargeDensity = sphericalProtonLCCurrent.computeChargeDensity(s);

			}

			for (int w = 0; w < totalTransversalCells; w++) {
				transversalChargeDensity[w].addAssign(nucleonChargeDensity[w]);
			}
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
	 * Utility class to deal with nucleon charges. Only used to specify the initial conditions.
	 */
	class NucleonCharge {
		public double[] location;
		double width, partonWidth;

		public NucleonCharge(double[] location, double width, double partonWidth) {
			this.location = location;
			this.width = width;
			this.partonWidth = partonWidth;
		}
	}
}
