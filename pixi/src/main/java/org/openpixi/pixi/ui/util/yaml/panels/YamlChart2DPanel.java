package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.chart.Chart2DPanel;

public class YamlChart2DPanel {

	// Boolean properties
	public Boolean logarithmicScale;

	// Chart content properties
	public Boolean gaussLaw;
	public Boolean eSquared;
	public Boolean bSquared;

	/** Empty constructor called by SnakeYaml */
	public YamlChart2DPanel() {
	}

	public YamlChart2DPanel(Component component) {
		if (component instanceof Chart2DPanel) {
			Chart2DPanel panel = (Chart2DPanel) component;
			logarithmicScale = panel.logarithmicProperty.getValue();
			gaussLaw = panel.chartContentProperty[panel.INDEX_GAUSS_VIOLATION].getValue();
			eSquared = panel.chartContentProperty[panel.INDEX_E_SQUARED].getValue();
			bSquared = panel.chartContentProperty[panel.INDEX_B_SQUARED].getValue();
		}
	}

	public Component inflate(PanelManager panelManager) {

		Chart2DPanel panel = new Chart2DPanel(panelManager.getSimulationAnimation());

		if (logarithmicScale != null) {
			panel.logarithmicProperty.setValue(logarithmicScale);
		}

		if (gaussLaw != null) {
			panel.chartContentProperty[panel.INDEX_GAUSS_VIOLATION].setValue(gaussLaw);
		}

		if (eSquared != null) {
			panel.chartContentProperty[panel.INDEX_E_SQUARED].setValue(eSquared);
		}

		if (bSquared != null) {
			panel.chartContentProperty[panel.INDEX_B_SQUARED].setValue(bSquared);
		}

		return panel;
	}
}
