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

	// Info properties
	public Boolean showInfo;

	// Trace properties
	public Boolean showTrace;

	public Component inflate(PanelManager panelManager) {

		Particle2DPanel panel = new Particle2DPanel(panelManager.getSimulationAnimation());

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

		if (showTrace != null) {
			panel.getTraceProperties().setShowTrace(showTrace);
		}

		return panel;
	}
}
