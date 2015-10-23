package org.openpixi.pixi.ui;

import java.util.prefs.Preferences;

/**
 * Class that contains all user preferences for this application.
 */
public class UserPreferences {
	/** Default path for storing and loading Yaml files */
	public final static String DEFAULT_YAML_PATH = "default_yaml_path";

	/**
	 * Returns user preferences for the OpenPixi package
	 */
	public static Preferences getUserPreferences() {
		Preferences preferences = Preferences.userNodeForPackage(org.openpixi.pixi.ui.MainControlApplet.class);
		return preferences;
	}
}
