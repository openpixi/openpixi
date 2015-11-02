package org.openpixi.pixi.ui.panel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.Box;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.gauge.CoulombGauge;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.BooleanArrayProperties;
import org.openpixi.pixi.ui.panel.properties.BooleanProperties;
import org.openpixi.pixi.ui.panel.properties.ColorProperties;
import org.openpixi.pixi.ui.panel.properties.CoordinateProperties;
import org.openpixi.pixi.ui.panel.properties.ScaleProperties;
import org.openpixi.pixi.ui.util.FrameRateDetector;

/**
 * This panel shows the one-dimensional electric field along the x-direction.
 * Several field lines for various grid positions along the y-direction are
 * superimposed.
 */
public class InfoPanel extends AnimationPanel {

	Color darkGreen = new Color(0x00, 0x80, 0x00);

	/** Constructor */
	public InfoPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
	}

	/** Display information */
	public void paintComponent(Graphics graph1) {
		Graphics2D graph = (Graphics2D) graph1;
		setBackground(Color.white);

		/*
			Note:
			These two commands set the origin of the coordinate system to the lower-left corner of the panel and flip
			the y-axis. The system is now as follows:
			Let (x,y0 denote a point on the panel. (0,0) is the lower-left corner. The x-component increases to the
			right. The y-component increases downwards.
		 */
		graph.translate(0, this.getHeight());
		graph.scale(1, -1);

		super.paintComponent(graph1);

		FrameRateDetector frameratedetector = getSimulationAnimation().getFrameRateDetector();
		Simulation s = getSimulationAnimation().getSimulation();

		graph.translate(0.0, getHeight());
		graph.scale(1.0, -1.0);
		graph.setColor(darkGreen);
		graph.drawString("Frame rate: " + frameratedetector.getRateString() + " fps", 30, 30);
		graph.drawString("Time step: " + (float) s.tstep, 30, 50);
		graph.drawString("Total time: " + (float) s.totalSimulationTime, 30, 70);

		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();

		graph.drawString("free memory: " + freeMemory / 1024, 30, 110);
		graph.drawString("allocated memory: " + allocatedMemory / 1024, 30, 130);
		graph.drawString("max memory: " + maxMemory /1024, 30, 150);
		graph.drawString("total free memory: " +
			(freeMemory + (maxMemory - allocatedMemory)) / 1024, 30, 170);

	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Info panel");
	}
}
