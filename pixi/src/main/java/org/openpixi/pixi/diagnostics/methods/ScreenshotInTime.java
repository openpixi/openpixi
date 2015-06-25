package org.openpixi.pixi.diagnostics.methods;

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
import org.openpixi.pixi.physics.gauge.CoulombGauge;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.gl.EnergyDensity3DGLPanel;

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
	private int width;
	private int height;

	private Simulation simulation;
	private SimulationAnimation simulationAnimation;
	private EnergyDensity3DGLPanel energyDensity3DGLPanel;

	GLAutoDrawable glautodrawable;

	public ScreenshotInTime(String path, double timeInterval, double timeOffset, int width, int height) {
		this.path = path;
		this.timeInterval = timeInterval;
		this.timeOffset = timeOffset;
		this.width = width;
		this.height = height;
	}

	@Override
	public void initialize(Simulation s) {
		this.stepInterval = (int) (timeInterval / s.getTimeStep());
		this.stepOffset = (int) (timeOffset / s.getTimeStep());
		this.simulation = s;
		this.simulationAnimation = new SimulationAnimation(s);
		this.energyDensity3DGLPanel = new EnergyDensity3DGLPanel(simulationAnimation);

		// TODO: Supply parameters from YAML
		energyDensity3DGLPanel.getScaleProperties().setScaleFactor(15);

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
	}

	@Override
	public void calculate(Grid grid, ArrayList<IParticle> particles, int steps)
			throws IOException {
		if ((steps - stepOffset) % stepInterval == 0) {

			glautodrawable.getContext().makeCurrent();

			energyDensity3DGLPanel.display(glautodrawable);

			BufferedImage im = new AWTGLReadBufferUtil(glautodrawable.getGLProfile(), true).readPixelsToBufferedImage(glautodrawable.getGL(), 0, 0, width, height, true); 

			int counter = steps / stepInterval;
			String counterString = String.format("%05d", counter);
			String pathWithNumber = path.replace("{counter}", counterString);
			File file = getOutputFile(pathWithNumber);
			ImageIO.write(im, "png", file);

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
}
