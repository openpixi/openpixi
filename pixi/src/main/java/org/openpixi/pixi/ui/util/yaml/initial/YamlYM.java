package org.openpixi.pixi.ui.util.yaml.initial;

import org.openpixi.pixi.physics.Settings;

import java.util.ArrayList;

public class YamlYM {


	public ArrayList<YamlSU2PlaneWave> SU2PlaneWaves = new ArrayList<YamlSU2PlaneWave>();

	public ArrayList<YamlSU2PlanePulse> SU2PlanePulses = new ArrayList<YamlSU2PlanePulse>();

	public void applyTo(Settings s) {
		for (YamlSU2PlaneWave wave : SU2PlaneWaves) {
			if (wave.checkConsistency(s)) {
				s.addInitialConditions(wave.getFieldGenerator());
			}
		}

		for (YamlSU2PlanePulse pulse : SU2PlanePulses) {
			if (pulse.checkConsistency(s)) {
				s.addInitialConditions(pulse.getFieldGenerator());
			}
		}
	}
}
