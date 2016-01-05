package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.gl.EnergyDensity2DGLPanel;

public class YamlEnergyDensity2DGLPanel {

	// Scale properties
	public Double scaleFactor;
	public Boolean automaticScaling;

	// Coordinate properties
	public String showCoordinates;

	// ComboBox properties
	public String data;

	/** Empty constructor called by SnakeYaml */
	public YamlEnergyDensity2DGLPanel() {
	}

	public YamlEnergyDensity2DGLPanel(Component component) {
		if (component instanceof EnergyDensity2DGLPanel) {
			EnergyDensity2DGLPanel panel = (EnergyDensity2DGLPanel) component;
			scaleFactor = panel.scaleProperties.getScaleFactor();
			automaticScaling = panel.scaleProperties.getAutomaticScaling();
			showCoordinates = panel.showCoordinateProperties.getValue();
			data = panel.dataProperties.getStringFromEntry();
		}
	}

	public Component inflate(PanelManager panelManager) {

		EnergyDensity2DGLPanel panel = new EnergyDensity2DGLPanel(panelManager.getSimulationAnimation());

		if (scaleFactor != null) {
			panel.scaleProperties.setScaleFactor(scaleFactor);
		}

		if (automaticScaling != null) {
			panel.scaleProperties.setAutomaticScaling(automaticScaling);
		}

		if (showCoordinates != null) {
			panel.showCoordinateProperties.setValue(showCoordinates);
		}

		if (data != null) {
			panel.dataProperties.setEntryFromString(data);
		}
		return panel;
	}
}
