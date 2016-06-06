package org.openpixi.pixi.ui.util.yaml;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.ui.util.yaml.initial.YamlCGC;

public class YamlInitialConditions {
	public YamlCGC CGC;

	/**
	 * Creates IInitialCondition instances and applies them to the Settings instance.
	 * @param s
	 */
	public void applyTo(Settings s) {
		CGC.applyTo(s);
	}
}
