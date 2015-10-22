package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.Particle3DPanel;

public class YamlParticle3DPanel {

	// Color properties
	public Integer colorIndex;
	public Integer directionIndex;

	// Field properties
	public Boolean drawCurrent;
	public Boolean drawFields;

	// Projection properties
	public Double phi;
	public Double theta;

	/** Empty constructor called by SnakeYaml */
	public YamlParticle3DPanel() {
	}

	public YamlParticle3DPanel(Component component) {
		if (component instanceof Particle3DPanel) {
			Particle3DPanel panel = (Particle3DPanel) component;
			colorIndex = panel.colorProperties.getColorIndex();
			directionIndex = panel.colorProperties.getDirectionIndex();
			drawCurrent = panel.fieldProperties.isDrawCurrent();
			drawFields = panel.fieldProperties.isDrawFields();
			phi = panel.projection.phi;
			theta = panel.projection.theta;
		}
	}

	public Component inflate(PanelManager panelManager) {

		Particle3DPanel panel = new Particle3DPanel(panelManager.getSimulationAnimation());

		if (colorIndex != null) {
			panel.colorProperties.setColorIndex(colorIndex);
		}

		if (directionIndex != null) {
			panel.colorProperties.setDirectionIndex(directionIndex);
		}

		if (drawCurrent != null) {
			panel.fieldProperties.setDrawCurrent(drawCurrent);
		}

		if (drawFields != null) {
			panel.fieldProperties.setDrawFields(drawFields);
		}

		if (phi != null) {
			panel.projection.phi = phi;
		}

		if (theta != null) {
			panel.projection.theta = theta;
		}
		return panel;
	}
}
