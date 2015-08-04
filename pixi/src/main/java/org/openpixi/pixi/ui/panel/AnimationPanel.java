package org.openpixi.pixi.ui.panel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.SimulationAnimationListener;

public class AnimationPanel extends JPanel implements FocusablePanel {

	protected SimulationAnimation simulationAnimation;
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

	/**
	 * Add components to the property panel.
	 * @param box Property panel.
	 */
	public void addPropertyComponents(Box box) {

	}

	/**
	 * Add a label to the property panel
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
