package org.openpixi.pixi.ui.util.yaml;

import java.awt.Component;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.util.yaml.panels.YamlElectricFieldPanel;
import org.openpixi.pixi.ui.util.yaml.panels.YamlParticle2DPanel;
import org.openpixi.pixi.ui.util.yaml.panels.YamlParticle3DPanel;
import org.openpixi.pixi.ui.util.yaml.panels.YamlPhaseSpacePanel;

public class YamlPanels {

	public YamlPanels leftPanel;
	public YamlPanels rightPanel;
	public Integer orientation;
	public Integer dividerLocation;

	public YamlElectricFieldPanel electricFieldPanel;
	public YamlParticle2DPanel particle2DPanel;
	public YamlParticle3DPanel particle3DPanel;
	public YamlPhaseSpacePanel phaseSpacePanel;

	public Component inflate(PanelManager panelManager) {
		Component component = null;
		if (leftPanel != null && rightPanel != null) {
			// Create new split panel
			Component leftComponent = leftPanel.inflate(panelManager);
			Component rightComponent = rightPanel.inflate(panelManager);

			component = panelManager.getSplitPanel(leftComponent, rightComponent, orientation, dividerLocation);
		} else if (electricFieldPanel != null) {
			component = electricFieldPanel.inflate(panelManager);
		} else if (particle2DPanel != null) {
			component = particle2DPanel.inflate(panelManager);
		} else if (particle3DPanel != null) {
			component = particle3DPanel.inflate(panelManager);
		} else if (phaseSpacePanel != null) {
			component = phaseSpacePanel.inflate(panelManager);
		}
		return component;
	}
}
