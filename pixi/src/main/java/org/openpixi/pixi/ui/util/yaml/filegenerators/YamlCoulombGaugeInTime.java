package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.CoulombGaugeInTime;

/**
 * Yaml wrapper for the YamlParticlesInTime FileGenerator.
 */
public class YamlCoulombGaugeInTime {

	/**
	 * Measurement interval.
	 */
	public double interval;

	/**
	 * Measurement interval offset.
	 */
	public double offset;

	/**
	 * Returns an instance of CoulombGaugeInTime according to the parameters in the YAML file.
	 *
	 * @return Instance of BulkQuantitiesInTime.
	 */
	public CoulombGaugeInTime getFileGenerator() {
		CoulombGaugeInTime fileGen = new CoulombGaugeInTime(interval, offset);
		return fileGen;
	}
}
