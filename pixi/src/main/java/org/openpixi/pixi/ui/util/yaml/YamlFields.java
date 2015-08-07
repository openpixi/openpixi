package org.openpixi.pixi.ui.util.yaml;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.ui.util.yaml.fieldgenerators.*;

import java.util.ArrayList;

public class YamlFields {

	/**
	 * List of SU2PlaneWaves.
	 */
	public ArrayList<YamlSU2PlaneWave> SU2PlaneWaves = new ArrayList<YamlSU2PlaneWave>();

	public ArrayList<YamlSUnPlanePulse> SUnPlanePulses = new ArrayList<YamlSUnPlanePulse>();

	public ArrayList<YamlSU2GaussianPulse> SU2GaussianPulses = new ArrayList<YamlSU2GaussianPulse>();

	public ArrayList<YamlSUnFocusedGaussianPulse> SUnFocusedGaussianPulses = new ArrayList<YamlSUnFocusedGaussianPulse>();

	/**
	 * Creates FieldGenerator instances and applies them to the Settings instance.
	 * @param s
	 */
	public void applyTo(Settings s) {
		for (YamlSU2PlaneWave wave : SU2PlaneWaves) {
			if (wave.checkConsistency(s)) {
				s.addFieldGenerator(wave.getFieldGenerator());
			}
		}

		for (YamlSUnPlanePulse pulse : SUnPlanePulses) {
			if (pulse.checkConsistency(s)) {
				s.addFieldGenerator(pulse.getFieldGenerator());
			}
		}

		for (YamlSU2GaussianPulse pulse : SU2GaussianPulses) {
			if (pulse.checkConsistency(s)) {
				s.addFieldGenerator(pulse.getFieldGenerator());
			}
		}
		for (YamlSUnFocusedGaussianPulse pulse : SUnFocusedGaussianPulses) {
			if (pulse.checkConsistency(s)) {
				s.addFieldGenerator(pulse.getFieldGenerator());
			}
		}
	}

}
