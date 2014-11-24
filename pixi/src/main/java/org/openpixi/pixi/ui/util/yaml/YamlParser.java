package org.openpixi.pixi.ui.util.yaml;

import org.openpixi.pixi.physics.Settings;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlParser {

	Settings settings;

	public YamlParser(Settings settings) {
		this.settings = settings;
	}

	public void parseString (String string) {
		Yaml yaml = new Yaml(new Constructor(YamlSettings.class));
		YamlSettings yamlSettings = (YamlSettings) yaml.load(string);
		yamlSettings.applyTo(settings);
	}

}
