package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.ProjectedEnergyDensity;

public class YamlProjectedEnergyDensity {

	/**
	 * File name.
	 */
	public String path;

	/**
	 * Measurement interval.
	 */
	public double interval;

	/**
	 * Direction left out of projection
	 */
	public int direction;

	/**
	 * Option whether to compute energy density components.
     */
	public Boolean computeEnergyDensity = true;

	/**
	 * Option whether to compute the longitudinal Poynting vector.
     */
	public Boolean computePoyntingVector = true;


	public ProjectedEnergyDensity getFileGenerator() {
		ProjectedEnergyDensity generator = new ProjectedEnergyDensity(path, interval, direction);
		generator.computeEnergyDensity = computeEnergyDensity;
		generator.computePoyntingVector = computePoyntingVector;
		return generator;
	}
}
