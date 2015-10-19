package org.openpixi.pixi.ui.util.yaml.currentgenerators;

import org.openpixi.pixi.physics.fields.currentgenerators.PointChargeLCCurrent;

import java.util.ArrayList;
import java.util.Random;

public class YamlRandomTemporalParticleColorCurrent {
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
	 * Width of the Gaussian distribution from which the random charges are sampled
	 */
	public Double colorDistributionWidth;

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


	//public NewLCCurrent getCurrentGenerator() {
	public PointChargeLCCurrent getCurrentGenerator() {
		//NewLCCurrent generator = new NewLCCurrent(direction, orientation, longitudinalLocation, longitudinalWidth);
		PointChargeLCCurrent generator = new PointChargeLCCurrent(direction, orientation, longitudinalLocation, longitudinalWidth, true, useDipoleRemoval);
		Random rand = new Random();
		if(randomSeed != null) {
			rand.setSeed(randomSeed);
		}

		int numberOfComponents = numberOfColors * numberOfColors - 1;
		if(numberOfColors == 1) {
			numberOfComponents = 1;
		}

		ArrayList<double[]> listOfChargeLocations = new ArrayList<double[]>();
		ArrayList<double[]> listOfChargeColorAmplitudes = new ArrayList<double[]>();

		double[] totalCharge = new double[numberOfComponents];
		for(int i = 0; i < numberOfCharges; i++) {
			double[] chargeLocation = new double[transversalLocation.size()];
			double[] chargeColorAmplitude = new double[numberOfComponents];
			for (int j = 0; j < transversalLocation.size(); j++) {
				chargeLocation[j] = transversalLocation.get(j) + rand.nextGaussian() * transversalWidth;
			}
			for (int j = 0; j < numberOfComponents; j++) {
				chargeColorAmplitude[j] = rand.nextGaussian() * colorDistributionWidth;
				totalCharge[j] += chargeColorAmplitude[j];
			}
			listOfChargeLocations.add(chargeLocation);
			listOfChargeColorAmplitudes.add(chargeColorAmplitude);
		}

		// Subtract a certain amount from each charge to make the whole distribution colorless.
		for(int i = 0; i < numberOfCharges; i++) {
			double[] amplitude = listOfChargeColorAmplitudes.get(i);
			for (int j = 0; j < numberOfComponents; j++) {
				amplitude[j] -= totalCharge[j] / numberOfCharges;
			}

			generator.addCharge(listOfChargeLocations.get(i), amplitude, getMagnitude(amplitude));
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
