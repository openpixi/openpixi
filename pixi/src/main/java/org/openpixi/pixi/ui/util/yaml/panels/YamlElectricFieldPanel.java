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

	public Integer dataSource;

	/** Empty constructor called by SnakeYaml */
	public YamlElectricFieldPanel() {
	}

	public YamlElectricFieldPanel(Component component) {
		if (component instanceof ElectricFieldPanel) {
			ElectricFieldPanel panel = (ElectricFieldPanel) component;
			showFields = panel.showFieldProperties.getStringArrayFromValues();
			colorIndex = panel.colorProperties.getColorIndex();
			directionIndex = panel.colorProperties.getDirectionIndex();
			scaleFactor = panel.scaleProperties.getScaleFactor();
			automaticScaling = panel.scaleProperties.getAutomaticScaling();
			showCoordinates = panel.showCoordinateProperties.getValue();
			dataSource = panel.sourceProperties.getIndex();
		}
	}

	public Component inflate(PanelManager panelManager) {

		ElectricFieldPanel panel = new ElectricFieldPanel(panelManager.getSimulationAnimation());

		if (showFields != null) {
			panel.showFieldProperties.setValuesFromStringArray(showFields);
		}

		if (colorIndex != null) {
			panel.colorProperties.setColorIndex(colorIndex);
		}

		if (directionIndex != null) {
			panel.colorProperties.setDirectionIndex(directionIndex);
		}

		if (scaleFactor != null) {
			panel.scaleProperties.setScaleFactor(scaleFactor);
		}

		if (automaticScaling != null) {
			panel.scaleProperties.setAutomaticScaling(automaticScaling);
		}

		if (showCoordinates != null) {
			panel.showCoordinateProperties.setValue(showCoordinates);
		}

		if (dataSource != null) {
			panel.sourceProperties.setIndex(dataSource);
		}
		return panel;
	}
}
