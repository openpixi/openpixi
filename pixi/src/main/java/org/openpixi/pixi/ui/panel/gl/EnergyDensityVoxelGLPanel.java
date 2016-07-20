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
public class EnergyDensityVoxelGLPanel extends AnimationGLPanel {

	public ScaleProperties scaleProperties;

	public double phi;
	public double theta;

	/** Distance of viewer */
	public double distanceFactor;

	/** Maximum height of values */
	public double heightFactor;

	/** Constructor */
	public EnergyDensityVoxelGLPanel(SimulationAnimation simulationAnimation) {
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
		float sizex = (float) s.getSimulationBoxSize(0);
		float sizey = (float) s.getSimulationBoxSize(1);
		float sizez = (float) s.getSimulationBoxSize(2);
		float size = Math.max(Math.max(sizex, sizey), sizez);
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
				sizex / 2 + distance * Math.cos(phi) * Math.sin(theta), // where we stand
				sizey / 2 + distance * Math.sin(phi) * Math.sin(theta),
				sizez / 2 + distance * Math.cos(theta),
				sizex / 2, // where we are viewing at
				sizey / 2,
				sizez / 2,
				0, 0, 1); // "up" direction

		// Lattice spacing and coupling constant
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		int[] pos = new int[s.getNumberOfDimensions()];
		for(int w = 2; w < s.getNumberOfDimensions(); w++) {
			pos[w] = s.grid.getNumCells(w)/2;
		}

		double colors = s.grid.getNumberOfColors();

		for (int i = 0; i < s.grid.getNumCells(0); i++) {
			for (int k = 0; k < s.grid.getNumCells(1); k++) {
				for (int l = 0; l < s.grid.getNumCells(2); l++) {
					float x = (float)(as * i);
					float y = (float) (as * k);
					float z = (float) (as * l);

					pos[0] = i;
					pos[1] = k;
					pos[2] = l;
					int index = s.grid.getCellIndex(pos);

					double EfieldSquared = 0.0;
					double BfieldSquared = 0.0;
					float red = 0;
					float green = 0;
					float blue = 0;
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

					// Set color according to E-field, and brightness according
					// to total energy density:
					red = (float) Math.sqrt(red / norm) * value;
					green = (float) Math.sqrt(green / norm) * value;
					blue = (float) Math.sqrt(blue / norm) * value;

					scaleProperties.putValue(EfieldSquared + BfieldSquared);

					gl2.glColor3f( red, green, blue);
					if (value > 0.4) {
						drawCube(gl2, x, y, z, (float) as*.5f);
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

	/**
	 * See http://www.tutorialspoint.com/jogl/jogl_3d_cube.htm
	 * @param gl2
	 * @param x
	 * @param y
	 * @param z
	 */
	private void drawCube(GL2 gl2, float x, float y, float z, float size) {

		gl2.glPushMatrix();

		gl2.glTranslatef(x, y, z);
		gl2.glScalef(size,  size, size);

		gl2.glBegin(GL2.GL_QUADS); // Start Drawing The Cube
		gl2.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right Of The Quad (Top)
		gl2.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left Of The Quad (Top)
		gl2.glVertex3f(-1.0f, 1.0f, 1.0f); // Bottom Left Of The Quad (Top)
		gl2.glVertex3f(1.0f, 1.0f, 1.0f); // Bottom Right Of The Quad (Top)

		gl2.glVertex3f(1.0f, -1.0f, 1.0f); // Top Right Of The Quad
		gl2.glVertex3f(-1.0f, -1.0f, 1.0f); // Top Left Of The Quad
		gl2.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Left Of The Quad
		gl2.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Right Of The Quad

		gl2.glVertex3f(1.0f, 1.0f, 1.0f); // Top Right Of The Quad (Front)
		gl2.glVertex3f(-1.0f, 1.0f, 1.0f); // Top Left Of The Quad (Front)
		gl2.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Left Of The Quad
		gl2.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Right Of The Quad

		gl2.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Left Of The Quad
		gl2.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Right Of The Quad
		gl2.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Right Of The Quad (Back)
		gl2.glVertex3f(1.0f, 1.0f, -1.0f); // Top Left Of The Quad (Back)

		gl2.glVertex3f(-1.0f, 1.0f, 1.0f); // Top Right Of The Quad (Left)
		gl2.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left Of The Quad (Left)
		gl2.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Left Of The Quad
		gl2.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Right Of The Quad

		gl2.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right Of The Quad (Right)
		gl2.glVertex3f(1.0f, 1.0f, 1.0f); // Top Left Of The Quad
		gl2.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Left Of The Quad
		gl2.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Right Of The Quad
		gl2.glEnd(); // Done Drawing The Quad

		gl2.glPopMatrix();
	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Energy density 2D (OpenGL) panel");
		scaleProperties.addComponents(box);
	}
}