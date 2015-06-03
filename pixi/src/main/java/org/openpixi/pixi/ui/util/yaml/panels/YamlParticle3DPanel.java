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

	// Info properties
	public Boolean showInfo;

	/** Empty constructor called by SnakeYaml */
	public YamlParticle3DPanel() {
	}

	public YamlParticle3DPanel(Component component) {
		if (component instanceof Particle3DPanel) {
			Particle3DPanel panel = (Particle3DPanel) component;
			colorIndex = panel.getColorProperties().getColorIndex();
			directionIndex = panel.getColorProperties().getDirectionIndex();
			drawCurrent = panel.getFieldProperties().isDrawCurrent();
			drawFields = panel.getFieldProperties().isDrawFields();
			showInfo = panel.getInfoProperties().isShowInfo();
		}
	}

	public Component inflate(PanelManager panelManager) {

		Particle3DPanel panel = new Particle3DPanel(panelManager.getSimulationAnimation());

		if (colorIndex != null) {
			panel.getColorProperties().setColorIndex(colorIndex);
		}

		if (directionIndex != null) {
			panel.getColorProperties().setDirectionIndex(directionIndex);
		}

		if (drawCurrent != null) {
			panel.getFieldProperties().setDrawCurrent(drawCurrent);
		}

		if (drawFields != null) {
			panel.getFieldProperties().setDrawFields(drawFields);
		}

		if (showInfo != null) {
			panel.getInfoProperties().setShowInfo(showInfo);
		}

		return panel;
	}
}
