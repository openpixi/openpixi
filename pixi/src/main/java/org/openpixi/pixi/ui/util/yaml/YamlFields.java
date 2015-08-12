package org.openpixi.pixi.ui.util.yaml;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.ui.util.yaml.fieldgenerators.*;

import java.util.ArrayList;

public class YamlFields {

	/**
	 * List of SU2PlaneWaves.
	 */
	public ArrayList<YamlSU2PlaneWave> SU2PlaneWaves = new ArrayList<YamlSU2PlaneWave>();

	public ArrayList<YamlSU2PlanePulse> SU2PlanePulses = new ArrayList<YamlSU2PlanePulse>();

	public ArrayList<YamlSU2GaussianPulse> SU2GaussianPulses = new ArrayList<YamlSU2GaussianPulse>();

	public ArrayList<YamlSU2FocusedGaussianPulse> SU2FocusedGaussianPulses = new ArrayList<YamlSU2FocusedGaussianPulse>();

	public ArrayList<YamlSUNFocusedGaussianPulse> SUNFocusedGaussianPulses = new ArrayList<YamlSUNFocusedGaussianPulse>();

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

		for (YamlSU2PlanePulse pulse : SU2PlanePulses) {
			if (pulse.checkConsistency(s)) {
				s.addFieldGenerator(pulse.getFieldGenerator());
			}
		}

		for (YamlSU2GaussianPulse pulse : SU2GaussianPulses) {
			if (pulse.checkConsistency(s)) {
				s.addFieldGenerator(pulse.getFieldGenerator());
			}
		}
		for (YamlSU2FocusedGaussianPulse pulse : SU2FocusedGaussianPulses) {
			if (pulse.checkConsistency(s)) {
				s.addFieldGenerator(pulse.getFieldGenerator());
			}
		}
		for (YamlSUNFocusedGaussianPulse pulse : SUNFocusedGaussianPulses) {
			if (pulse.checkConsistency(s)) {
				s.addFieldGenerator(pulse.getFieldGenerator());
			}
		}
	}

}
