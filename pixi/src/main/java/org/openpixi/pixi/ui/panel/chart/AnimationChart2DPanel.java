package org.openpixi.pixi.ui.panel.chart;

import info.monitorenter.gui.chart.Chart2D;

import java.awt.Graphics;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.SimulationAnimationListener;
import org.openpixi.pixi.ui.panel.FocusablePanel;

/**
 * This panel shows various charts.
 */
public class AnimationChart2DPanel extends Chart2D implements FocusablePanel {

	private SimulationAnimation simulationAnimation;
	private MyAnimationListener listener;
	boolean focus = false;

	/** Constructor */
	public AnimationChart2DPanel(SimulationAnimation simulationAnimation) {
		super();
		this.simulationAnimation = simulationAnimation;
		listener = new MyAnimationListener();
		this.simulationAnimation.addListener(listener);
		this.setVisible(true);
	}

	/** Listener for timer */
	private class MyAnimationListener implements SimulationAnimationListener {

		public void repaint() {
			AnimationChart2DPanel.this.update();
		}

		public void clear() {
			AnimationChart2DPanel.this.clear();
		}
	}

	public void update() {
	}

	public void clear() {
	}

	public SimulationAnimation getSimulationAnimation() {
		return simulationAnimation;
	}

	/** Unregister this panel */
	public void destruct() {
		simulationAnimation.removeListener(listener);
	}

	@Override
	public void setFocus(boolean focus) {
		this.focus = focus;
	}

	@Override
	public boolean isFocused() {
		return focus;
	}

	/**
	 * Add a component to the property panel.
	 *
	 * @param box Property panel.
	 */
	public void addPropertyComponents(Box box) {

	}

	/**
	 * Add a label to the property panel
	 *
	 * @param box Property panel.
	 * @param label Label of property panel.
	 */
	public void addLabel(Box box, String label) {
		JLabel jlabel = new JLabel(label, SwingConstants.CENTER);
		box.add(Box.createVerticalStrut(20));
		box.add(jlabel);
		box.add(Box.createVerticalGlue());
	}
}