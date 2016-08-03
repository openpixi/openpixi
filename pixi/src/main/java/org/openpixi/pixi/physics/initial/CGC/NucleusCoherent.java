package org.openpixi.pixi.physics.initial.CGC;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.ArrayList;
import java.util.Random;

public class NucleusCoherent implements IInitialChargeDensity {

	/**
	 * Charge density as an array of AlgebraElements.
	 */
	private AlgebraElement[] rho;

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
	 * Transverse location of the initial charge density in the simulation box.
	 */
	private double[] locationTransverse;

	/**
	 * Longitudinal width of the charge density.
	 */
	private double longitudinalWidth;

	/**
	 * Option whether to use the constituent quark model or not. In the latter case spherical proton model is used!!
	 */
	private boolean useConstituentQuarks;

	/**
	 * List of nucleons to use as intial conditions.
	 */
	private ArrayList<NucleonCharge> nucleons;

	/**
	 * List of quarks to use as intial conditions.
	 */
	private ArrayList<GaussianQuarkCharge> quarks;

	/**
	 * Color charge density, similar to the \mu parameter of the MV model.
	 */
	private double mu;

	/**
	 * Transversal radius of the Woods-Saxon (Fermi-Dirac) distribution
	 */
	private double transversalRadius;

	/**
	 * Transversal surface thickness of the Woods-Saxon (Fermi-Dirac) distribution
	 */
	private double surfaceThickness;

	/**
	 * Width of the Gaussian distribution of the valence quark charge
	 */
	private double partonWidth;

	/**
	 * Width of the Gaussian distribution of the nucleons
	 */
	private double nucleonWidth;

	/**
	 * Seed for the random variables.
	 */
	private boolean useSeed = false;
	private int seed;

	/**
	 * Number of nucleons in the simulated nucleus.
	 */
	private int numberOfNucleons;

	/**
	 * Coefficient used for the transverse UV regulator, which is implemented as a hard cutoff. This parameter is given
	 * in units of inverse lattice spacings. A value of sqrt(2)*PI corresponds to no UV cutoff. A value of 0.0 cuts off
	 * all modes in momentum space.
	 */
	private double ultravioletCutoffTransverse;

	/**
	 * Coefficient used for the longitudinal UV regulator, which is implemented as a hard cutoff. This parameter is
	 * given in units of inverse lattice spacings. A value of sqrt(2)*PI corresponds to no UV cutoff. A value of 0.0
	 * cuts off all modes in momentum space.
	 */
	private double ultravioletCutoffLongitudinal;

	/**
	 * Coefficient used for the IR regulator, which is implemented as a mass-term in the Poisson solver. As with the
	 * UV regulator this coefficient is given in units of inverse lattice spacings. A value of 0.0 removes the IR
	 * regulator, any other value leads to a finite mass term in the Poisson equation.
	 */
	private double infraredCoefficient;

	/**
	 * This class implements the color charge density of the MV model with coherent longitudinal structure. The fields
	 * are regulated in Fourier space with a hard UV cutoff in the transverse and longitudinal directions, but the
	 * longitudinal cutoff should not have any effect. The IR modes are regulated in the transverse plane with a
	 * 'gluon mass' term.
	 *
	 * @param direction                         index of the longitudinal direction
	 * @param orientation                       orientation of movement in the longitudinal direction
	 * @param location                          longitudinal position
	 * @param longitudinalWidth                 longitudinal width of the MV model
	 * @param mu                                MV model parameter
	 * @param useSeed                           use a fixed seed for random number generation
	 * @param seed                              seed of the random number generator
	 * @param ultravioletCutoffTransverse       UV cutoff in transverse plane (in inverse lattice spacings)
	 * @param ultravioletCutoffLongitudinal     UV cutoff in the longitudinal direction (in inverse lattice spacings)
	 * @param infraredCoefficient               IR regulator coefficient in the transverse plane
	 */
	public NucleusCoherent(int direction, int orientation, double location, double[] locationTransverse, double longitudinalWidth, double mu,
						   boolean useSeed, int seed, int numberOfNucleons, boolean useConstituentQuarks, double transversalRadius, double surfaceThickness,
						   double nucleonWidth, double partonWidth, double ultravioletCutoffTransverse, double ultravioletCutoffLongitudinal,
						   double infraredCoefficient){

		this.direction = direction;
		this.orientation = orientation;
		this.location = location;
		this.longitudinalWidth = longitudinalWidth;
		this.locationTransverse = locationTransverse;
		this.transversalRadius = transversalRadius;
		this.surfaceThickness = surfaceThickness;
		this.nucleonWidth = nucleonWidth;
		this.partonWidth = partonWidth;
		this.mu = mu;
		this.useSeed = useSeed;
		this.useConstituentQuarks = useConstituentQuarks;
		this.seed = seed;
		this.numberOfNucleons = numberOfNucleons;
		this.ultravioletCutoffTransverse = ultravioletCutoffTransverse;
		this.ultravioletCutoffLongitudinal = ultravioletCutoffLongitudinal;
		this.infraredCoefficient = infraredCoefficient;

		this.nucleons = new ArrayList<NucleonCharge>();
		this.quarks = new ArrayList<GaussianQuarkCharge>();
	}

	public void initialize(Simulation s) {
		int totalCells = s.grid.getTotalNumberOfCells();
		int numberOfColors = s.getNumberOfColors();
		int numberOfComponents = (numberOfColors > 1) ? numberOfColors * numberOfColors - 1 : 1;
		double as = s.grid.getLatticeSpacing();
		int[] transNumCells = GridFunctions.reduceGridPos(s.grid.getNumCells(), direction);
		int totalTransCells = GridFunctions.getTotalNumberOfCells(transNumCells);
		int longitudinalNumCells = s.grid.getNumCells(direction);

		this.rho = new AlgebraElement[totalCells];
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			this.rho[i] = s.grid.getElementFactory().algebraZero();
		}

		double[] transversalWidths = new double[totalTransCells];

		Random rand = new Random();
		if(useSeed) {
			rand.setSeed(seed);
		}

		// Iterate over nucleons and place them according to a Woods-Saxon profile.
		int start = 0, range;
		if(transNumCells[start] > 1) {
			range = transNumCells[start];
		} else {
			range = transNumCells[start+1];
		}
		for (int j = 0; j < transNumCells.length; j++) {
			if( (transNumCells[j] > 1) && (transNumCells[j] < range) ) {
				range = transNumCells[j];
			}
		}

		ArrayList<double[]> listOfNucleonLocations = new ArrayList<double[]>();
		for(int i = 0; i < numberOfNucleons; i++) {
			double[] chargeLocation = new double[locationTransverse.length];
			double[] woodsSaxon = getWoodsSaxonMonteCarlo(rand, range*as);
			chargeLocation[0] = locationTransverse[0] + woodsSaxon[0];//Attention: This only works in 3D!!!
			chargeLocation[1] = locationTransverse[1] + woodsSaxon[1];//Attention: This only works in 3D!!!
			/*for (int j = 0; j < locationTransverse.length; j++) {
				chargeLocation[j] = locationTransverse[j] + getWoodsSaxonMonteCarlo(rand, range*as);
			}*/
			listOfNucleonLocations.add(chargeLocation);
		}

		for(int i = 0; i < numberOfNucleons; i++) {
			addNucleon(listOfNucleonLocations.get(i), nucleonWidth, partonWidth);
		}


		int numOverlappingQuarks = 1;
		if(!useConstituentQuarks) {
			numOverlappingQuarks = 3;
		}

		// Iterate over nucleons, create a quark distribution inside of them and add them to the quark array.
		for (int i = 0; i < nucleons.size(); i++) {
			NucleonCharge nc = nucleons.get(i);

			for (int j = 0; j < 3; j++) {

				if (useConstituentQuarks) {

					double[] protonLocation = new double[nc.location.length];
					for (int k = 0; k < nc.location.length; k++) {
						protonLocation[k] = nc.location[k] + rand.nextGaussian() * nc.width;
					}
					addQuark(protonLocation, nc.partonWidth);

				} else {

					addQuark(nc.location, nc.width);

				}
			}
		}

		double norm = 0.0;
		for (int i = 0; i < quarks.size(); i++) {
			GaussianQuarkCharge qc = quarks.get(i);
			for (int k = 0; k < totalTransCells; k++) {
				double distance = getDistance(qc.location, GridFunctions.getCellPos(k, transNumCells), as);
				transversalWidths[k] += Math.abs(shapeFunction(distance, qc.width) / Math.pow(qc.width * Math.sqrt(2 * Math.PI), transNumCells.length)/numOverlappingQuarks);
				norm += Math.abs(shapeFunction(distance, qc.width) / Math.pow(qc.width * Math.sqrt(2 * Math.PI), transNumCells.length)/numOverlappingQuarks);
			}
		}
		for (int k = 0; k < totalTransCells; k++) {
			transversalWidths[k] /= norm;
		}

		for (int j = 0; j < numberOfComponents; j++) {
			double[] tempRho = new double[s.grid.getTotalNumberOfCells()];

			// Place random charges on the grid (with coherent longitudinal structure).
			for (int i = 0; i < totalTransCells; i++) {
				double charge = rand.nextGaussian() * transversalWidths[i] * mu * s.getCouplingConstant() / as;
				int[] transPos = GridFunctions.getCellPos(i, transNumCells);
				for (int k = 0; k < longitudinalNumCells; k++) {
					int[] gridPos = GridFunctions.insertGridPos(transPos, direction, k);
					int index = s.grid.getCellIndex(gridPos);
					tempRho[index] = charge;
				}
			}

			// Apply hard momentum regulation in Fourier space.
			tempRho = FourierFunctions.regulateChargeDensityHard(tempRho, s.grid.getNumCells(),
					ultravioletCutoffTransverse, ultravioletCutoffLongitudinal, infraredCoefficient, direction,
					s.grid.getLatticeSpacing());

			// Apply longitudinal profile.
			Gaussian gauss = new Gaussian(location, longitudinalWidth);
			for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
				int[] pos = s.grid.getCellPos(i);
				double longPos = pos[direction] * s.grid.getLatticeSpacing();
				double profile = gauss.value(longPos);
				tempRho[i] *= profile;
			}

			// Put everything into rho array.
			for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
				this.rho[i].set(j, tempRho[i]);
			}
		}

		// Done!
	}

	public AlgebraElement getChargeDensity(int index) {
		return rho[index];
	}

	public AlgebraElement[] getChargeDensity() {
		return rho;
	}

	public int getDirection() {
		return direction;
	}

	public int getOrientation() {
		return orientation;
	}

	public double getRegulator() {
		return infraredCoefficient;
	}

	public void clear() {
		this.rho = null;
	}

	public String getInfo() {
		/*
			mu   ... MV model parameter
			w    ... longitudinal width
			UVT  ... transverse UV cutoff
			R    ... nuclear radius
			m    ... IR regulator
		 */
		return String.format("MV, mu: %f, w: %f, UVT: %f, R: %f, m: %f",
				mu, longitudinalWidth, ultravioletCutoffTransverse, transversalRadius, infraredCoefficient);
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

	private double[] getWoodsSaxonMonteCarlo(Random rand, double range) {
		double[] random = new double[2];
		double random3, radius, y;
		do {
			random[0] = rand.nextDouble();
			random[1] = rand.nextDouble();
			random3 = rand.nextDouble();
			double norm = 2.0*Math.PI/surfaceThickness*Math.log(1.0 + Math.exp(transversalRadius/surfaceThickness));
			//double range = transversalRadius + surfaceThickness*Math.log(1.0/(10e-10*norm) - 1.0);
			random[0] *= range;
			random[1] *= range;
			radius = Math.sqrt(random[0]*random[0] + random[1]*random[1]);
			random3 /= norm;
			y = 1.0/(norm*(Math.exp((radius - transversalRadius)/surfaceThickness) + 1));
		} while (random3 > y);

		return random;
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

	/**
	 * Utility class to deal with Gaussian quark charges. Only used to specify the initial conditions.
	 */
	class GaussianQuarkCharge {
		public double[] location;
		double width;

		public GaussianQuarkCharge(double[] location, double width) {
			this.location = location;
			this.width = width;
		}
	}

	/**
	 * Adds a nucleon at a certain position. This is mainly used to specify initial conditions.
	 *
	 * @param location
	 * @param width
	 */
	private void addNucleon(double[] location, double width, double partonWidth) {
		this.nucleons.add(new NucleonCharge(location, width, partonWidth));
	}

	/**
	 * Adds a quark at a certain position. This is mainly used to specify initial conditions.
	 *
	 * @param location
	 * @param width
	 */
	private void addQuark(double[] location, double width) {
		this.quarks.add(new GaussianQuarkCharge(location, width));
	}
}


