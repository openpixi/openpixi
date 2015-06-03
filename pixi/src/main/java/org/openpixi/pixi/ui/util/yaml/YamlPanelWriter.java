package org.openpixi.pixi.ui.util.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class YamlPanelWriter {

	public String getYamlString(YamlPanels panels) {
		DumperOptions options = new DumperOptions();
		options.setIndent(2);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		PanelWrapper wrapper = new PanelWrapper(panels);
		String output = yaml.dump(wrapper);
		output = removeLines(output);
		System.out.println(output);
		return output;
	}

	/**
	 * Workaround to remove lines that only contain "xxx: null".
	 * @param string
	 * @return
	 */
	private String removeLines(String string) {
		String lines[] = string.split("\\r?\\n");
		String result = "";
		for (String line : lines) {
			if (line.endsWith(": null")) {
				// omit
			} else if (line.startsWith("!!org.openpixi")) {
				// omit
			} else {
				result = result + line + "\n";
			}
		}
		return result;
	}

	class PanelWrapper {
		public YamlPanels panels;

		public PanelWrapper(YamlPanels panels) {
			this.panels = panels;
		}
	}
}
