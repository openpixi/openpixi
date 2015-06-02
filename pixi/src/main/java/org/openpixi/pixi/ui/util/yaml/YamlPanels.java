package org.openpixi.pixi.ui.util.yaml;

import java.awt.Component;

import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.ElectricFieldPanel;
import org.openpixi.pixi.ui.util.yaml.panels.YamlParticle2DPanel;

public class YamlPanels {

	public YamlPanels leftPanel;
	public YamlPanels rightPanel;
	public Integer orientation;
	public Integer dividerLocation;

	public YamlParticle2DPanel particle2DPanel;

	public Component inflate(SimulationAnimation simulationAnimation) {
		Component component = new ElectricFieldPanel(simulationAnimation);
		return component;
	}
}
