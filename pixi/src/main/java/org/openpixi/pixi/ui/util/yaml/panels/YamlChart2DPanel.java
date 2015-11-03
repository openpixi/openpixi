package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.chart.Chart2DPanel;

public class YamlChart2DPanel {

	// Boolean properties
	public Boolean logarithmicScale;

	// Chart content properties
	public String[] showCharts;

	/** Empty constructor called by SnakeYaml */
	public YamlChart2DPanel() {
	}

	public YamlChart2DPanel(Component component) {
		if (component instanceof Chart2DPanel) {
			Chart2DPanel panel = (Chart2DPanel) component;
			logarithmicScale = panel.logarithmicProperty.getValue();
			showCharts = panel.showChartsProperty.getStringArrayFromValues();
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

		return panel;
	}
}
