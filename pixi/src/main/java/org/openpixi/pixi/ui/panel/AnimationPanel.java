package org.openpixi.pixi.ui.panel;

import javax.swing.JPanel;

import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.SimulationAnimationListener;

public class AnimationPanel extends JPanel {

	private SimulationAnimation simulationAnimation;
	private MyAnimationListener listener;

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
}
