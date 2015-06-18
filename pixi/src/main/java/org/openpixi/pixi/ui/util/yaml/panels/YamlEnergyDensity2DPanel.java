package org.openpixi.pixi.ui.util.yaml.panels;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.EnergyDensity2DPanel;

public class YamlEnergyDensity2DPanel {

	// Info properties
	public Boolean showInfo;
	
	// Scale properties
	public Double scaleFactor;
	public Boolean automaticScaling;	

	/** Empty constructor called by SnakeYaml */
	public YamlEnergyDensity2DPanel() {
	}

	public YamlEnergyDensity2DPanel(Component component) {
		if (component instanceof EnergyDensity2DPanel) {
			EnergyDensity2DPanel panel = (EnergyDensity2DPanel) component;
			scaleFactor = panel.getScaleProperties().getScaleFactor();
			automaticScaling = panel.getScaleProperties().getAutomaticScaling();
			showInfo = panel.getInfoProperties().isShowInfo();
		}
	}

	public Component inflate(PanelManager panelManager) {

		EnergyDensity2DPanel panel = new EnergyDensity2DPanel(panelManager.getSimulationAnimation());

		if (showInfo != null) {
			panel.getInfoProperties().setShowInfo(showInfo);
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
