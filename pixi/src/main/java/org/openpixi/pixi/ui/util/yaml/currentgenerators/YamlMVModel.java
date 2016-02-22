package org.openpixi.pixi.ui.util.yaml.currentgenerators;

import org.openpixi.pixi.physics.fields.currentgenerators.MVModel;

public class YamlMVModel {
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
	 * Longitudinal width of the pulse (Gauss shape)
	 */
	public Double longitudinalWidth;

	/**
	 * \mu parameter of the MV model. This controls the average charge density squared.
	 */
	public Double mu;

	/**
	 * Coefficient for the low pass filter in the Poisson solver
	 */
	public Double lowPassCoefficient = 1.0;

	/**
	 * Coefficient infrared regulator in the Poisson solver
	 */
	public Double infraredCoefficient = 0.0;

	/**
	 * Seed to use for the random number generator
	 */
	public Integer randomSeed;

	/**
	 * Option whether to use the \mu^2 (true) or the g^2 \mu^2 (false, default) normalization for the Gaussian
	 * probability distribution of the color charge densities.
	 */
	public Boolean useAlternativeNormalization = false;


	public MVModel getCurrentGenerator() {
		boolean useSeed = (randomSeed != null);
		if(!useSeed) {
			randomSeed = 0;
		}
		return new MVModel(direction, orientation, longitudinalLocation, longitudinalWidth, mu, useSeed, randomSeed,
				lowPassCoefficient, infraredCoefficient, useAlternativeNormalization);
	}

}
