package org.openpixi.pixi.ui.util.yaml;

import java.awt.Component;
import java.util.List;

import javax.swing.JSplitPane;

import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.panel.ElectricFieldPanel;
import org.openpixi.pixi.ui.panel.InfoPanel;
import org.openpixi.pixi.ui.panel.Particle2DPanel;
import org.openpixi.pixi.ui.panel.Particle3DPanel;
import org.openpixi.pixi.ui.panel.PhaseSpacePanel;
import org.openpixi.pixi.ui.panel.EnergyDensity1DPanel;
import org.openpixi.pixi.ui.panel.EnergyDensity2DPanel;
import org.openpixi.pixi.ui.panel.chart.Chart2DPanel;
import org.openpixi.pixi.ui.panel.gl.EnergyDensity2DGLPanel;
import org.openpixi.pixi.ui.panel.gl.EnergyDensity3DGLPanel;
import org.openpixi.pixi.ui.panel.gl.EnergyDensityVoxelGLPanel;
import org.openpixi.pixi.ui.panel.gl.GaussViolation2DGLPanel;
import org.openpixi.pixi.ui.panel.gl.OccupationNumbers2DGLPanel;
import org.openpixi.pixi.ui.util.yaml.panels.*;

public class YamlPanels {

	public YamlPanels leftPanel;
	public YamlPanels rightPanel;
	public Integer orientation;
	public Integer dividerLocation;
	public Integer windowWidth;
	public Integer windowHeight;

	public YamlElectricFieldPanel electricFieldPanel;
	public YamlParticle2DPanel particle2DPanel;
	public YamlParticle3DPanel particle3DPanel;
	public YamlPhaseSpacePanel phaseSpacePanel;
	public YamlEnergyDensity1DPanel energyDensity1DPanel;
	public YamlEnergyDensity2DPanel energyDensity2DPanel;
	public YamlEnergyDensity2DGLPanel energyDensity2DGLPanel;
	public YamlEnergyDensity3DGLPanel energyDensity3DGLPanel;
	public YamlEnergyDensityVoxelGLPanel energyDensityVoxelGLPanel;
	public YamlOccupationNumbers2DGLPanel occupationNumbers2DGLPanel;
	public YamlGaussViolation2DGLPanel gaussViolation2DGLPanel;
	public YamlChart2DPanel chartPanel;
	public YamlInfoPanel infoPanel;

	/** Empty constructor called by SnakeYaml */
	public YamlPanels() {
	}

	/**
	 * Constructor which fills the values of this class according to
	 * the component attached.
	 * @param component Java swing component from which to extract the parameters.
	 * @param windowWidth Width of window
	 * @param windowHeight Height of Window
	 */
	public YamlPanels(Component component, int windowWidth, int windowHeight) {
		this(component);
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
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
		} else if (component instanceof EnergyDensity1DPanel) {
			energyDensity1DPanel = new YamlEnergyDensity1DPanel(component);
		} else if (component instanceof EnergyDensity2DPanel) {
			energyDensity2DPanel = new YamlEnergyDensity2DPanel(component);
		} else if (component instanceof EnergyDensity2DGLPanel) {
			energyDensity2DGLPanel = new YamlEnergyDensity2DGLPanel(component);
		} else if (component instanceof EnergyDensity3DGLPanel) {
			energyDensity3DGLPanel = new YamlEnergyDensity3DGLPanel(component);
		} else if (component instanceof EnergyDensityVoxelGLPanel) {
			energyDensityVoxelGLPanel = new YamlEnergyDensityVoxelGLPanel(component);
		} else if (component instanceof OccupationNumbers2DGLPanel) {
			occupationNumbers2DGLPanel = new YamlOccupationNumbers2DGLPanel(component);
		} else if (component instanceof Chart2DPanel) {
			chartPanel = new YamlChart2DPanel(component);
		} else if (component instanceof GaussViolation2DGLPanel) {
			gaussViolation2DGLPanel = new YamlGaussViolation2DGLPanel(component);
		} else if (component instanceof InfoPanel) {
			infoPanel = new YamlInfoPanel(component);
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
		} else if (energyDensity1DPanel != null) {
			component = energyDensity1DPanel.inflate(panelManager);
		} else if (energyDensity2DPanel != null) {
			component = energyDensity2DPanel.inflate(panelManager);
		} else if (energyDensity2DGLPanel != null) {
			component = energyDensity2DGLPanel.inflate(panelManager);
		} else if (energyDensity3DGLPanel != null) {
			component = energyDensity3DGLPanel.inflate(panelManager);
		} else if (energyDensityVoxelGLPanel != null) {
			component = energyDensityVoxelGLPanel.inflate(panelManager);
		} else if (occupationNumbers2DGLPanel != null) {
			component = occupationNumbers2DGLPanel.inflate(panelManager);
		} else if (chartPanel != null) {
			component = chartPanel.inflate(panelManager);
		} else if (gaussViolation2DGLPanel != null) {
			component = gaussViolation2DGLPanel.inflate(panelManager);
		} else if (infoPanel != null) {
			component = infoPanel.inflate(panelManager);
		}

		return component;
	}
}
