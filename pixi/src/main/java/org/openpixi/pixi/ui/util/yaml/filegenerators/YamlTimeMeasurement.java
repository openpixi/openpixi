package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.TimeMeasurement;

/**
 * Yaml wrapper for the YamlParticlesInTime FileGenerator.
 */
public class YamlTimeMeasurement {

	/**
	 * File name.
	 */
	public String path;

	/**
	 * Measurement interval.
	 */
	public double interval;


	/**
	 * Returns an instance of BulkQuantitiesInTime according to the parameters in the YAML file.
	 *
	 * @return Instance of BulkQuantitiesInTime.
	 */
	public TimeMeasurement getFileGenerator() {
		TimeMeasurement fileGen = new TimeMeasurement(path, interval);
		return fileGen;
	}
}