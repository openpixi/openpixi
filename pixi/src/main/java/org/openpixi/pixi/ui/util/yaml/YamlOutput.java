package org.openpixi.pixi.ui.util.yaml;

import java.util.ArrayList;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.ui.util.yaml.filegenerators.YamlParticlesInTime;
import org.openpixi.pixi.ui.util.yaml.filegenerators.YamlBulkQuantitiesInTime;

public class YamlOutput {
	/**
	 * List of output file generators.
	 */
	public ArrayList<YamlParticlesInTime> particlesInTime = new ArrayList<YamlParticlesInTime>();
	
	public ArrayList<YamlBulkQuantitiesInTime> bulkQuantitiesInTime = new ArrayList<YamlBulkQuantitiesInTime>();

	/**
	 * Creates FileGenerator instances and applies them to the Settings instance.
	 * @param s
	 */

	public void applyTo(Settings s) {
		for (YamlParticlesInTime output1 : particlesInTime) {
			s.addDiagnostics(output1.getFileGenerator());
		}

		for (YamlBulkQuantitiesInTime output2 : bulkQuantitiesInTime) {
			s.addDiagnostics(output2.getFileGenerator());
		}
	}
	
}
