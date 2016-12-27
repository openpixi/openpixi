package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.EnergyDensity;

public class YamlEnergyDensity {

	/**
	 * File name.
	 */
	public String path;

	/**
	 * Measurement time.
	 */
	public double timeInstant;


	public EnergyDensity getFileGenerator() {
		EnergyDensity generator = new EnergyDensity(path, timeInstant);
		return generator;
	}
}
