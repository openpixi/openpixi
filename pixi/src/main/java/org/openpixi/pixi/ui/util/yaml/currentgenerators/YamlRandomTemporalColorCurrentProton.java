package org.openpixi.pixi.ui.util.yaml.currentgenerators;

import org.openpixi.pixi.physics.fields.currentgenerators.NewLCCurrentProton;

import java.util.ArrayList;
import java.util.Random;

public class YamlRandomTemporalColorCurrentProton {
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


	public NewLCCurrentProton getCurrentGenerator() {
		double[] locationTransverse = new double[transversalLocation.size()];
		for (int i = 0; i < transversalLocation.size(); i++) {
			locationTransverse[i] = transversalLocation.get(i);
		}
		NewLCCurrentProton generator = new NewLCCurrentProton(direction, orientation, longitudinalLocation, longitudinalWidth, locationTransverse);
		Random rand = new Random();
		if(randomSeed != null) {
			rand.setSeed(randomSeed);
		}

		int numberOfComponents = numberOfColors * numberOfColors - 1;
		if(numberOfColors == 1) {
			numberOfComponents = 1;
		}

		ArrayList<double[]> listOfChargeLocations = new ArrayList<double[]>();

		double[] totalCharge = new double[numberOfComponents];
		for(int i = 0; i < numberOfCharges; i++) {
			double[] chargeLocation = new double[transversalLocation.size()];
			for (int j = 0; j < transversalLocation.size(); j++) {
				chargeLocation[j] = transversalLocation.get(j) + rand.nextGaussian() * transversalWidth;
			}
			listOfChargeLocations.add(chargeLocation);
		}

		// Subtract a certain amount from each charge to make the whole distribution colorless.
		for(int i = 0; i < numberOfCharges; i++) {
			/*
			double[] amplitude = listOfChargeColorAmplitudes.get(i);
			for (int j = 0; j < numberOfComponents; j++) {
				amplitude[j] -= totalCharge[j] / numberOfCharges;
			}
			*/
			generator.addCharge(listOfChargeLocations.get(i), partonWidth);//For the spherical model the partonWidth should be identical to the transversalWidth!!!
		}

		return generator;
	}

	private double getMagnitude(double[] vector) {
		double magnitude = 0.0;
		for(int i = 0; i < vector.length; i++) {
			magnitude += Math.pow(vector[i], 2);
		}
		return Math.sqrt(magnitude);
	}


}
