package org.openpixi.pixi.ui.util.yaml.currentgenerators;

import org.openpixi.pixi.physics.fields.currentgenerators.ConstituentProtonLCCurrent;
import org.openpixi.pixi.physics.fields.currentgenerators.SphericalProtonLCCurrent;

import java.util.ArrayList;
import java.util.Random;

public class YamlRandomTemporalParticleColorCurrentConstituentProton {
	/**
	 * Direction of the current pulse (0 to d)
	 */
	public Integer direction;

	/**
	 * Orientation of the current pulse (-1 or 1)
	 */
	public Integer orientation;

	/**
	 * Starting location of the pulse on the longitudinal line
	 */
	public Double longitudinalLocation;

	/**
	 *	Center of the charge distribution in the transversal plane
	 */
	public ArrayList<Double> transversalLocation;

	/**
	 * Longitudinal width of the pulse (Gauss shape)
	 */
	public Double longitudinalWidth;

	/**
	 * Transversal width of the random charge distribution
	 */
	public Double transversalWidth;

	/**
	 * Number of point-like charges in the distribution
	 */
	public Integer numberOfCharges;

	/**
	 * Width of the Gaussian distribution of the valence quark charge
	 */
	public Double partonWidth;

	/**
	 * Number of colors
	 */
	public Integer numberOfColors;

	/**
	 * Seed to use for the random number generator
	 */
	public Integer randomSeed = null;

	/**
	 * Option whether to use the dipole removal method.
	 */
	public Boolean useDipoleRemoval = true;

	/**
	 * Option whether to use the monopole removal method.
	 */
	public Boolean useMonopoleRemoval = true;

	public ConstituentProtonLCCurrent getCurrentGenerator() {
		double[] locationTransverse = new double[transversalLocation.size()];
		for (int j = 0; j < transversalLocation.size(); j++) {
			locationTransverse[j] = transversalLocation.get(j);
		}
<<<<<<< HEAD
=======

>>>>>>> origin/YM
		Random rand = new Random();
		if(randomSeed != null) {
			rand.setSeed(randomSeed);
		}

		ArrayList<double[]> listOfChargeLocations = new ArrayList<double[]>();
		for(int i = 0; i < numberOfCharges; i++) {
			double[] chargeLocation = new double[transversalLocation.size()];
			for (int j = 0; j < transversalLocation.size(); j++) {
				chargeLocation[j] = transversalLocation.get(j) + rand.nextGaussian() * transversalWidth;
			}
			listOfChargeLocations.add(chargeLocation);
		}

		ConstituentProtonLCCurrent generator = new ConstituentProtonLCCurrent(direction, orientation, longitudinalLocation, longitudinalWidth, locationTransverse, useMonopoleRemoval, useDipoleRemoval, rand);

		for(int i = 0; i < numberOfCharges; i++) {
			generator.addCharge(listOfChargeLocations.get(i), partonWidth);
		}

		return generator;
	}


}
