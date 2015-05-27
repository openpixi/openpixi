package org.openpixi.pixi.ui.panel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.SimulationAnimationListener;

public class AnimationPanel extends JPanel {

	private SimulationAnimation simulationAnimation;
	private MyAnimationListener listener;
	boolean focus = false;

	/** Constructor */
	public AnimationPanel(SimulationAnimation simulationAnimation) {
		this.simulationAnimation = simulationAnimation;
		listener = new MyAnimationListener();
		this.simulationAnimation.addListener(listener);
		this.setVisible(true);
	}

	/** Listener for timer */
	public class MyAnimationListener implements SimulationAnimationListener {

		public void repaint() {
			AnimationPanel.this.repaint();
		}

		public void clear() {
			AnimationPanel.this.clear();
		}
	}

	public SimulationAnimation getSimulationAnimation() {
		return simulationAnimation;
	}

	/** Clear screen.
	 * (Overwrite for custom behavior.) */
	public void clear() {
	}

	/** Unregister this panel */
	public void destruct() {
		simulationAnimation.removeListener(listener);
	}

	public void setFocus(boolean focus) {
		this.focus = focus;
	}

	public boolean isFocused() {
		return focus;
	}

	public void paintComponent(Graphics graph1) {
		super.paintComponent(graph1);
		if (focus) {
			// Paint focus frame
			Graphics2D graph = (Graphics2D) graph1;
			graph.setColor(Color.gray);
			graph.drawRect(0, 1, getWidth() - 1, getHeight() - 2);
		}
	}
}
