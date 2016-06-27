package org.openpixi.pixi.ui.util.yaml.initial;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.ui.util.yaml.fieldgenerators.YamlSU2PlaneWave;

import java.util.ArrayList;

public class YamlCGC {
	public ArrayList<YamlMVModel> MVModel = new ArrayList<YamlMVModel>();
	public ArrayList<YamlMVModelCoherent> MVModelCoherent = new ArrayList<YamlMVModelCoherent>();


	/**
	 * Creates IInitialCondition instances and applies them to the Settings instance.
	 * @param s
	 */
	public void applyTo(Settings s) {
		for (YamlMVModel init : MVModel) {
			s.addInitialConditions(init.getInitialCondition());
		}

		for (YamlMVModelCoherent init : MVModelCoherent) {
			s.addInitialConditions(init.getInitialCondition());
		}
	}
}
