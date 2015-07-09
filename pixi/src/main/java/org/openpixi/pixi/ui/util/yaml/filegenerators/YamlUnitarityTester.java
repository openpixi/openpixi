package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.UnitarityTester;

/**
 * Yaml wrapper for the YamlParticlesInTime FileGenerator.
 */
public class YamlUnitarityTester {
	/**
	 * Measurement interval.
	 */
	public double interval;


	/**
	 * Returns an instance of BulkQuantitiesInTime according to the parameters in the YAML file.
	 *
	 * @return Instance of BulkQuantitiesInTime.
	 */
	public UnitarityTester getFileGenerator() {
		UnitarityTester fileGen = new UnitarityTester(interval);
		return fileGen;
	}
}