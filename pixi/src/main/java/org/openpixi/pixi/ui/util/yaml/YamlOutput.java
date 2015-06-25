package org.openpixi.pixi.ui.util.yaml;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.ui.util.yaml.filegenerators.YamlCoulombGaugeInTime;
import org.openpixi.pixi.ui.util.yaml.filegenerators.YamlParticlesInTime;
import org.openpixi.pixi.ui.util.yaml.filegenerators.YamlBulkQuantitiesInTime;
import org.openpixi.pixi.ui.util.yaml.filegenerators.YamlRandomGaugeInTime;
import org.openpixi.pixi.ui.util.yaml.filegenerators.YamlScreenshotInTime;

public class YamlOutput {
	/**
	 * List of output file generators.
	 */
	public ArrayList<YamlParticlesInTime> particlesInTime = new ArrayList<YamlParticlesInTime>();
	
	public ArrayList<YamlBulkQuantitiesInTime> bulkQuantitiesInTime = new ArrayList<YamlBulkQuantitiesInTime>();

	public ArrayList<YamlCoulombGaugeInTime> coulombGaugeInTime = new ArrayList<YamlCoulombGaugeInTime>();

	public ArrayList<YamlRandomGaugeInTime> randomGaugeInTime = new ArrayList<YamlRandomGaugeInTime>();

	public ArrayList<YamlScreenshotInTime> screenshotInTime = new ArrayList<YamlScreenshotInTime>();

	/**
	 * Creates FileGenerator instances and applies them to the Settings instance.
	 * @param s
	 */

	public void applyTo(Settings s) {
		for (YamlParticlesInTime output : particlesInTime) {
			s.addDiagnostics(output.getFileGenerator());
		}

		for (YamlBulkQuantitiesInTime output : bulkQuantitiesInTime) {
			s.addDiagnostics(output.getFileGenerator());
		}

		for (YamlCoulombGaugeInTime output : coulombGaugeInTime) {
			s.addDiagnostics(output.getFileGenerator());
		}

		for (YamlRandomGaugeInTime output : randomGaugeInTime) {
			s.addDiagnostics(output.getFileGenerator());
		}

		for (YamlScreenshotInTime output : screenshotInTime) {
			s.addDiagnostics(output.getFileGenerator());
		}
	}
	
}
