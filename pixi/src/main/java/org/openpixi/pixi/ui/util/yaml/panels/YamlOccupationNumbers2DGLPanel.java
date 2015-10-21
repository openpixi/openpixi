package org.openpixi.pixi.ui.util.yaml.panels;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.gl.EnergyDensity2DGLPanel;
import org.openpixi.pixi.ui.panel.gl.OccupationNumbers2DGLPanel;
import org.openpixi.pixi.ui.panel.properties.IntegerProperties;

import java.awt.*;

public class YamlOccupationNumbers2DGLPanel {

	// Scale properties
	public Double scaleFactor;
	public Boolean automaticScaling;

	// Colorful display properties
	public Boolean colorful;

	// Frame skip properties
	public Integer frameSkip;

	/** Empty constructor called by SnakeYaml */
	public YamlOccupationNumbers2DGLPanel() {
	}

	public YamlOccupationNumbers2DGLPanel(Component component) {
		if (component instanceof OccupationNumbers2DGLPanel) {
			OccupationNumbers2DGLPanel panel = (OccupationNumbers2DGLPanel) component;
			scaleFactor = panel.scaleProperties.getScaleFactor();
			automaticScaling = panel.scaleProperties.getAutomaticScaling();
			colorful = panel.colorfulProperties.getValue();
			frameSkip = panel.frameSkipProperties.getValue();
		}
	}

	public Component inflate(PanelManager panelManager) {

		OccupationNumbers2DGLPanel panel = new OccupationNumbers2DGLPanel(panelManager.getSimulationAnimation());

		if (scaleFactor != null) {
			panel.scaleProperties.setScaleFactor(scaleFactor);
		}

		if (automaticScaling != null) {
			panel.scaleProperties.setAutomaticScaling(automaticScaling);
		}

		if (colorful != null) {
			panel.colorfulProperties.setValue(colorful);
		}

		if (frameSkip != null) {
			panel.frameSkipProperties.setValue(frameSkip);
		}


		return panel;
	}
}
