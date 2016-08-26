package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.gl.EnergyDensityVoxelGLPanel;

public class YamlEnergyDensityVoxelGLPanel {

	// ComboBox properties
	public String data;
	public String direction;

	// Scale properties
	public Double scaleFactor;
	public Boolean automaticScaling;

	// Double properties
	public Double visibilityThreshold;
	public Double opacity;

	// Boolean properties
	public Boolean showSimulationBox;
	public Boolean whiteBackground;
	public Boolean unequalScaling;

	// Projection properties
	public Double phi;
	public Double theta;
	public Double centerx;
	public Double centery;
	public Double centerz;

	/** Distance of viewer */
	public Double distanceFactor;

	/** Empty constructor called by SnakeYaml */
	public YamlEnergyDensityVoxelGLPanel() {
	}

	public YamlEnergyDensityVoxelGLPanel(Component component) {
		if (component instanceof EnergyDensityVoxelGLPanel) {
			EnergyDensityVoxelGLPanel panel = (EnergyDensityVoxelGLPanel) component;
			data = panel.dataProperties.getStringFromEntry();
			direction = panel.directionProperties.getStringFromEntry();
			scaleFactor = panel.scaleProperties.getScaleFactor();
			automaticScaling = panel.scaleProperties.getAutomaticScaling();
			visibilityThreshold = panel.visibilityThresholdProperties.getValue();
			opacity = panel.opacityProperties.getValue();
			showSimulationBox = panel.showSimulationBoxProperties.getValue();
			whiteBackground = panel.whiteBackgroundProperties.getValue();
			unequalScaling = panel.unequalScalingProperties.getValue();
			phi = panel.phi;
			theta = panel.theta;
			centerx = panel.centerx;
			centery = panel.centery;
			centerz = panel.centerz;
			distanceFactor = panel.distanceFactor;
		}
	}

	public Component inflate(PanelManager panelManager) {

		EnergyDensityVoxelGLPanel panel = new EnergyDensityVoxelGLPanel(panelManager.getSimulationAnimation());

		if (data != null) {
			panel.dataProperties.setEntryFromString(data);
		}

		if (direction != null) {
			panel.directionProperties.setEntryFromString(direction);
		}

		if (scaleFactor != null) {
			panel.scaleProperties.setScaleFactor(scaleFactor);
		}

		if (automaticScaling != null) {
			panel.scaleProperties.setAutomaticScaling(automaticScaling);
		}

		if (visibilityThreshold != null) {
			panel.visibilityThresholdProperties.setValue(visibilityThreshold);
		}

		if (opacity != null) {
			panel.opacityProperties.setValue(opacity);
		}

		if (showSimulationBox != null) {
			panel.showSimulationBoxProperties.setValue(showSimulationBox);
		}

		if (whiteBackground != null) {
			panel.whiteBackgroundProperties.setValue(whiteBackground);
		}

		if (unequalScaling != null) {
			panel.unequalScalingProperties.setValue(unequalScaling);
		}

		if (phi != null) {
			panel.phi = phi;
		}

		if (theta != null) {
			panel.theta = theta;
		}

		if (centerx != null) {
			panel.centerx = centerx;
		}

		if (centery != null) {
			panel.centery = centery;
		}

		if (centerz != null) {
			panel.centerz = centerz;
		}

		if (distanceFactor != null) {
			panel.distanceFactor = distanceFactor;
		}

		return panel;
	}
}
