package org.openpixi.pixi.ui.util.yaml.panels;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.gl.EnergyDensity2DGLPanel;
import org.openpixi.pixi.ui.panel.gl.GaussViolation2DGLPanel;

import java.awt.*;

public class YamlGaussViolation2DGLPanel {

	// Scale properties
	public Double scaleFactor;
	public Boolean automaticScaling;

	// Coordinate properties
	public String showCoordinates;

	/** Empty constructor called by SnakeYaml */
	public YamlGaussViolation2DGLPanel() {
	}

	public YamlGaussViolation2DGLPanel(Component component) {
		if (component instanceof GaussViolation2DGLPanel) {
			GaussViolation2DGLPanel panel = (GaussViolation2DGLPanel) component;
			scaleFactor = panel.getScaleProperties().getScaleFactor();
			automaticScaling = panel.getScaleProperties().getAutomaticScaling();
			showCoordinates = panel.showCoordinateProperties.getValue();
		}
	}

	public Component inflate(PanelManager panelManager) {

		GaussViolation2DGLPanel panel = new GaussViolation2DGLPanel(panelManager.getSimulationAnimation());

		if (scaleFactor != null) {
			panel.getScaleProperties().setScaleFactor(scaleFactor);
		}

		if (automaticScaling != null) {
			panel.getScaleProperties().setAutomaticScaling(automaticScaling);
		}

		if (showCoordinates != null) {
			panel.showCoordinateProperties.setValue(showCoordinates);
		}
		return panel;
	}
}
