package org.openpixi.pixi.ui.util.yaml.currentgenerators;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.openpixi.pixi.physics.fields.currentgenerators.NewLorenzLCCurrent;

import java.util.ArrayList;
import java.util.Random;

public class YamlRandomLorenzColorCurrent {
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


	public NewLorenzLCCurrent getCurrentGenerator() {
		NewLorenzLCCurrent generator = new NewLorenzLCCurrent(direction, orientation, longitudinalLocation, longitudinalWidth);
		Random rand = new Random();
		if(randomSeed != null) {
			rand.setSeed(randomSeed);
		}

		int numberOfComponents = numberOfColors * numberOfColors - 1;
		if(numberOfColors == 1) {
			numberOfComponents = 1;
		}

		double[] totalCharge = new double[numberOfComponents];
		for(int i = 0; i < numberOfCharges - 1; i++) {
			double[] chargeLocation = new double[transversalLocation.size()];
			double[] chargeColorDirection = new double[numberOfComponents];
			for (int j = 0; j < transversalLocation.size(); j++) {
				chargeLocation[j] = transversalLocation.get(j) + rand.nextGaussian() * transversalWidth;
			}
			double chargeMagnitude = 0.0;
			for (int j = 0; j < numberOfComponents; j++) {
				chargeColorDirection[j] = rand.nextGaussian() * colorDistributionWidth;
				totalCharge[j] += chargeColorDirection[j];
				chargeMagnitude += Math.pow(chargeColorDirection[j], 2);
			}
			chargeMagnitude = Math.sqrt(chargeMagnitude);
			generator.addCharge(chargeLocation, chargeColorDirection, chargeMagnitude);
		}

		// Add last charge to make the charge distribution colorless.
		double[] chargeLocation = new double[transversalLocation.size()];
		double[] chargeColorDirection = new double[numberOfComponents];
		for (int j = 0; j < transversalLocation.size(); j++) {
			chargeLocation[j] = transversalLocation.get(j) + rand.nextGaussian() * longitudinalWidth;
		}
		double totalChargeMagnitude = 0.0;
		for (int j = 0; j < numberOfComponents; j++) {
			totalChargeMagnitude += Math.pow(totalCharge[j], 2);
		}
		totalChargeMagnitude = -Math.sqrt(totalChargeMagnitude);
		generator.addCharge(chargeLocation, totalCharge, totalChargeMagnitude);

		return generator;
	}


}
