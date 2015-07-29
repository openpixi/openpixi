package org.openpixi.pixi.ui.panel.properties;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JCheckBox;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.AnimationPanel;
import org.openpixi.pixi.ui.util.FrameRateDetector;

public class InfoProperties {

	private SimulationAnimation simulationAnimation;
	public boolean showInfo = false;

	Color darkGreen = new Color(0x00, 0x80, 0x00);

	public InfoProperties(SimulationAnimation simulationAnimation) {
		this.simulationAnimation = simulationAnimation;
	}

	public boolean isShowInfo() {
		return showInfo;
	}

	public void setShowInfo(boolean showInfo) {
		this.showInfo = showInfo;
	}

	public void addComponents(Box box) {
		Box settingControls = Box.createVerticalBox();

		JCheckBox framerateCheck;
		framerateCheck = new JCheckBox("Info");
		framerateCheck.addItemListener(new FrameListener());
		framerateCheck.setSelected(showInfo);

		settingControls.add(framerateCheck);
		settingControls.add(Box.createVerticalGlue());

		box.add(settingControls);
	}

	class FrameListener implements ItemListener {
		public void itemStateChanged(ItemEvent event) {
			InfoProperties.this.showInfo =
					(event.getStateChange() == ItemEvent.SELECTED);
			simulationAnimation.repaint();
		}
	}

	public void showInfo(Graphics2D graph, AnimationPanel panel) {

		if (showInfo) {
			FrameRateDetector frameratedetector = panel.getSimulationAnimation().getFrameRateDetector();
			Simulation s = panel.getSimulationAnimation().getSimulation();

			graph.translate(0.0, panel.getHeight());
			graph.scale(1.0, -1.0);
			graph.setColor(darkGreen);
			graph.drawString("Frame rate: " + frameratedetector.getRateString() + " fps", 30, 30);
			graph.drawString("Time step: " + (float) s.tstep, 30, 50);
			graph.drawString("Total time: " + (float) s.totalSimulationTime, 30, 70);

			Runtime runtime = Runtime.getRuntime();
			long maxMemory = runtime.maxMemory();
			long allocatedMemory = runtime.totalMemory();
			long freeMemory = runtime.freeMemory();

			int bottom = panel.getHeight();
			graph.drawString("free memory: " + freeMemory / 1024, 30, bottom - 90);
			graph.drawString("allocated memory: " + allocatedMemory / 1024, 30, bottom - 70);
			graph.drawString("max memory: " + maxMemory /1024, 30, bottom - 50);
			graph.drawString("total free memory: " +
				(freeMemory + (maxMemory - allocatedMemory)) / 1024, 30, bottom - 30);
		}
	}
}
