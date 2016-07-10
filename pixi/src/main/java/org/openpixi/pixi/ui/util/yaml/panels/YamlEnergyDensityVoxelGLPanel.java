package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.gl.EnergyDensityVoxelGLPanel;

public class YamlEnergyDensityVoxelGLPanel {

	// Scale properties
	public Double scaleFactor;
	public Boolean automaticScaling;

	// Projection properties
	public Double phi;
	public Double theta;

	/** Distance of viewer */
	public Double distanceFactor;

	/** Maximum height of values */
	public Double heightFactor;

	/** Empty constructor called by SnakeYaml */
	public YamlEnergyDensityVoxelGLPanel() {
	}

	public YamlEnergyDensityVoxelGLPanel(Component component) {
		if (component instanceof EnergyDensityVoxelGLPanel) {
			EnergyDensityVoxelGLPanel panel = (EnergyDensityVoxelGLPanel) component;
			scaleFactor = panel.scaleProperties.getScaleFactor();
			automaticScaling = panel.scaleProperties.getAutomaticScaling();
			phi = panel.phi;
			theta = panel.theta;
			distanceFactor = panel.distanceFactor;
			heightFactor = panel.heightFactor;
		}
	}

	public Component inflate(PanelManager panelManager) {

		EnergyDensityVoxelGLPanel panel = new EnergyDensityVoxelGLPanel(panelManager.getSimulationAnimation());

		if (scaleFactor != null) {
			panel.scaleProperties.setScaleFactor(scaleFactor);
		}

		if (automaticScaling != null) {
			panel.scaleProperties.setAutomaticScaling(automaticScaling);
		}

		if (phi != null) {
			panel.phi = phi;
		}

		if (theta != null) {
			panel.theta = theta;
		}

		if (distanceFactor != null) {
			panel.distanceFactor = distanceFactor;
		}

		if (heightFactor != null) {
			panel.heightFactor = heightFactor;
		}

		return panel;
	}
}
