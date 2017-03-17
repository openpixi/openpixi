package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.EnergyDensity;

public class YamlEnergyDensity {

	/**
	 * File name.
	 */
	public String path;

	/**
	 * Simulation time for first output.
	 */
	public double startTime;

	/**
	 * Simulation time interval between outputs.
	 */
	public double timeInterval;


	public EnergyDensity getFileGenerator() {
		EnergyDensity generator = new EnergyDensity(path, startTime, timeInterval);
		return generator;
	}
}
