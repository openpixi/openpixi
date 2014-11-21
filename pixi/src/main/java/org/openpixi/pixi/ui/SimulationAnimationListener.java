package org.openpixi.pixi.ui;

/**
 * Interface for the SimulationAnimation class.
 *
 */
public interface SimulationAnimationListener {

	/**
	 * Called when objects should repaint themselves.
	 */
	public void repaint();

	/**
	 * Clear the screen from traces
	 */
	public void clear();
}
