/*
 * OpenPixi - Open Particle-In-Cell (PIC) Simulator
 * Copyright (C) 2012  OpenPixi.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openpixi.pixi.ui.panel.gl;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.swing.Box;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.ScaleProperties;


/**
 * Displays 2D energy density in 3D view.
 */
public class EnergyDensity3DGLPanel extends AnimationGLPanel {

	ScaleProperties scaleProperties;

	public double phi;
	public double theta;

	/** Distance of viewer */
	public double distanceFactor;

	/** Maximum height of values */
	public double heightFactor;

	/** Constructor */
	public EnergyDensity3DGLPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
		scaleProperties = new ScaleProperties(simulationAnimation);

		MouseListener l = new MouseListener();
		addMouseListener(l);
		addMouseMotionListener(l);

		phi = - 0.5 * Math.PI;
		theta = Math.PI * 0.25;
		distanceFactor = 1;
		heightFactor = .25;

		scaleProperties.setAutomaticScaling(true);
	}

	@Override
	public void reshape(GLAutoDrawable glautodrawable, int x, int y,
			int width, int height) {
		// Set up perspective below
	}

	@Override
	public void display(GLAutoDrawable glautodrawable) {
		GL2 gl2 = glautodrawable.getGL().getGL2();
		int width = glautodrawable.getWidth();
		int height = glautodrawable.getHeight();
		gl2.glEnable(GL.GL_DEPTH_TEST);
		gl2.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
		gl2.glLoadIdentity();

		// coordinate system origin at lower left with width and height same as the window
		GLU glu = new GLU();

		double scale = scaleProperties.getScale();
		scaleProperties.resetAutomaticScale();
		Simulation s = getSimulationAnimation().getSimulation();

		// Perspective.
		float size = (float) Math.max(s.getWidth(), s.getHeight());
		float distance = (float) distanceFactor * size;
		float widthHeightRatio = (float) width / (float) height;

		// Scaling for height:
		float heightScale = (float) heightFactor * size;

		gl2.glMatrixMode( GL2.GL_PROJECTION );
		gl2.glLoadIdentity();
		glu.gluPerspective(
				45, // field of view angle, in degrees
				widthHeightRatio, // aspect ratio of field of view
				1, // distance to near clipping plane
				2.5 * size); // distance to far clipping plane
		glu.gluLookAt(
				s.getWidth() / 2 + distance * Math.cos(phi) * Math.sin(theta), s.getHeight() / 2 + distance * Math.sin(phi) * Math.sin(theta), distance * Math.cos(theta), // where we stand
				s.getWidth() / 2, s.getHeight() / 2, 0, // where we are viewing at
				0, 0, 1); // "up" direction

		/** Scaling factor for the displayed panel in x-direction*/
		double sx = 1; //getWidth() / s.getWidth();
		/** Scaling factor for the displayed panel in y-direction*/
		double sy = 1; //getHeight() / s.getHeight();

		// Lattice spacing and coupling constant
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		int[] pos = new int[s.getNumberOfDimensions()];
		for(int w = 2; w < s.getNumberOfDimensions(); w++) {
			pos[w] = s.grid.getNumCells(w)/2;
		}

		float[] previousValue = new float[s.grid.getNumCells(1)];
		float[] previousRed = new float[s.grid.getNumCells(1)];
		float[] previousGreen = new float[s.grid.getNumCells(1)];
		float[] previousBlue = new float[s.grid.getNumCells(1)];

		for(int i = 0; i < s.grid.getNumCells(0); i++) {
			gl2.glBegin( GL2.GL_QUAD_STRIP );
			for(int k = 0; k < s.grid.getNumCells(1); k++)
			{
				//float xstart = (float) (s.grid.getLatticeSpacing() * (i + 0.5) * sx);
				float xstart2 = (float)(s.grid.getLatticeSpacing() * i * sx);
				float xstart3 = (float)(s.grid.getLatticeSpacing() * (i + 1) * sx);
				//float ystart = (float) (s.grid.getLatticeSpacing() * (k + 0.5) * sy);
				float ystart2 = (float) (s.grid.getLatticeSpacing() * k * sy);

				pos[0] = i;
				pos[1] = k;
				int index = s.grid.getCellIndex(pos);

				double EfieldSquared = 0.0;
				double BfieldSquared = 0.0;
				float red = 0;
				float green = 0;
				float blue = 0;
				for(int w = 0; w < s.getNumberOfDimensions(); w++) {
					EfieldSquared += s.grid.getEsquaredFromLinks(index, w) / (as * g * as * g) / 2;
					// Time averaging for B field.
					BfieldSquared += s.grid.getBsquaredFromLinks(index, w, 0) / (as * g * as * g) / 4.0;
					BfieldSquared += s.grid.getBsquaredFromLinks(index, w, 1) / (as * g * as * g) / 4.0;
					// get color:
					double color = s.grid.getE(index, w).get(0);
					red += color * color;
					color = s.grid.getE(index, w).get(1);
					green += color * color;
					color = s.grid.getE(index, w).get(2);
					blue += color * color;
				}
				// Normalize
				double norm = red + green + blue;
				float value = (float) Math.min(1, scale * (EfieldSquared + BfieldSquared));

				// Set color according to E-field, and brightness according
				// to total energy density:
				red = (float) Math.sqrt(red / norm) * value;
				green = (float) Math.sqrt(green / norm) * value;
				blue = (float) Math.sqrt(blue / norm) * value;

				scaleProperties.putValue(EfieldSquared + BfieldSquared);

				if (k > 0) {
					gl2.glColor3f( previousRed[k], previousGreen[k], previousBlue[k]);
					gl2.glVertex3f( xstart2, ystart2, heightScale * previousValue[k]);
					gl2.glColor3f( red, green, blue);
					gl2.glVertex3f( xstart3, ystart2, heightScale * (float) value);
				}
				previousValue[k] = (float) value;
				previousRed[k] = (float) red;
				previousGreen[k] = (float) green;
				previousBlue[k] = (float) blue;
			}
			gl2.glEnd();
		}
		scaleProperties.calculateAutomaticScale(1.0);
	}

	private int mouseOldX, mouseOldY;

	class MouseListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			//System.out.println("Pressed "+e.getX() + " : " + e.getY());
			mouseOldX = e.getX();
			mouseOldY = e.getY();
			super.mousePressed(e);
		}

		public void mouseDragged(MouseEvent e) {
			//System.out.println("D "+e.getX() + " : " + e.getY());
			double deltaX = e.getX() - mouseOldX;
			double deltaY = e.getY() - mouseOldY;
			double factor = 0.01;
			phi -= factor * deltaX;
			theta -= factor * deltaY;
			mouseOldX = e.getX();
			mouseOldY = e.getY();
			super.mouseDragged(e);
			simulationAnimation.repaint();
		}

		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			simulationAnimation.repaint();
		}
	}


	public void addPropertyComponents(Box box) {
		addLabel(box, "Energy density 2D (OpenGL) panel");
		scaleProperties.addComponents(box);
	}

	public ScaleProperties getScaleProperties() {
		return scaleProperties;
	}
}