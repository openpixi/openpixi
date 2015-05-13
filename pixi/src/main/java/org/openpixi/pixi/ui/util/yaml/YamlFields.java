package org.openpixi.pixi.ui.util.yaml;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.ui.util.yaml.FieldGenerators.*;

import java.util.ArrayList;

public class YamlFields {

	/**
	 * List of SU2PlaneWaves.
	 */
	public ArrayList<YamlSU2PlaneWave> SU2PlaneWaves = new ArrayList<YamlSU2PlaneWave>();

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
	}

}
