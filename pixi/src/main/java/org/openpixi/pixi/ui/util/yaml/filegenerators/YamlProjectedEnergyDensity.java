package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.ProjectedEnergyDensity;

/**
 * Created by dmueller on 10/20/15.
 */
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
	 * Option for colorful or grayscale plots
	 */
	public boolean colorful = true;


	public ProjectedEnergyDensity getFileGenerator() {
		ProjectedEnergyDensity generator = new ProjectedEnergyDensity(path, interval, direction, colorful);
		return generator;
	}
}
