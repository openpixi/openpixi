package org.openpixi.pixi.ui.panel.gl;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.SimulationAnimationListener;

public class AnimationGLPanel extends GLJPanel {

	private SimulationAnimation simulationAnimation;
	private MyAnimationListener listener;
	boolean focus = false;

	/** Constructor */
	public AnimationGLPanel(SimulationAnimation simulationAnimation) {
		this.simulationAnimation = simulationAnimation;
		listener = new MyAnimationListener();
		this.simulationAnimation.addListener(listener);
		this.setVisible(true);

		this.addGLEventListener(new GLEventListener() {

			@Override
			public void reshape(GLAutoDrawable glautodrawable, int x, int y,
					int width, int height) {
				OneTriangle.setup(glautodrawable.getGL().getGL2(), width,
						height);
			}

			@Override
			public void init(GLAutoDrawable glautodrawable) {
			}

			@Override
			public void dispose(GLAutoDrawable glautodrawable) {
			}

			@Override
			public void display(GLAutoDrawable glautodrawable) {
				// TODO: Show focus (if set)
				OneTriangle.render(glautodrawable.getGL().getGL2(),
						glautodrawable.getWidth(), glautodrawable.getHeight());
			}
		});
	}

	/** Listener for timer */
	private class MyAnimationListener implements SimulationAnimationListener {

		public void repaint() {
			AnimationGLPanel.this.repaint();
		}

		public void clear() {
		}
	}

	public SimulationAnimation getSimulationAnimation() {
		return simulationAnimation;
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

	/**
	 * Add a component to the property panel.
	 *
	 * @param box Property panel.
	 */
	public void addComponents(Box box) {

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
