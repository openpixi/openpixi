package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.InfoPanel;

public class YamlInfoPanel {

	/** Empty constructor called by SnakeYaml */
	public YamlInfoPanel() {
	}

	public YamlInfoPanel(Component component) {
		if (component instanceof InfoPanel) {
			InfoPanel panel = (InfoPanel) component;
		}
	}

	public Component inflate(PanelManager panelManager) {
		InfoPanel panel = new InfoPanel(panelManager.getSimulationAnimation());
		return panel;
	}
}
