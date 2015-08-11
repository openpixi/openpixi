package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.ElectricFieldPanel;

public class YamlElectricFieldPanel {

	// Fields content properties
	public String[] showFields;

	// Color properties
	public Integer colorIndex;
	public Integer directionIndex;

	// Scale properties
	public Double scaleFactor;
	public Boolean automaticScaling;

	// Coordinate properties
	public String showCoordinates;

	/** Empty constructor called by SnakeYaml */
	public YamlElectricFieldPanel() {
	}

	public YamlElectricFieldPanel(Component component) {
		if (component instanceof ElectricFieldPanel) {
			ElectricFieldPanel panel = (ElectricFieldPanel) component;
			showFields = panel.showFieldProperties.getStringArrayFromValues();
			colorIndex = panel.getColorProperties().getColorIndex();
			directionIndex = panel.getColorProperties().getDirectionIndex();
			scaleFactor = panel.getScaleProperties().getScaleFactor();
			automaticScaling = panel.getScaleProperties().getAutomaticScaling();
			showCoordinates = panel.showCoordinateProperties.getValue();
		}
	}

	public Component inflate(PanelManager panelManager) {

		ElectricFieldPanel panel = new ElectricFieldPanel(panelManager.getSimulationAnimation());

		if (showFields != null) {
			panel.showFieldProperties.setValuesFromStringArray(showFields);
		}

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

		if (showCoordinates != null) {
			panel.showCoordinateProperties.setValue(showCoordinates);
		}
		return panel;
	}
}
