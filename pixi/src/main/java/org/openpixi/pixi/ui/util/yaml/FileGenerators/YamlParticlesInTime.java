package org.openpixi.pixi.ui.util.yaml.FileGenerators;

import org.openpixi.pixi.diagnostics.methods.ParticlesInTime;

/**
 * Yaml wrapper for the YamlParticlesInTime FileGenerator.
 */
public class YamlParticlesInTime {

	/**
	 * File name.
	 */
	public String path;

	/**
	 * Measurement interval.
	 */
	public double interval;


	/**
	 * Returns an instance of ParticlesInTime according to the parameters in the YAML file.
	 *
	 * @return Instance of ParticlesInTime.
	 */
	public ParticlesInTime getFileGenerator() {
		ParticlesInTime fileGen = new ParticlesInTime(path, interval);
		return fileGen;
	}
}
