package org.openpixi.pixi.ui.util.yaml.initial;

import org.openpixi.pixi.physics.Settings;

import java.util.ArrayList;

public class YamlCGC {
	public ArrayList<YamlMVModel> MVModel = new ArrayList<YamlMVModel>();
	public ArrayList<YamlMVModelCoherent> MVModelCoherent = new ArrayList<YamlMVModelCoherent>();
	public ArrayList<YamlNucleusCoherent> NucleusCoherent = new ArrayList<YamlNucleusCoherent>();
	public ArrayList<YamlNucleus> Nucleus = new ArrayList<YamlNucleus>();


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

		for (YamlNucleusCoherent init : NucleusCoherent) {
			s.addInitialConditions(init.getInitialCondition());
		}

		for (YamlNucleus init : Nucleus) {
			s.addInitialConditions(init.getInitialCondition());
		}
	}
}
