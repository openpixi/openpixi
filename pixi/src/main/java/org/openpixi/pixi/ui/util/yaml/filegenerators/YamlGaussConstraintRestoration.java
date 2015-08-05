package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.GaussConstraintRestoration;

/**
 * Yaml wrapper for the GaussConstraintRestoration algorithm.
 */
public class YamlGaussConstraintRestoration {

	/**
	 * Measurement interval.
	 */
	public double interval = 1.0;

	/**
	 * Measurement interval offset.
	 */
	public double offset = 0.0;

	/**
	 * Parameter for the restoration algorithm. Should be tuned of optimality.
	 */
	public double gamma = 0.25;

	/**
	 * Maximum number of iterations done by the algorithm.
	 */
	public int maxIterations = 100;

	/**
	 * Absolute value goal for the restoration algorithm. If the desired absolute value is reached the iteration stops.
	 */
	public double absoluteValue = 10-8;

	/**
	 * Option to apply the iteration only once at the time defined by time offset.
	 */
	public boolean applyOnlyOnce = false;

	/**
	 * Returns an instance of GaussConstraintRestoration according to the parameters in the YAML file.
	 *
	 * @return instance of GaussConstraintRestoration.
	 */
	public GaussConstraintRestoration getFileGenerator() {
		GaussConstraintRestoration fileGen = new GaussConstraintRestoration(interval, offset, gamma, maxIterations, absoluteValue, applyOnlyOnce);
		return fileGen;
	}
}
