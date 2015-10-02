package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.chart.Chart2DPanel;
import org.openpixi.pixi.ui.panel.properties.BooleanProperties;

public class YamlChart2DPanel {

	// Boolean properties
	public Boolean logarithmicScale;
	public Boolean useExcludedRegion;

	// Chart content properties
	public String[] showCharts;

	// Excluded region properties
	public String restrictedRegion;


	/** Empty constructor called by SnakeYaml */
	public YamlChart2DPanel() {
	}

	public YamlChart2DPanel(Component component) {
		if (component instanceof Chart2DPanel) {
			Chart2DPanel panel = (Chart2DPanel) component;
			logarithmicScale = panel.logarithmicProperty.getValue();
			showCharts = panel.showChartsProperty.getStringArrayFromValues();

			useExcludedRegion = panel.useExcludeRegionProperty.getValue();
			restrictedRegion = panel.regionPropery.getValue();
		}
	}

	public Component inflate(PanelManager panelManager) {

		Chart2DPanel panel = new Chart2DPanel(panelManager.getSimulationAnimation());

		if (logarithmicScale != null) {
			panel.logarithmicProperty.setValue(logarithmicScale);
		}

		if (showCharts != null) {
			panel.showChartsProperty.setValuesFromStringArray(showCharts);
		}

		if(useExcludedRegion != null) {
			panel.useExcludeRegionProperty.setValue(useExcludedRegion);
		}

		if(restrictedRegion != null) {
			panel.regionPropery.setValue(restrictedRegion);
		}

		return panel;
	}
}
