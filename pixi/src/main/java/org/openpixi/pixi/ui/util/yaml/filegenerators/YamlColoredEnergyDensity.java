package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.ColoredEnergyDensity;

public class YamlColoredEnergyDensity {

	/**
	 * File name.
	 */
	public String path;

	/**
	 * Measurement time.
	 */
	public double timeInstant;


	public ColoredEnergyDensity getFileGenerator() {
		ColoredEnergyDensity generator = new ColoredEnergyDensity(path, timeInstant);
		return generator;
	}
}
