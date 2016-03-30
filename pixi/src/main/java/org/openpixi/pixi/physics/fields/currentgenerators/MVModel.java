package org.openpixi.pixi.physics.fields.currentgenerators;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.SimulationType;
import org.openpixi.pixi.physics.util.GridFunctions;

import java.util.Random;

public class MVModel implements ICurrentGenerator {

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
	 * Coefficient used for the UV regulator, which is implemented as a hard cutoff. This parameter is given in units of
	 * the maximum lattice momentum. A value of 1.0 corresponds to no UV cutoff. A value of 0.0 cuts off all modes in
	 * momentum space.
	 */
	private double lowPassCoefficient ;

	/**
	 * Coefficient used for the IR regulator, which is implemented as a mass-term in the Poisson solver. As with the
	 * UV regulator this coefficient is given in units of the lattice momentum. A value of 0.0 removes the IR regulator,
	 * any other value leads to a finite mass term in the Poisson equation.
	 */
	private double infraredCoefficient;

	/**
	 * Option whether to use the \mu^2 (true) or the g^2 \mu^2 (false) normalization for the Gaussian
	 * probability distribution of the color charge densities.
	 */
	private boolean useAlternativeNormalization;

	protected ParticleLCCurrent particleLCCurrent;

	public MVModel(int direction, int orientation, double location, double longitudinalWidth, double mu,
				   boolean useSeed, int seed,
				   double lowPassCoefficient, double infraredCoefficient, boolean useAlternativeNormalization){
		this.direction = direction;
		this.orientation = orientation;
		this.location = location;
		this.longitudinalWidth = longitudinalWidth;
		this.mu = mu;
		this.useSeed = useSeed;
		this.seed = seed;
		this.lowPassCoefficient = lowPassCoefficient;
		this.infraredCoefficient = infraredCoefficient;
		this.useAlternativeNormalization = useAlternativeNormalization;
	}

	public void initializeCurrent(Simulation s, int totalInstances) {
		int[] transversalNumCells = GridFunctions.reduceGridPos(s.grid.getNumCells(), direction);
		int totalTransversalCells = GridFunctions.getTotalNumberOfCells(transversalNumCells);
		int numberOfComponents = s.grid.getElementFactory().numberOfComponents;

		Random rand = new Random();
		if(useSeed) {
			rand.setSeed(seed);
		}

		// Initialize transversal charge density with random charges from a Gaussian distribution with zero mean and width \mu.
		AlgebraElement[] transversalChargeDensity = new AlgebraElement[totalTransversalCells];
		AlgebraElement totalCharge = s.grid.getElementFactory().algebraZero();

		double gaussianWidth;
		if(useAlternativeNormalization) {
			gaussianWidth = mu / s.grid.getLatticeSpacing();
		} else {
			gaussianWidth = mu * s.getCouplingConstant() / s.grid.getLatticeSpacing();
		}

		for (int i = 0; i < totalTransversalCells; i++) {
			AlgebraElement charge = s.grid.getElementFactory().algebraZero();
			for (int c = 0; c < numberOfComponents; c++) {
				double value = rand.nextGaussian() * gaussianWidth;
				charge.set(c, value);
			}
			totalCharge.addAssign(charge);
			transversalChargeDensity[i] = charge;
		}

		// Remove monopole moment.
		totalCharge.multAssign(- 1.0 / totalTransversalCells);
		for (int i = 0; i < totalTransversalCells; i++) {
			transversalChargeDensity[i].addAssign(totalCharge);
		}

		// Initialize particle light-cone current
		double L = s.grid.getNumCells(direction) * s.grid.getLatticeSpacing();

		// Wrap location
		if(location < 0) {
			location += L;
		} else if(location > L) {
			location -= L;
		}

		if(s.getSimulationType() == SimulationType.TemporalCGC) {
			this.particleLCCurrent = new ParticleLCCurrent(direction, orientation, location, longitudinalWidth);
		} else if(s.getSimulationType() == SimulationType.TemporalCGCNGP) {
			this.particleLCCurrent = new ParticleLCCurrentNGP(direction, orientation, location, longitudinalWidth);
		}
		particleLCCurrent.lowPassCoefficient = lowPassCoefficient;
		particleLCCurrent.infraredCoefficient = infraredCoefficient;
		particleLCCurrent.setTransversalChargeDensity(transversalChargeDensity);
		particleLCCurrent.initializeCurrent(s, totalInstances);

	}


	public void applyCurrent(Simulation s) {
		particleLCCurrent.applyCurrent(s);
	}

}
