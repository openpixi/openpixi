package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.ElectricFieldPanel;

public class YamlElectricFieldPanel {


	public Integer colorIndex;
	public Integer directionIndex;
	public Double scaleFactor;
	public Boolean automaticScaling;

	public Component inflate(PanelManager panelManager) {

		ElectricFieldPanel panel = new ElectricFieldPanel(panelManager.getSimulationAnimation());

		if (colorIndex != null) {
			panel.getColorProperties().setColorIndex(colorIndex);
		}

		if (directionIndex != null) {
			panel.getColorProperties().setDirectionIndex(directionIndex);
		}

		if (scaleFactor != null) {
			panel.getScaleProperties().setScaleFactor(scaleFactor);
		}

		if (automaticScaling != null) {
			panel.getScaleProperties().setAutomaticScaling(automaticScaling);
		}
		return panel;
	}
}
