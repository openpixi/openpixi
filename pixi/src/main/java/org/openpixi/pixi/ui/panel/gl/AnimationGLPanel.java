package org.openpixi.pixi.ui.panel.gl;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.SimulationAnimationListener;
import org.openpixi.pixi.ui.panel.FocusablePanel;

public class AnimationGLPanel extends GLJPanel implements GLEventListener, FocusablePanel {

	protected SimulationAnimation simulationAnimation;
	private MyAnimationListener listener;
	boolean focus = false;

	/** Constructor */
	public AnimationGLPanel(SimulationAnimation simulationAnimation) {
		this.simulationAnimation = simulationAnimation;
		listener = new MyAnimationListener();
		this.simulationAnimation.addListener(listener);
		this.setVisible(true);

		this.addGLEventListener(this);
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

	@Override
	public void display(GLAutoDrawable glautodrawable) {
		// TODO: Show focus (if set)
	}

	@Override
	public void dispose(GLAutoDrawable glautodrawable) {
	}

	@Override
	public void init(GLAutoDrawable glautodrawable) {
	}

	@Override
	public void reshape(GLAutoDrawable glautodrawable, int x, int y,
			int width, int height) {
		// Setup 2D mode
		GL2 gl2 = glautodrawable.getGL().getGL2();
		gl2.glMatrixMode( GL2.GL_PROJECTION );
		gl2.glLoadIdentity();

		// coordinate system origin at lower left with width and height same as the window
		GLU glu = new GLU();
		glu.gluOrtho2D( 0.0f, width, 0.0f, height );

		gl2.glMatrixMode( GL2.GL_MODELVIEW );
		gl2.glLoadIdentity();

		gl2.glViewport( 0, 0, width, height );
	}
}
