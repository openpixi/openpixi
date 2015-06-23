package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.gl.EnergyDensity3DGLPanel;

public class YamlEnergyDensity3DGLPanel {

	// Scale properties
	public Double scaleFactor;
	public Boolean automaticScaling;

	// Projection properties
	public Double phi;
	public Double theta;

	/** Empty constructor called by SnakeYaml */
	public YamlEnergyDensity3DGLPanel() {
	}

	public YamlEnergyDensity3DGLPanel(Component component) {
		if (component instanceof EnergyDensity3DGLPanel) {
			EnergyDensity3DGLPanel panel = (EnergyDensity3DGLPanel) component;
			scaleFactor = panel.getScaleProperties().getScaleFactor();
			automaticScaling = panel.getScaleProperties().getAutomaticScaling();
			phi = panel.phi;
			theta = panel.theta;
		}
	}

	public Component inflate(PanelManager panelManager) {

		EnergyDensity3DGLPanel panel = new EnergyDensity3DGLPanel(panelManager.getSimulationAnimation());

		if (scaleFactor != null) {
			panel.getScaleProperties().setScaleFactor(scaleFactor);
		}

		if (automaticScaling != null) {
			panel.getScaleProperties().setAutomaticScaling(automaticScaling);
		}

		if (phi != null) {
			panel.phi = phi;
		}

		if (theta != null) {
			panel.theta = theta;
		}

		return panel;
	}
}
