package org.openpixi.pixi.physics.initial.CGC;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;

import java.util.Random;

public class MVModel implements IInitialChargeDensity {

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
	 * Longitudinal width of the charge density.
	 */
	private double longitudinalWidth;

	/**
	 * \mu parameter of the MV model.
	 */
	private double mu;

	/**
	 * Seed for the random variables.
	 */
	private boolean useSeed = false;
	private int seed;

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
	private double longitudinalCoherenceLength;

	/**
	 * Coefficient used for the IR regulator, which is implemented as a mass-term in the Poisson solver. As with the
	 * UV regulator this coefficient is given in units of inverse lattice spacings. A value of 0.0 removes the IR
	 * regulator, any other value leads to a finite mass term in the Poisson equation.
	 */
	private double infraredCoefficient;

	/**
	 * This class implements the color charge density of the MV model with longitudinal randomness. The fields are
	 * regulated in Fourier space with a hard UV cutoff in the transverse and longitudinal directions. The IR modes are
	 * regulated in the transverse plane with a 'gluon mass' term.
	 *
	 * @param direction                   index of the longitudinal direction
	 * @param orientation                 orientation of movement in the longitudinal direction
	 * @param location                    longitudinal position
	 * @param longitudinalWidth           longitudinal width of the MV model
	 * @param mu                          MV model parameter
	 * @param useSeed                     use a fixed seed for random number generation
	 * @param seed                        seed of the random number generator
	 * @param ultravioletCutoffTransverse UV cutoff in transverse plane (in inverse lattice spacings)
	 * @param longitudinalCoherenceLength Longitudinal coherence length inside nucleus (in physical units)
	 * @param infraredCoefficient         IR regulator coefficient in the transverse plane
	 */
	public MVModel(int direction, int orientation, double location, double longitudinalWidth, double mu,
	               boolean useSeed, int seed,
	               double ultravioletCutoffTransverse, double longitudinalCoherenceLength,
	               double infraredCoefficient) {

		this.direction = direction;
		this.orientation = orientation;
		this.location = location;
		this.longitudinalWidth = longitudinalWidth;
		this.mu = mu;
		this.useSeed = useSeed;
		this.seed = seed;
		this.ultravioletCutoffTransverse = ultravioletCutoffTransverse;
		this.longitudinalCoherenceLength = longitudinalCoherenceLength;
		this.infraredCoefficient = infraredCoefficient;
	}

	public void initialize(Simulation s) {
		int totalCells = s.grid.getTotalNumberOfCells();
		int numberOfColors = s.getNumberOfColors();
		int numberOfComponents = (numberOfColors > 1) ? numberOfColors * numberOfColors - 1 : 1;

		this.rho = new AlgebraElement[totalCells];
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			this.rho[i] = s.grid.getElementFactory().algebraZero();
		}


		// Longitudinal and transverse lattice spacing
		double aL = s.grid.getLatticeSpacing(direction);
		double aT = s.grid.getLatticeSpacing((direction + 1) % s.getNumberOfDimensions());

		Random rand = new Random();
		if (useSeed) {
			rand.setSeed(seed);
		}

		for (int j = 0; j < numberOfComponents; j++) {
			double[] tempRho = new double[s.grid.getTotalNumberOfCells()];

			// Place random charges on the grid (with longitudinal randomness and profile).
			Gaussian gauss = new Gaussian(location, longitudinalWidth);
			double randomColorWidth = mu * s.getCouplingConstant() / Math.sqrt(aL * aT * aT);
			for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
				int[] pos = s.grid.getCellPos(i);
				double longPos = pos[direction] * s.grid.getLatticeSpacing(direction);
				double profile = Math.sqrt(gauss.value(longPos));
				tempRho[i] = rand.nextGaussian() * randomColorWidth * profile;
			}

			// Apply soft momentum regulation in Fourier space.
			tempRho = FourierFunctions.regulateChargeDensityGaussian(tempRho, s.grid.getNumCells(),
					ultravioletCutoffTransverse, longitudinalCoherenceLength, infraredCoefficient, direction,
					aT, aL);

			/*
			 Put everything into rho array, but exclude charges that lie outside of a simulation box centered around the
			 longitudinal location of the MV model.
			  */
			double simulationBoxWidth = s.grid.getNumCells(direction) * s.grid.getLatticeSpacing(direction);
			double zmin = Math.max(this.location - simulationBoxWidth / 2.0, 0.0);
			double zmax = Math.min(this.location + simulationBoxWidth / 2.0, simulationBoxWidth);
			int lmin = (int) Math.floor(zmin / s.grid.getLatticeSpacing(direction));
			int lmax = (int) Math.ceil(zmax / s.grid.getLatticeSpacing(direction));
			for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
				int longPos = s.grid.getCellPos(i)[direction];
				if (lmin < longPos && longPos < lmax && s.grid.isActive(i)) {
					this.rho[i].set(j, tempRho[i]);
				}
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

	public String getInfo() {
		/*
			mu   ... MV model parameter
			w    ... longitudinal width
			UVT  ... transverse UV cutoff
			c    ... longitudinal correlation length
			m    ... IR regulator
		 */
		return String.format("MV, mu: %f, w: %f, UVT: %f, c: %f, m: %f",
				mu, longitudinalWidth, ultravioletCutoffTransverse, longitudinalCoherenceLength, infraredCoefficient);
	}

	public void clear() {
		this.rho = null;
	}
}


