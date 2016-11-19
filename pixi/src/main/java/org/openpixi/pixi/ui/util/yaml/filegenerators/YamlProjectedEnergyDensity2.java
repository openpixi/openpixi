package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.ProjectedEnergyDensity2;

public class YamlProjectedEnergyDensity2 {

	/**
	 * File name.
	 */
	public String path;

	/**
	 * Measurement interval.
	 */
	public double interval;


	public ProjectedEnergyDensity2 getFileGenerator() {
		ProjectedEnergyDensity2 generator = new ProjectedEnergyDensity2(path, interval);
		return generator;
	}
}
