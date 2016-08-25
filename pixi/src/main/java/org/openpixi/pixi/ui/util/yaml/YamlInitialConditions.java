package org.openpixi.pixi.ui.util.yaml;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.ui.util.yaml.initial.YamlCGC;
import org.openpixi.pixi.ui.util.yaml.initial.YamlYM;

public class YamlInitialConditions {
	public YamlCGC CGC;
	public YamlYM YM;

	/**
	 * Creates IInitialCondition instances and applies them to the Settings instance.
	 * @param s
	 */
	public void applyTo(Settings s) {
		if(CGC != null) {
			CGC.applyTo(s);
		}

		if(YM != null) {
			YM.applyTo(s);
		}
	}
}
