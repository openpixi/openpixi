package org.openpixi.pixi.ui.panel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Box;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.ScaleProperties;

/**
 * This panel shows the one-dimensional phase space (x vs. vx).
 */
public class PhaseSpacePanel extends AnimationPanel {

	ScaleProperties scaleProperties;

	/** Constructor */
	public PhaseSpacePanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
		scaleProperties = new ScaleProperties(simulationAnimation);
	}

	/** Display the particles */
	public void paintComponent(Graphics graph1) {
		Graphics2D graph = (Graphics2D) graph1;
		setBackground(Color.white);
		graph.translate(0, this.getHeight());
		graph.scale(1, -1);

		super.paintComponent(graph1);

		// scale factor for velocity
		double scaleV = scaleProperties.getScale();

		Simulation s = getSimulationAnimation().getSimulation();
		/** Scaling factor for the displayed panel in x-direction*/
		double sx = getWidth() / s.getWidth();
		/** Scaling factor for the displayed panel in y-direction*/
		double sy = getHeight() / s.getHeight();

		double panelHeight = getHeight();

		scaleProperties.resetAutomaticScale();

		for (int i = 0; i < s.particles.size(); i++) {
			IParticle par = (IParticle) s.particles.get(i);
			graph.setColor(par.getColor());
			double radius = par.getRadius();
			int width = (int) (2*sx*radius);
			int height = (int) (2*sx*radius);
			double velocity = par.getVelocity(0);
			scaleProperties.putValue(velocity);
			double position = (0.5 + scaleV * velocity) * panelHeight;
			if(width > 2 && height > 2) {
				graph.fillOval((int) (par.getPosition(0)*sx) - width/2, (int) position - height/2,  width,  height);
			}
			else {
				graph.drawRect((int) (par.getPosition(0)*sx), (int) position, 0, 0);
			}
		}
		scaleProperties.calculateAutomaticScale(0.5);

	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Phase space panel");
		scaleProperties.addComponents(box);
	}

	public ScaleProperties getScaleProperties() {
		return scaleProperties;
	}
}
