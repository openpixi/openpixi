package org.openpixi.pixi.ui.util.yaml.currentgenerators;

import org.openpixi.pixi.physics.fields.currentgenerators.ConstituentProtonLCCurrent;
import org.openpixi.pixi.physics.fields.currentgenerators.NucleusLCCurrent;

import java.util.ArrayList;
import java.util.Random;

public class YamlRandomTemporalParticleColorCurrentNucleus {
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
	 * Transversal radius of the Woods-Saxon (Fermi-Dirac) distribution
	 */
	public Double transversalRadius;

	/**
	 * Transversal surface thickness of the Woods-Saxon (Fermi-Dirac) distribution
	 */
	public Double surfaceThickness;

	/**
	 * Number of nucleons in the nucleus
	 */
	public Integer numberOfNucleons;

	/**
	 * Width of the Gaussian distribution of the valence quark charge
	 */
	public Double partonWidth;

	/**
	 * Width of the Gaussian distribution of the nucleons
	 */
	public Double nucleonWidth;

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

	/**
	 * Option whether to use the constituent quark model for the nucleons.
	 */
	public Boolean useConstituentQuarks = true;


	public NucleusLCCurrent getCurrentGenerator() {
		double[] locationTransverse = new double[transversalLocation.size()];
		for (int j = 0; j < transversalLocation.size(); j++) {
			locationTransverse[j] = transversalLocation.get(j);
		}
		NucleusLCCurrent generator = new NucleusLCCurrent(direction, orientation, longitudinalLocation, longitudinalWidth, locationTransverse, useMonopoleRemoval, useDipoleRemoval, randomSeed);
		Random rand = new Random();
		if(randomSeed != null) {
			rand.setSeed(randomSeed);
		}

		int numberOfComponents = numberOfColors * numberOfColors - 1;
		if(numberOfColors == 1) {
			numberOfComponents = 1;
		}

		ArrayList<double[]> listOfChargeLocations = new ArrayList<double[]>();
		for(int i = 0; i < numberOfNucleons; i++) {
			double[] chargeLocation = new double[transversalLocation.size()];
			for (int j = 0; j < transversalLocation.size(); j++) {
				chargeLocation[j] = transversalLocation.get(j) + rand.nextGaussian() * transversalRadius;
			}
			listOfChargeLocations.add(chargeLocation);
		}

		for(int i = 0; i < numberOfNucleons; i++) {
			generator.addCharge(listOfChargeLocations.get(i), partonWidth);
		}

		return generator;
	}


}
