package org.openpixi.pixi.ui.util.yaml;

import org.openpixi.pixi.physics.Settings;

/**
 * Generic settings class into which the YAML parser parses
 */
public class YamlSettings {
	public Double simulationWidth;
	public Double simulationHeight;
	public Integer numberOfParticles;
	
	public void applyTo(Settings settings) {
		if (simulationWidth != null) {
			settings.setSimulationWidth(simulationWidth);
		}

		if (simulationHeight != null) {
			settings.setSimulationHeight(simulationHeight);
		}

		if (numberOfParticles != null) {
			settings.setNumOfParticles(numberOfParticles);
		}
	}
}
