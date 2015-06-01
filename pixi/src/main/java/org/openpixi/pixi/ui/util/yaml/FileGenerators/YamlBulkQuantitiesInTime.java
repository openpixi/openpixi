package org.openpixi.pixi.ui.util.yaml.FileGenerators;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.diagnostics.methods.BulkQuantitiesInTime;

import java.util.List;

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
		BulkQuantitiesInTime fileGen = new BulkQuantitiesInTime();

        fileGen.setPath(path);
        
        fileGen.setInterval(interval);

		return fileGen;
	}
}
