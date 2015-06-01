package org.openpixi.pixi.ui.util.yaml.FileGenerators;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.diagnostics.methods.ParticlesInTime;

import java.util.List;

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
		ParticlesInTime fileGen = new ParticlesInTime();

        fileGen.setPath(path);
        
        fileGen.setInterval(interval);

		return fileGen;
	}
}
