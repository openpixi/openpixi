package org.openpixi.pixi.ui.tab;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.AnimationPanel;

public class PropertiesTab extends Box {

	private Component parent;
	private PanelManager panelManager;

	public PropertiesTab(Component parent, PanelManager panelManager) {
		super(BoxLayout.PAGE_AXIS);
		this.parent = parent;
		this.panelManager = panelManager;
		this.panelManager.setPropertiesTab(this);
	}

	public void refreshContent(AnimationPanel panel) {
		this.removeAll();
		panel.addComponents(this);
		this.repaint();
	}
}
