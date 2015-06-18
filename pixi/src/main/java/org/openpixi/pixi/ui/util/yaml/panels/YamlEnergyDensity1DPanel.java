package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.EnergyDensity1DPanel;

public class YamlEnergyDensity1DPanel {

	// Scale properties
	public Double scaleFactor;
	public Boolean automaticScaling;

	/** Empty constructor called by SnakeYaml */
	public YamlEnergyDensity1DPanel() {
	}

	public YamlEnergyDensity1DPanel(Component component) {
		if (component instanceof EnergyDensity1DPanel) {
			EnergyDensity1DPanel panel = (EnergyDensity1DPanel) component;
			scaleFactor = panel.getScaleProperties().getScaleFactor();
			automaticScaling = panel.getScaleProperties().getAutomaticScaling();
		}
	}

	public Component inflate(PanelManager panelManager) {

		EnergyDensity1DPanel panel = new EnergyDensity1DPanel(panelManager.getSimulationAnimation());

		if (scaleFactor != null) {
			panel.getScaleProperties().setScaleFactor(scaleFactor);
		}

		if (automaticScaling != null) {
			panel.getScaleProperties().setAutomaticScaling(automaticScaling);
		}
		return panel;
	}
}
