package org.openpixi.pixi.ui.util.yaml;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.ElectricFieldPanel;
import org.openpixi.pixi.ui.panel.Particle2DPanel;
import org.openpixi.pixi.ui.util.yaml.panels.YamlParticle2DPanel;

public class YamlPanels {

	public YamlPanels leftPanel;
	public YamlPanels rightPanel;
	public Integer orientation;
	public Integer dividerLocation;

	public YamlParticle2DPanel particle2DPanel;

	public Component inflate(PanelManager panelManager) {
		Component component = null;
		if (leftPanel != null && rightPanel != null) {
			// Create new split panel
			Component leftComponent = leftPanel.inflate(panelManager);
			Component rightComponent = rightPanel.inflate(panelManager);

			component = panelManager.getSplitPanel(leftComponent, rightComponent, orientation, dividerLocation);
		} else if (particle2DPanel != null) {
			component = new Particle2DPanel(panelManager.getSimulationAnimation());
		}
		return component;
	}
}
