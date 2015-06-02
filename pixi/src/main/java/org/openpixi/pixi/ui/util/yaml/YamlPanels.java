package org.openpixi.pixi.ui.util.yaml;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.ElectricFieldPanel;
import org.openpixi.pixi.ui.util.yaml.panels.YamlParticle2DPanel;

public class YamlPanels {

	public YamlPanels leftPanel;
	public YamlPanels rightPanel;
	public Integer orientation;
	public Integer dividerLocation;

	public YamlParticle2DPanel particle2DPanel;

	public Component inflate(PanelManager panelManager) {
		Component component = new ElectricFieldPanel(panelManager.getSimulationAnimation());
		return component;
	}
}
