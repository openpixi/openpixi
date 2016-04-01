package org.openpixi.pixi.ui.util.yaml.filegenerators;

import org.openpixi.pixi.diagnostics.methods.PoyntingTheoremInTime;

import java.util.ArrayList;

/**
 * Yaml wrapper for the YamlParticlesInTime FileGenerator.
 */
public class YamlPoyntingTheoremInTime {

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
	public PoyntingTheoremInTime getFileGenerator() {
		PoyntingTheoremInTime fileGen = new PoyntingTheoremInTime(path, interval);
		return fileGen;
	}
}