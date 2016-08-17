package org.openpixi.pixi.ui.util.yaml;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.ui.util.yaml.filegenerators.*;

public class YamlOutput {
	/**
	 * List of output file generators.
	 */
	public ArrayList<YamlParticlesInTime> particlesInTime = new ArrayList<YamlParticlesInTime>();
	
	public ArrayList<YamlBulkQuantitiesInTime> bulkQuantitiesInTime = new ArrayList<YamlBulkQuantitiesInTime>();

	public ArrayList<YamlCoulombGaugeInTime> coulombGaugeInTime = new ArrayList<YamlCoulombGaugeInTime>();

	public ArrayList<YamlRandomGaugeInTime> randomGaugeInTime = new ArrayList<YamlRandomGaugeInTime>();

	public ArrayList<YamlOccupationNumbersInTime> occupationNumbersInTime = new ArrayList<YamlOccupationNumbersInTime>();

	public ArrayList<YamlScreenshotInTime> screenshotInTime = new ArrayList<YamlScreenshotInTime>();

	public ArrayList<YamlUnitarityTester> unitarityTester = new ArrayList<YamlUnitarityTester>();

	public ArrayList<YamlGaussConstraintRestoration> gaussConstraintRestoration = new ArrayList<YamlGaussConstraintRestoration>();

	public ArrayList<YamlBoundaryFixer> boundaryFixer = new ArrayList<YamlBoundaryFixer>();

	public ArrayList<YamlProjectedEnergyDensity> projectedEnergyDensity = new ArrayList<YamlProjectedEnergyDensity>();

	public ArrayList<YamlPlanarFields> planarFields = new ArrayList<YamlPlanarFields>();

	public ArrayList<YamlPoyntingTheoremInTime> poyntingTheoremInTime = new ArrayList<YamlPoyntingTheoremInTime>();

	public ArrayList<YamlTimeMeasurement> timeMeasurement = new ArrayList<YamlTimeMeasurement>();

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

		for (YamlOccupationNumbersInTime output : occupationNumbersInTime) {
			s.addDiagnostics(output.getFileGenerator());
		}

		for (YamlScreenshotInTime output : screenshotInTime) {
			s.addDiagnostics(output.getFileGenerator());
		}

		for (YamlUnitarityTester output : unitarityTester) {
			s.addDiagnostics(output.getFileGenerator());
		}

		for (YamlGaussConstraintRestoration output : gaussConstraintRestoration) {
			s.addDiagnostics(output.getFileGenerator());
		}

		for(YamlBoundaryFixer output : boundaryFixer) {
			s.addDiagnostics(output.getFileGenerator());
		}

		for(YamlProjectedEnergyDensity output : projectedEnergyDensity) {
			s.addDiagnostics(output.getFileGenerator());
		}

		for(YamlPlanarFields output : planarFields) {
			s.addDiagnostics(output.getFileGenerator());
		}

		for(YamlPoyntingTheoremInTime output : poyntingTheoremInTime) {
			s.addDiagnostics(output.getFileGenerator());
		}

		for(YamlTimeMeasurement output : timeMeasurement) {
			s.addDiagnostics(output.getFileGenerator());
		}
	}
	
}
