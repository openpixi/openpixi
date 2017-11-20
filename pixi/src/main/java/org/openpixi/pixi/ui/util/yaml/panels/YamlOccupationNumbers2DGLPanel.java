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

	public String showCoordinates;
	public Boolean mirrorX;
	public Boolean coneRestriction;
	public Double collisionTime;
	public String collisionPosition;
	public String cutConeVelocity;
	public Boolean gaussianWindow;
	public Boolean tukeyWindow;
	public Double tukeyWidth;

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
			showCoordinates = panel.showCoordinateProperties.getValue();
			mirrorX = panel.mirrorProperties.getValue();
			coneRestriction = panel.useConeProperties.getValue();
			collisionTime = panel.collisionTimeDoubleProperties.getValue();
			collisionPosition = panel.collisionCoordinateProperties.getValue();
			cutConeVelocity = panel.velocityCoordinateProperties.getValue();
			gaussianWindow = panel.useGaussianWindowProperties.getValue();
			tukeyWindow = panel.useTukeyWindowProperties.getValue();
			tukeyWidth = panel.tukeyWidthProperties.getValue();
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

		if (showCoordinates != null) {
			panel.showCoordinateProperties.setValue(showCoordinates);
		}

		if (mirrorX != null) {
			panel.mirrorProperties.setValue(mirrorX);
		}

		if (coneRestriction != null) {
			panel.useConeProperties.setValue(coneRestriction);
		}

		if (collisionTime != null) {
			panel.collisionTimeDoubleProperties.setValue(collisionTime);
		}

		if (collisionPosition != null) {
			panel.collisionCoordinateProperties.setValue(collisionPosition);
		}

		if (cutConeVelocity != null) {
			panel.velocityCoordinateProperties.setValue(cutConeVelocity);
		}

		if (gaussianWindow != null) {
			panel.useGaussianWindowProperties.setValue(gaussianWindow);
		}

		if (tukeyWindow != null) {
			panel.useTukeyWindowProperties.setValue(tukeyWindow);
		}

		if (tukeyWidth != null) {
			panel.tukeyWidthProperties.setValue(tukeyWidth);
		}

		return panel;
	}
}
