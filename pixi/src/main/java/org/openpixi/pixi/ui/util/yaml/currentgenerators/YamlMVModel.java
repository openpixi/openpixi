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
	 * Seed to use for the random number generator
	 */
	public Integer randomSeed;


	public MVModel getCurrentGenerator() {
		if(randomSeed != null) {
			return new MVModel(direction, orientation, longitudinalLocation, longitudinalWidth, mu, randomSeed, lowPassCoefficient);
		}
		return new MVModel(direction, orientation, longitudinalLocation, longitudinalWidth, mu, lowPassCoefficient);
	}

}
