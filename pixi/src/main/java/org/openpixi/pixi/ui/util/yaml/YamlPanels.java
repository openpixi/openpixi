package org.openpixi.pixi.ui.util.yaml;

import java.awt.Component;

import javax.swing.JSplitPane;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.ElectricFieldPanel;
import org.openpixi.pixi.ui.panel.Particle2DPanel;
import org.openpixi.pixi.ui.panel.Particle3DPanel;
import org.openpixi.pixi.ui.panel.PhaseSpacePanel;
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

	/** Empty constructor called by SnakeYaml */
	public YamlPanels() {
	}

	/**
	 * Constructor which fills the values of this class according to
	 * the component attached.
	 * @param component Java swing component from which to extract the parameters.
	 */
	public YamlPanels(Component component) {
		if (component instanceof JSplitPane) {
			JSplitPane splitpane = (JSplitPane) component;
			Component leftComponent = splitpane.getLeftComponent();
			Component rightComponent = splitpane.getRightComponent();
			orientation = splitpane.getOrientation();
			dividerLocation = splitpane.getDividerLocation();
			leftPanel = new YamlPanels(leftComponent);
			rightPanel = new YamlPanels(rightComponent);
		} else if (component instanceof ElectricFieldPanel) {
			electricFieldPanel = new YamlElectricFieldPanel(component);
		} else if (component instanceof Particle2DPanel) {
			particle2DPanel = new YamlParticle2DPanel(component);
		} else if (component instanceof Particle3DPanel) {
			particle3DPanel = new YamlParticle3DPanel(component);
		} else if (component instanceof PhaseSpacePanel) {
			phaseSpacePanel = new YamlPhaseSpacePanel(component);
		}
	}

	/**
	 * Inflate the values into Java components that can be displayed by the PanelManager.
	 * @param panelManager
	 * @return
	 */
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
