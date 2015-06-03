package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.Particle3DPanel;

public class YamlParticle3DPanel {

	public Integer colorIndex;
	public Integer directionIndex;

	public Component inflate(PanelManager panelManager) {

		Particle3DPanel panel = new Particle3DPanel(panelManager.getSimulationAnimation());

		if (colorIndex != null) {
			panel.getColorProperties().setColorIndex(colorIndex);
		}

		if (directionIndex != null) {
			panel.getColorProperties().setDirectionIndex(directionIndex);
		}

		return panel;
	}
}
