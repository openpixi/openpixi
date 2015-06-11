package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.BulkQuantitiesInTime;

/**
 * Yaml wrapper for the YamlParticlesInTime FileGenerator.
 */
public class YamlBulkQuantitiesInTime {

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
	public BulkQuantitiesInTime getFileGenerator() {
		BulkQuantitiesInTime fileGen = new BulkQuantitiesInTime(path, interval);
		return fileGen;
	}
}
