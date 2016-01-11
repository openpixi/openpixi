package org.openpixi.pixi.ui.util.yaml.currentgenerators;

import org.openpixi.pixi.physics.fields.currentgenerators.DualMVModel;

public class YamlDualMVModel {
	/**
	 * Direction of the current pulse (0 to d)
	 */
	public Integer direction;

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
	 * Seeds to use for the random number generator
	 */
	public Integer randomSeed1 = null;
	public Integer randomSeed2 = null;

	/**
	 * Option for writing boost-invariant collision initial conditions to file.
	 */
	public Boolean createInitialConditionsOutput =  false;

	/**
	 * Path to the output file.
	 */
	public String outputFile = null;

	/**
	 * Option whether to use the \mu^2 (true) or the g^2 \mu^2 (false, default) normalization for the Gaussian
	 * probability distribution of the color charge densities.
	 */
	public Boolean useAlternativeNormalization = false;


	public DualMVModel getCurrentGenerator() {
		if(randomSeed1 != null && randomSeed2 != null) {
			return new DualMVModel(direction, longitudinalLocation, longitudinalWidth, mu, lowPassCoefficient, true,
					randomSeed1, randomSeed2, createInitialConditionsOutput, outputFile, useAlternativeNormalization);
		}
		return new DualMVModel(direction, longitudinalLocation, longitudinalWidth, mu, lowPassCoefficient, false, 0, 0,
				createInitialConditionsOutput, outputFile, useAlternativeNormalization);
	}

}
