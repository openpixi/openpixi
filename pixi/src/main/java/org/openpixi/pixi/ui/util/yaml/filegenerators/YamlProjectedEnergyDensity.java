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


	public ProjectedEnergyDensity getFileGenerator() {
		ProjectedEnergyDensity generator = new ProjectedEnergyDensity(path, interval, direction);
		return generator;
	}
}
