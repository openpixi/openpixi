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
			scaleFactor = panel.getScaleProperties().getScaleFactor();
			automaticScaling = panel.getScaleProperties().getAutomaticScaling();
			colorful = panel.getColorfulPropteries().getValue();
			frameSkip = panel.getFrameSkipProperties().getValue();
		}
	}

	public Component inflate(PanelManager panelManager) {

		OccupationNumbers2DGLPanel panel = new OccupationNumbers2DGLPanel(panelManager.getSimulationAnimation());

		if (scaleFactor != null) {
			panel.getScaleProperties().setScaleFactor(scaleFactor);
		}

		if (automaticScaling != null) {
			panel.getScaleProperties().setAutomaticScaling(automaticScaling);
		}

		if (colorful != null) {
			panel.getColorfulPropteries().setValue(colorful);
		}

		if (frameSkip != null) {
			panel.getFrameSkipProperties().setValue(frameSkip);
		}


		return panel;
	}
}
