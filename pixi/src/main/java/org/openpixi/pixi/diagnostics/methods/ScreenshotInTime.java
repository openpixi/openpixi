package org.openpixi.pixi.diagnostics.methods;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.media.opengl.DefaultGLCapabilitiesChooser;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLProfile;

import org.openpixi.pixi.diagnostics.Diagnostics;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.ui.MainControlApplet;
import org.openpixi.pixi.ui.PanelManager;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.AnimationPanel;
import org.openpixi.pixi.ui.panel.gl.AnimationGLPanel;
import org.openpixi.pixi.ui.util.yaml.YamlPanels;

import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

/**
 * Takes a screenshot of the current simulation at specified times.
 */
public class ScreenshotInTime implements Diagnostics {

	private String path;
	private double timeInterval;
	private int stepInterval;
	private double timeOffset;
	private int stepOffset;
	private int stepIterations;
	private int width;
	private int height;
	private YamlPanels panel;
	private boolean finished;

	private AnimationGLPanel animationGLPanel;
	private Component component;

	GLAutoDrawable glautodrawable;

	public ScreenshotInTime(String path, double timeInterval, double timeOffset, int width, int height, YamlPanels panel) {
		this.path = path;
		this.timeInterval = timeInterval;
		this.timeOffset = timeOffset;
		this.width = width;
		this.panel = panel;
		this.height = height;
	}

	@Override
	public void initialize(Simulation s) {
		this.stepInterval = (int) (timeInterval / s.getTimeStep());
		this.stepOffset = (int) (timeOffset / s.getTimeStep());
		this.stepIterations = s.getIterations();
		finished = false;

		SimulationAnimationDummy simulationAnimationDummy = new SimulationAnimationDummy(s);

		if (panel != null) {
			PanelManagerDummy panelManagerDummy = new PanelManagerDummy(null, simulationAnimationDummy);
			component = panel.inflate(panelManagerDummy);

			if (component instanceof AnimationGLPanel) {
				animationGLPanel = (AnimationGLPanel) component;

				// Prepare offscreen OpenGL drawable
				GLProfile glp = GLProfile.getDefault();
				GLCapabilities caps = new GLCapabilities(glp);
				caps.setHardwareAccelerated(true);
				caps.setDoubleBuffered(false);
				//caps.setAlphaBits(8);
				caps.setRedBits(8);
				caps.setBlueBits(8);
				caps.setGreenBits(8);
				caps.setOnscreen(false);
				GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);

				glautodrawable = factory.createGLPbuffer(factory.getDefaultDevice(), caps, new DefaultGLCapabilitiesChooser(), width, height, null);
				glautodrawable.display();
				glautodrawable.getContext().makeCurrent();

				animationGLPanel.reshape(glautodrawable, 0, 0, width, height);

			} else if (component instanceof AnimationPanel) {
				// TODO: Can not yet draw non-OpenGL panels.
			}
		}
	}

	@Override
	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps)
			throws IOException {
		if (finished) {
			return;
		}
		if ((stepInterval > 0) && (steps - stepOffset >= 0) && ((steps - stepOffset) % stepInterval == 0)) {

			if (animationGLPanel != null) {
				glautodrawable.getContext().makeCurrent();

				animationGLPanel.display(glautodrawable);

				BufferedImage im = new AWTGLReadBufferUtil(glautodrawable.getGLProfile(), true).readPixelsToBufferedImage(glautodrawable.getGL(), 0, 0, width, height, true); 

				int counter = steps / stepInterval;
				String counterString = String.format("%05d", counter);
				String pathWithNumber = path.replace("{counter}", counterString);
				File file = getOutputFile(pathWithNumber);
				ImageIO.write(im, "png", file);
			}
		}
		if (steps >= stepIterations - 1) {
			finished = true;

			System.out.println("use: ffmpeg -r 25 -sameq -i img-%05d.png test_1.mov");
			// Create movie using e.g.
			// ffmpeg -r 25 -sameq -i img-%05d.png test_1.mov
		}
	}

	/** Creates a file with a given name in the output folder*/
	private File getOutputFile(String filename) {
		// Default output path is
		// 'output/' + filename
		File fullpath = new File("output");
		if(!fullpath.exists()) fullpath.mkdir();

		return new File(fullpath, filename);
	}

	class SimulationAnimationDummy extends SimulationAnimation {

		/**
		 * Alternative Constructor to create a dummy SimulationAnimation object as a wrapper for a panel.
		 *
		 * @param simulation  The simulation object.
		 */
		public SimulationAnimationDummy(Simulation simulation) {
			this.s = simulation;
		}
	}

	class PanelManagerDummy extends PanelManager {
		private SimulationAnimation simulationAnimation;

		PanelManagerDummy(MainControlApplet m, SimulationAnimation s) {
			super(m);
			simulationAnimation = s;
		}

		public SimulationAnimation getSimulationAnimation() {
			return simulationAnimation;
		}
	}
}
