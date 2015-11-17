package org.openpixi.pixi.physics.fields.currentgenerators;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;
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

	private ParticleLCCurrent particleLCCurrent;


	public MVModel(int direction, int orientation, double location, double longitudinalWidth, double mu) {
		this(direction, orientation, location, longitudinalWidth, mu, 0);
		this.useSeed = false;
	}

	public MVModel(int direction, int orientation, double location, double longitudinalWidth, double mu, int seed){
		this.direction = direction;
		this.orientation = orientation;
		this.location = location;
		this.longitudinalWidth = longitudinalWidth;
		this.mu = mu;
		this.seed = seed;
		this.useSeed = true;

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
		double gaussianWidth = mu * s.getCouplingConstant() / s.grid.getLatticeSpacing();
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

		this.particleLCCurrent = new ParticleLCCurrent(direction, orientation, location, longitudinalWidth);
		particleLCCurrent.setTransversalChargeDensity(transversalChargeDensity);
		particleLCCurrent.initializeCurrent(s, totalInstances);
	}


	public void applyCurrent(Simulation s) {
		particleLCCurrent.applyCurrent(s);
	}

}
