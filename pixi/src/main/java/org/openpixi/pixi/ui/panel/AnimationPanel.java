package org.openpixi.pixi.ui.panel;

import javax.swing.JPanel;

import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.SimulationAnimationListener;

public class AnimationPanel extends JPanel {

	private SimulationAnimation simulationAnimation;

	/** Constructor */
	public AnimationPanel(SimulationAnimation simulationAnimation) {
		this.simulationAnimation = simulationAnimation;
		this.simulationAnimation.addListener(new MyAnimationListener());
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
}
