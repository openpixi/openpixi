package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.ElectricFieldPanel;

public class YamlElectricFieldPanel {

	// Color properties
	public Integer colorIndex;
	public Integer directionIndex;

	// Scale properties
	public Double scaleFactor;
	public Boolean automaticScaling;

	/** Empty constructor called by SnakeYaml */
	public YamlElectricFieldPanel() {
	}

	public YamlElectricFieldPanel(Component component) {
		if (component instanceof ElectricFieldPanel) {
			ElectricFieldPanel panel = (ElectricFieldPanel) component;
			colorIndex = panel.getColorProperties().getColorIndex();
			directionIndex = panel.getColorProperties().getDirectionIndex();
			scaleFactor = panel.getScaleProperties().getScaleFactor();
			automaticScaling = panel.getScaleProperties().getAutomaticScaling();
		}
	}

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
