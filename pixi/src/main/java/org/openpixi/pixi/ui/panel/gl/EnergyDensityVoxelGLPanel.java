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

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.BooleanProperties;
import org.openpixi.pixi.ui.panel.properties.ComboBoxProperties;
import org.openpixi.pixi.ui.panel.properties.DoubleProperties;
import org.openpixi.pixi.ui.panel.properties.ScaleProperties;


/**
 * Displays 2D energy density in 3D view.
 */
public class EnergyDensityVoxelGLPanel extends AnimationGLPanel {

	public static final int INDEX_ENERGY_DENSITY = 0;
	public static final int INDEX_ENERGY_DENSITY_LONGITUDINAL_ELECTRIC = 1;
	public static final int INDEX_ENERGY_DENSITY_LONGITUDINAL_MAGNETIC = 2;
	public static final int INDEX_ENERGY_DENSITY_TRANSVERSE_ELECTRIC = 3;
	public static final int INDEX_ENERGY_DENSITY_TRANSVERSE_MAGNETIC = 4;
	public static final int INDEX_GAUSS_VIOLATION = 5;
	public static final int INDEX_U_LONGITUDINAL = 6;

	String[] dataLabel = new String[] {
			"Energy density",
			"Energy density longitudinal electric",
			"Energy density longitudinal magnetic",
			"Energy density transverse electric",
			"Energy density transverse magnetic",
			"Gauss violation",
			"U (along direction)"
	};

	String[] directionLabel = {
			"x",
			"y",
			"z"};

	public static final int RED = 0;
	public static final int GREEN = 1;
	public static final int BLUE = 2;

	public ComboBoxProperties dataProperties;
	public ComboBoxProperties directionProperties;
	public ScaleProperties scaleProperties;
	public DoubleProperties visibilityThresholdProperties;
	public DoubleProperties opacityProperties;
	public BooleanProperties showSimulationBoxProperties;
	public BooleanProperties whiteBackgroundProperties;

	public double phi;
	public double theta;

	public double centerx;
	public double centery;
	public double centerz;

	/** Distance of viewer */
	public double distanceFactor;

	public boolean shiftKeyPressed = false;

	/** Constructor */
	public EnergyDensityVoxelGLPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
		dataProperties = new ComboBoxProperties(simulationAnimation, "Data", dataLabel, 0);
		directionProperties = new ComboBoxProperties(simulationAnimation, "Field direction", directionLabel, 0);
		scaleProperties = new ScaleProperties(simulationAnimation);
		visibilityThresholdProperties = new DoubleProperties(simulationAnimation, "Visibility threshold", 0.0);
		opacityProperties = new DoubleProperties(simulationAnimation, "Opacity", 1);
		showSimulationBoxProperties = new BooleanProperties(simulationAnimation, "Show simulation box", false);
		whiteBackgroundProperties = new BooleanProperties(simulationAnimation, "White background", false);

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
		gl2.glLoadIdentity();

		// coordinate system origin at lower left with width and height same as the window
		GLU glu = new GLU();

		int dataIndex = dataProperties.getIndex();
		int direction = directionProperties.getIndex();

		double scale = scaleProperties.getScale();
		scaleProperties.resetAutomaticScale();
		Simulation s = getSimulationAnimation().getSimulation();

		double visibilityThreshold = visibilityThresholdProperties.getValue();
		double opacity = opacityProperties.getValue();
		boolean showSimulationBox = showSimulationBoxProperties.getValue();
		boolean whiteBackground = whiteBackgroundProperties.getValue();

		// Perspective.
		float sizex = (float) s.getSimulationBoxSize(0);
		float sizey = (float) s.getSimulationBoxSize(1);
		float sizez = (float) s.getSimulationBoxSize(2);
		float size = (float) Math.sqrt(sizex * sizex + sizey * sizey + sizez * sizez);
		float distance = (float) distanceFactor * size;
		float widthHeightRatio = (float) width / (float) height;

		if (whiteBackground) {
			gl2.glClearColor(1, 1, 1, 1);
		} else {
			gl2.glClearColor(0, 0, 0, 1);
		}
		gl2.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );

		gl2.glMatrixMode( GL2.GL_PROJECTION );
		gl2.glLoadIdentity();
		glu.gluPerspective(
				45, // field of view angle, in degrees
				widthHeightRatio, // aspect ratio of field of view
				1, // distance to near clipping plane
				2.5 * distance); // distance to far clipping plane
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

		// Enable lighting
		gl2.glEnable(GL2.GL_LIGHTING);

		// Enable color material
		gl2.glEnable(GL2.GL_COLOR_MATERIAL);
		gl2.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);

		float specReflection[] = { .8f, .8f, .8f, 1.0f };
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specReflection, 0);
		gl2.glMateriali(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 64);

		// Light source 0
		float light0_ambient[] = { .3f, .3f, .3f, 1f };
		float light0_diffuse[] = { .7f, .7f, .7f, 1f };
		float light0_specular[] = { .8f, .8f, .8f, 1.0f };
		float light0_position[] = { 10 * size, 20 * size, 40 * size, 0.0f };

		gl2.glEnable(GL2.GL_LIGHT0);
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, light0_ambient, 0);
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light0_diffuse, 0);
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, light0_specular, 0);
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light0_position, 0);

		// Light source 1
		float light1_ambient[] = { 0f, 0f, 0f, 1f };
		float light1_diffuse[] = { .5f, .5f, .5f, 1.0f };
		float light1_specular[] = { .5f, .5f, .5f, 1.0f };
		float light1_position[] = { -20 * size, -40 * size, -10 * size, 0.0f };

		gl2.glEnable(GL2.GL_LIGHT1);
		gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, light1_ambient, 0);
		gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, light1_diffuse, 0);
		gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, light1_specular, 0);
		gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, light1_position, 0);

		// Lattice spacing
		double as = s.grid.getLatticeSpacing();

		int[] pos = new int[s.getNumberOfDimensions()];
		for(int w = 2; w < s.getNumberOfDimensions(); w++) {
			pos[w] = s.grid.getNumCells(w)/2;
		}

		if (showSimulationBox) {
			double thickness = as * 0.05;

			// Translation wireframe
			if (shiftKeyPressed) {
				gl2.glColor3f( .5f, 0, 0);
				drawCubeWireframe(gl2, centerx - as * 0.5, centery - as * 0.5, centerz - as * 0.5,
						sizex, sizey, sizez, thickness);
			}

			// Wireframe of simulation box
			gl2.glColor3f( .5f, .5f, .5f);
			drawCubeWireframe(gl2, -as * 0.5, -as * 0.5, -as * 0.5,
					sizex, sizey, sizez, thickness);
		}

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

		double[] color = new double[3];

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

					double value = 0;
					color[RED] = 0;
					color[GREEN] = 0;
					color[BLUE] = 0;
					double alpha = 0;
					if(s.grid.isEvaluatable(index)) {
						switch(dataIndex) {
						case INDEX_ENERGY_DENSITY:
							value = getEnergyDensity(s, index, color, direction, true, true, true, true);
							break;
						case INDEX_ENERGY_DENSITY_LONGITUDINAL_ELECTRIC:
							value = getEnergyDensity(s, index, color, direction, true, false, true, false);
							break;
						case INDEX_ENERGY_DENSITY_LONGITUDINAL_MAGNETIC:
							value = getEnergyDensity(s, index, color, direction, true, false, false, true);
							break;
						case INDEX_ENERGY_DENSITY_TRANSVERSE_ELECTRIC:
							value = getEnergyDensity(s, index, color, direction, false, true, true, false);
							break;
						case INDEX_ENERGY_DENSITY_TRANSVERSE_MAGNETIC:
							value = getEnergyDensity(s, index, color, direction, false, true, false, true);
							break;
						case INDEX_GAUSS_VIOLATION:
							value = getGaussViolation(s, index, color);
							break;
						case INDEX_U_LONGITUDINAL:
							value = getU(s, index, color, direction);
							break;
						}
					}
					// Normalize
					double norm = Math.max(color[RED] + color[GREEN] + color[BLUE], 10E-20);
					double limitedValue = Math.min(1, scale * Math.abs(value));

					// Set color according to E-field, and transparency according
					// to total energy density:
					color[RED] = Math.sqrt(color[RED] / norm);
					color[GREEN] = Math.sqrt(color[GREEN] / norm);
					color[BLUE] = Math.sqrt(color[BLUE] / norm);
					alpha = limitedValue * opacity;

					scaleProperties.putValue(value);

					gl2.glColor4d( color[RED], color[GREEN], color[BLUE], alpha);
					if (limitedValue >= visibilityThreshold) {
						drawCube(gl2, x, y, z, (float) as * .5f, (float) viewx, (float) viewy, (float) viewz);
					}
				}
			}
		}
		scaleProperties.calculateAutomaticScale(1.0);
	}

	private void drawCubeWireframe(GL2 gl2, double x, double y, double z, double sizex, double sizey, double sizez, double thickness) {
		gl2.glPushMatrix();
		gl2.glTranslated(x, y, z);

		gl2.glBegin(GL2.GL_QUADS);

		gl2.glVertex3d(0, 0, 0);
		gl2.glVertex3d(sizex, 0, 0);
		gl2.glVertex3d(sizex, 0, thickness);
		gl2.glVertex3d(0, 0, thickness);

		gl2.glVertex3d(0, 0, 0);
		gl2.glVertex3d(0, sizey, 0);
		gl2.glVertex3d(0, sizey, thickness);
		gl2.glVertex3d(0, 0, thickness);

		gl2.glVertex3d(0, sizey, 0);
		gl2.glVertex3d(sizex, sizey, 0);
		gl2.glVertex3d(sizex, sizey, thickness);
		gl2.glVertex3d(0, sizey, thickness);

		gl2.glVertex3d(sizex, 0, 0);
		gl2.glVertex3d(sizex, sizey, 0);
		gl2.glVertex3d(sizex, sizey, thickness);
		gl2.glVertex3d(sizex, 0, thickness);

		gl2.glVertex3d(0, 0, 0);
		gl2.glVertex3d(0, 0, sizez);
		gl2.glVertex3d(0, thickness, sizez);
		gl2.glVertex3d(0, thickness, 0);

		gl2.glVertex3d(sizex, 0, 0);
		gl2.glVertex3d(sizex, 0, sizez);
		gl2.glVertex3d(sizex, thickness, sizez);
		gl2.glVertex3d(sizex, thickness, 0);

		gl2.glVertex3d(0, sizey, 0);
		gl2.glVertex3d(0, sizey, sizez);
		gl2.glVertex3d(0, sizey + thickness, sizez);
		gl2.glVertex3d(0, sizey + thickness, 0);

		gl2.glVertex3d(sizex, sizey, 0);
		gl2.glVertex3d(sizex, sizey, sizez);
		gl2.glVertex3d(sizex, sizey + thickness, sizez);
		gl2.glVertex3d(sizex, sizey + thickness, 0);

		gl2.glVertex3d(0, 0, sizez);
		gl2.glVertex3d(sizex, 0, sizez);
		gl2.glVertex3d(sizex, 0, sizez + thickness);
		gl2.glVertex3d(0, 0, sizez + thickness);

		gl2.glVertex3d(0, 0, sizez);
		gl2.glVertex3d(0, sizey, sizez);
		gl2.glVertex3d(0, sizey, sizez + thickness);
		gl2.glVertex3d(0, 0, sizez + thickness);

		gl2.glVertex3d(0, sizey, sizez);
		gl2.glVertex3d(sizex, sizey, sizez);
		gl2.glVertex3d(sizex, sizey, sizez + thickness);
		gl2.glVertex3d(0, sizey, sizez + thickness);

		gl2.glVertex3d(sizex, 0, sizez);
		gl2.glVertex3d(sizex, sizey, sizez);
		gl2.glVertex3d(sizex, sizey, sizez + thickness);
		gl2.glVertex3d(sizex, 0, sizez + thickness);

		gl2.glEnd();
		gl2.glPopMatrix();
	}

	/**
	 * Obtain energy density and corresponding color.
	 * Longitudinal / transverse, electric / magnetic components can be specified.
	 * @param s
	 * @param index
	 * @param color Returns color in a vector
	 * @param direction Longitudinal direction
	 * @param longitudinal Along 'direction'
	 * @param transverse Sum of all directions except 'direction'
	 * @param electric
	 * @param magnetic
	 * @return
	 */
	private double getEnergyDensity(Simulation s, int index, double[] color, int direction, boolean longitudinal, boolean transverse, boolean electric, boolean magnetic) {
		float red = 0;
		float green = 0;
		float blue = 0;

		double EfieldSquared = 0.0;
		double BfieldSquared = 0.0;

		// Lattice spacing and coupling constant
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		double colors = s.grid.getNumberOfColors();

		for (int w = 0; w < s.getNumberOfDimensions(); w++) {
			if ((longitudinal && w == direction) || (transverse && w != direction)) {
				if (electric) {
					EfieldSquared += s.grid.getEsquaredFromLinks(index, w) / (as * g * as * g) / 2;
				}
				if (magnetic) {
					// Time averaging for B field.
					BfieldSquared += s.grid.getBsquaredFromLinks(index, w, 0) / (as * g * as * g) / 4.0;
					BfieldSquared += s.grid.getBsquaredFromLinks(index, w, 1) / (as * g * as * g) / 4.0;
				}
				// get color:
				double c = 0;
				for (int n = 0; n < colors * colors - 1; n++) {
					if (electric) {
						c = s.grid.getE(index, w).get(n);
					} else if (magnetic) {
						c = s.grid.getB(index, w, 0).get(n);
					}
					// cycle through colors if there are more than three
					switch (n % 3) {
						case 0:
							red += c * c;
							break;
						case 1:
							green += c * c;
							break;
						case 2:
							blue += c * c;
							break;
					}
				}
			}
		}
		color[RED] = red;
		color[GREEN] = green;
		color[BLUE] = blue;
		return EfieldSquared + BfieldSquared;
	}

	private double getGaussViolation(Simulation s, int index, double[] color) {
		AlgebraElement gaussAlg = s.grid.getGaussConstraint(index);

		double value = gaussAlg.square();

		color[RED] = Math.pow(gaussAlg.get(0), 2);
		color[GREEN] = Math.pow(gaussAlg.get(1), 2);
		color[BLUE] = Math.pow(gaussAlg.get(2), 2);
		return value;
	}

	private double getU(Simulation s, int index, double[] color, int direction) {
		AlgebraElement U = s.grid.getU(index, direction).proj();

		double value = U.square();

		color[RED] = Math.pow(U.get(0), 2);
		color[GREEN] = Math.pow(U.get(1), 2);
		color[BLUE] = Math.pow(U.get(2), 2);
		return value;
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
			shiftKeyPressed = false;
			if (e.isControlDown()) {
				// Change distance (Ctrl key)
				double factor = 0.01;
				double prevDistanceFactor = distanceFactor;
				distanceFactor -= factor * deltaY;
				if (distanceFactor <= 0) {
					distanceFactor = prevDistanceFactor;
				}
			} else if (e.isShiftDown()) {
				// Translate scene (Shift key)
				shiftKeyPressed = true;
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
				double oldTheta = theta;
				theta -= factor * deltaY;
				if ((theta <= 0) || (theta > Math.PI)) {
					theta = oldTheta;
				}
			}
			mouseOldX = e.getX();
			mouseOldY = e.getY();
			super.mouseDragged(e);
			simulationAnimation.repaint();
		}

		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			shiftKeyPressed = false;
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
			gl2.glNormal3f(1f, 0, 0);
			gl2.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right Of The Quad (Right)
			gl2.glVertex3f(1.0f, 1.0f, 1.0f); // Top Left Of The Quad
			gl2.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Left Of The Quad
			gl2.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Right Of The Quad
		} else {
			gl2.glNormal3f(-1f, 0, 0);
			gl2.glVertex3f(-1.0f, 1.0f, 1.0f); // Top Right Of The Quad (Left)
			gl2.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left Of The Quad (Left)
			gl2.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Left Of The Quad
			gl2.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Right Of The Quad
		}

		if (viewy > 0) {
			gl2.glNormal3f(0, 1f, 0);
			gl2.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right Of The Quad (Top)
			gl2.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left Of The Quad (Top)
			gl2.glVertex3f(-1.0f, 1.0f, 1.0f); // Bottom Left Of The Quad (Top)
			gl2.glVertex3f(1.0f, 1.0f, 1.0f); // Bottom Right Of The Quad (Top)
		} else {
			gl2.glNormal3f(0, -1f, 0);
			gl2.glVertex3f(1.0f, -1.0f, 1.0f); // Top Right Of The Quad
			gl2.glVertex3f(-1.0f, -1.0f, 1.0f); // Top Left Of The Quad
			gl2.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Left Of The Quad
			gl2.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Right Of The Quad
		}

		if (viewz > 0) {
			gl2.glNormal3f(0, 0, 1f);
			gl2.glVertex3f(1.0f, 1.0f, 1.0f); // Top Right Of The Quad (Front)
			gl2.glVertex3f(-1.0f, 1.0f, 1.0f); // Top Left Of The Quad (Front)
			gl2.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Left Of The Quad
			gl2.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Right Of The Quad
		} else {
			gl2.glNormal3f(0, 0, -1f);
			gl2.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Left Of The Quad
			gl2.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Right Of The Quad
			gl2.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Right Of The Quad (Back)
			gl2.glVertex3f(1.0f, 1.0f, -1.0f); // Top Left Of The Quad (Back)
		}

		gl2.glEnd(); // Done Drawing The Quad

		gl2.glPopMatrix();
	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Energy density Voxel (OpenGL) panel");
		dataProperties.addComponents(box);
		directionProperties.addComponents(box);
		scaleProperties.addComponents(box);
		visibilityThresholdProperties.addComponents(box);
		opacityProperties.addComponents(box);
		showSimulationBoxProperties.addComponents(box);
		whiteBackgroundProperties.addComponents(box);
	}
}