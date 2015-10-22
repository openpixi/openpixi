package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.Particle2DPanel;

public class YamlParticle2DPanel {

	// Color properties
	public Integer colorIndex;
	public Integer directionIndex;

	// Field properties
	public Boolean drawCurrent;
	public Boolean drawFields;

	/** Empty constructor called by SnakeYaml */
	public YamlParticle2DPanel() {
	}

	public YamlParticle2DPanel(Component component) {
		if (component instanceof Particle2DPanel) {
			Particle2DPanel panel = (Particle2DPanel) component;
			colorIndex = panel.colorProperties.getColorIndex();
			directionIndex = panel.colorProperties.getDirectionIndex();
			drawCurrent = panel.fieldProperties.isDrawCurrent();
			drawFields = panel.fieldProperties.isDrawFields();
		}
	}

	public Component inflate(PanelManager panelManager) {

		Particle2DPanel panel = new Particle2DPanel(panelManager.getSimulationAnimation());

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

		return panel;
	}
}
