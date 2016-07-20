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
import org.openpixi.pixi.ui.panel.properties.DoubleProperties;
import org.openpixi.pixi.ui.panel.properties.ScaleProperties;


/**
 * Displays 2D energy density in 3D view.
 */
public class EnergyDensityVoxelGLPanel extends AnimationGLPanel {

	public ScaleProperties scaleProperties;
	public DoubleProperties visibilityThresholdProperties;
	public DoubleProperties opacityProperties;

	public double phi;
	public double theta;

	public double centerx;
	public double centery;
	public double centerz;

	/** Distance of viewer */
	public double distanceFactor;

	/** Constructor */
	public EnergyDensityVoxelGLPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
		scaleProperties = new ScaleProperties(simulationAnimation);
		visibilityThresholdProperties = new DoubleProperties(simulationAnimation, "Visibility threshold", 0.0);
		opacityProperties = new DoubleProperties(simulationAnimation, "Opacity", 1);

		MouseListener l = new MouseListener();
		addMouseListener(l);
		addMouseMotionListener(l);

		phi = - 0.5 * Math.PI;
		theta = Math.PI * 0.25;
		distanceFactor = 1;

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

		double visibilityThreshold = visibilityThresholdProperties.getValue();
		double opacity = opacityProperties.getValue();

		// Perspective.
		float sizex = (float) s.getSimulationBoxSize(0);
		float sizey = (float) s.getSimulationBoxSize(1);
		float sizez = (float) s.getSimulationBoxSize(2);
		float size = Math.max(Math.max(sizex, sizey), sizez);
		float distance = (float) distanceFactor * size;
		float widthHeightRatio = (float) width / (float) height;

		gl2.glMatrixMode( GL2.GL_PROJECTION );
		gl2.glLoadIdentity();
		glu.gluPerspective(
				45, // field of view angle, in degrees
				widthHeightRatio, // aspect ratio of field of view
				1, // distance to near clipping plane
				2.5 * size); // distance to far clipping plane
		double viewx = Math.cos(phi) * Math.sin(theta);
		double viewy = Math.sin(phi) * Math.sin(theta);
		double viewz = Math.cos(theta);
		glu.gluLookAt(
				centerx + sizex / 2 + distance * viewx, // where we stand
				centery + sizey / 2 + distance * viewy,
				centerz + sizez / 2 + distance * viewz,
				centerx + sizex / 2, // where we are viewing at
				centery + sizey / 2,
				centerz + sizez / 2,
				0, 0, 1); // "up" direction

		// Turn on transparent drawing
		gl2.glEnable(GL.GL_BLEND);
		gl2.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		// Lattice spacing and coupling constant
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		int[] pos = new int[s.getNumberOfDimensions()];
		for(int w = 2; w < s.getNumberOfDimensions(); w++) {
			pos[w] = s.grid.getNumCells(w)/2;
		}

		double colors = s.grid.getNumberOfColors();

		// Determine order of drawing which is important for transparent drawing
		int loop1 = 0; // outermost loop
		int loop2 = 1;
		int loop3 = 2; // innermost loop

		double ax = Math.abs(viewx);
		double ay = Math.abs(viewy);
		double az = Math.abs(viewz);

		if (ax < ay) {
			if (ay < az) {
				// ax < ay < az
				loop1 = 2;
				loop2 = 1;
				loop3 = 0;
			} else {
				if (ax < az) {
					// ax < az <= ay
					loop1 = 1;
					loop2 = 2;
					loop3 = 0;
				} else {
					// az <= ax < ay
					loop1 = 1;
					loop2 = 0;
					loop3 = 2;
				}
			}
		} else {
			if (ax < az) {
				// ay <= ax < az
				loop1 = 2;
				loop2 = 0;
				loop3 = 1;
			} else {
				if (ay < az) {
					// ay < az <= ax
					loop1 = 0;
					loop2 = 2;
					loop3 = 1;
				} else {
					// az <= ay <= ax
					loop1 = 0;
					loop2 = 1;
					loop3 = 2;
				}
			}
		}

		boolean[] increasing = new boolean[3];
		increasing[0] = true;
		increasing[1] = true;
		increasing[2] = true;

		if (viewx < 0) increasing[0] = false;
		if (viewy < 0) increasing[1] = false;
		if (viewz < 0) increasing[2] = false;

		for (int i = 0; i < s.grid.getNumCells(loop1); i++) {
			for (int k = 0; k < s.grid.getNumCells(loop2); k++) {
				for (int l = 0; l < s.grid.getNumCells(loop3); l++) {
					pos[loop1] = increasing[loop1] ? i : s.grid.getNumCells(loop1) - i - 1;
					pos[loop2] = increasing[loop2] ? k : s.grid.getNumCells(loop2) - k - 1;
					pos[loop3] = increasing[loop3] ? l : s.grid.getNumCells(loop3) - l - 1;
					int index = s.grid.getCellIndex(pos);

					float x = (float)(as * pos[0]);
					float y = (float) (as * pos[1]);
					float z = (float) (as * pos[2]);

					double EfieldSquared = 0.0;
					double BfieldSquared = 0.0;
					float red = 0;
					float green = 0;
					float blue = 0;
					float alpha = 0;
					if(s.grid.isEvaluatable(index)) {
						for (int w = 0; w < s.getNumberOfDimensions(); w++) {
							EfieldSquared += s.grid.getEsquaredFromLinks(index, w) / (as * g * as * g) / 2;
							// Time averaging for B field.
							BfieldSquared += s.grid.getBsquaredFromLinks(index, w, 0) / (as * g * as * g) / 4.0;
							BfieldSquared += s.grid.getBsquaredFromLinks(index, w, 1) / (as * g * as * g) / 4.0;
							// get color:
							double color;
							for (int n = 0; n < colors * colors - 1; n++) {
								color = s.grid.getE(index, w).get(n);
								// cycle through colors if there are more than three
								switch (n % 3) {
									case 0:
										red += color * color;
										break;
									case 1:
										green += color * color;
										break;
									case 2:
										blue += color * color;
										break;
								}
							}
						}
					}
					// Normalize
					double norm = Math.max(red + green + blue, 10E-20);
					float value = (float) Math.min(1, scale * (EfieldSquared + BfieldSquared));

					// Set color according to E-field, and transparency according
					// to total energy density:
					red = (float) Math.sqrt(red / norm);
					green = (float) Math.sqrt(green / norm);
					blue = (float) Math.sqrt(blue / norm);
					alpha = value * (float) opacity;

					scaleProperties.putValue(EfieldSquared + BfieldSquared);

					gl2.glColor4f( red, green, blue, alpha);
					if (value >= visibilityThreshold) {
						drawCube(gl2, x, y, z, (float) as * .5f, (float) viewx, (float) viewy, (float) viewz);
					}
				}
			}
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
			if (e.isControlDown()) {
				// Change distance (Ctrl key)
				double factor = 0.01;
				distanceFactor -= factor * deltaY;
			} else if (e.isShiftDown()) {
				// Translate scene (Shift key)
				double factor = 0.1;
				double shiftphi = factor * deltaX;
				double shifttheta = factor * deltaY;
				double vectorphix = shiftphi * Math.sin(phi);
				double vectorphiy = -shiftphi * Math.cos(phi);
				double vectorphiz = 0;
				double vectorthetax = -shifttheta * Math.cos(phi) * Math.cos(theta);
				double vectorthetay = -shifttheta * Math.sin(phi) * Math.cos(theta);
				double vectorthetaz = shifttheta * Math.sin(theta);
				centerx += vectorphix + vectorthetax;
				centery += vectorphiy + vectorthetay;
				centerz += vectorphiz + vectorthetaz;
			} else {
				// No modifiers used:
				// Rotate scene
				double factor = 0.01;
				phi -= factor * deltaX;
				theta -= factor * deltaY;
			}
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

	/**
	 * See http://www.tutorialspoint.com/jogl/jogl_3d_cube.htm
	 *
	 * Only draw visible sides (approximately)
	 */
	private void drawCube(GL2 gl2, float x, float y, float z, float size, float viewx, float viewy, float viewz) {

		gl2.glPushMatrix();

		gl2.glTranslatef(x, y, z);
		gl2.glScalef(size,  size, size);

		gl2.glBegin(GL2.GL_QUADS); // Start Drawing The Cube

		if (viewx > 0) {
			gl2.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right Of The Quad (Right)
			gl2.glVertex3f(1.0f, 1.0f, 1.0f); // Top Left Of The Quad
			gl2.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Left Of The Quad
			gl2.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Right Of The Quad
		} else {
			gl2.glVertex3f(-1.0f, 1.0f, 1.0f); // Top Right Of The Quad (Left)
			gl2.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left Of The Quad (Left)
			gl2.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Left Of The Quad
			gl2.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Right Of The Quad
		}

		if (viewy > 0) {
			gl2.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right Of The Quad (Top)
			gl2.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left Of The Quad (Top)
			gl2.glVertex3f(-1.0f, 1.0f, 1.0f); // Bottom Left Of The Quad (Top)
			gl2.glVertex3f(1.0f, 1.0f, 1.0f); // Bottom Right Of The Quad (Top)
		} else {
			gl2.glVertex3f(1.0f, -1.0f, 1.0f); // Top Right Of The Quad
			gl2.glVertex3f(-1.0f, -1.0f, 1.0f); // Top Left Of The Quad
			gl2.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Left Of The Quad
			gl2.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Right Of The Quad
		}

		if (viewz > 0) {
			gl2.glVertex3f(1.0f, 1.0f, 1.0f); // Top Right Of The Quad (Front)
			gl2.glVertex3f(-1.0f, 1.0f, 1.0f); // Top Left Of The Quad (Front)
			gl2.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Left Of The Quad
			gl2.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Right Of The Quad
		} else {
			gl2.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Left Of The Quad
			gl2.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Right Of The Quad
			gl2.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Right Of The Quad (Back)
			gl2.glVertex3f(1.0f, 1.0f, -1.0f); // Top Left Of The Quad (Back)
		}

		gl2.glEnd(); // Done Drawing The Quad

		gl2.glPopMatrix();
	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Energy density 2D (OpenGL) panel");
		scaleProperties.addComponents(box);
		visibilityThresholdProperties.addComponents(box);
		opacityProperties.addComponents(box);
	}
}