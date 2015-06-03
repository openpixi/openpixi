package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.PhaseSpacePanel;

public class YamlPhaseSpacePanel {

	// Scale properties
	public Double scaleFactor;
	public Boolean automaticScaling;

	/** Empty constructor called by SnakeYaml */
	public YamlPhaseSpacePanel() {
	}

	public YamlPhaseSpacePanel(Component component) {
		if (component instanceof PhaseSpacePanel) {
			PhaseSpacePanel panel = (PhaseSpacePanel) component;
			scaleFactor = panel.getScaleProperties().getScaleFactor();
			automaticScaling = panel.getScaleProperties().getAutomaticScaling();
		}
	}

	public Component inflate(PanelManager panelManager) {

		PhaseSpacePanel panel = new PhaseSpacePanel(panelManager.getSimulationAnimation());


		if (scaleFactor != null) {
			panel.getScaleProperties().setScaleFactor(scaleFactor);
		}

		if (automaticScaling != null) {
			panel.getScaleProperties().setAutomaticScaling(automaticScaling);
		}

		return panel;
	}
}
