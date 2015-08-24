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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.Box;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.ScaleProperties;


/**
 * Displays 2D energy density.
 */
public class EnergyDensity2DGLPanel extends AnimationGLPanel {

	ScaleProperties scaleProperties;

	/** Constructor */
	public EnergyDensity2DGLPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
		scaleProperties = new ScaleProperties(simulationAnimation);

		scaleProperties.setAutomaticScaling(true);
	}

	@Override
	public void display(GLAutoDrawable glautodrawable) {
		GL2 gl2 = glautodrawable.getGL().getGL2();
		int width = glautodrawable.getWidth();
		int height = glautodrawable.getHeight();
		gl2.glClear( GL.GL_COLOR_BUFFER_BIT );
		gl2.glLoadIdentity();

		double scale = scaleProperties.getScale();
		scaleProperties.resetAutomaticScale();
		Simulation s = getSimulationAnimation().getSimulation();

		/** Scaling factor for the displayed panel in x-direction*/
		double sx = width / s.getWidth();
		/** Scaling factor for the displayed panel in y-direction*/
		double sy = height / s.getHeight();

		// Lattice spacing and coupling constant
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		int[] pos = new int[s.getNumberOfDimensions()];
		for(int w = 2; w < s.getNumberOfDimensions(); w++) {
			pos[w] = s.grid.getNumCells(w)/2;
		}

		for(int i = 0; i < s.grid.getNumCells(0); i++) {
			gl2.glBegin( GL2.GL_QUAD_STRIP );
			for(int k = 0; k < s.grid.getNumCells(1); k++)
			{
				int xstart = (int) (s.grid.getLatticeSpacing() * (i + 0.5) * sx);
				int xstart2 = (int)(s.grid.getLatticeSpacing() * i * sx);
				int xstart3 = (int)(s.grid.getLatticeSpacing() * (i + 1) * sx);
				int ystart = (int) (s.grid.getLatticeSpacing() * (k + 0.5) * sy);
				int ystart2 = (int) (s.grid.getLatticeSpacing() * k * sy);

				pos[0] = i;
				pos[1] = k;
				int index = s.grid.getCellIndex(pos);

				double EfieldSquared = 0.0;
				double BfieldSquared = 0.0;
				double red = 0;
				double green = 0;
				double blue = 0;
				for(int w = 0; w < s.getNumberOfDimensions(); w++) {
					EfieldSquared += s.grid.getEsquaredFromLinks(index, w) / (as * g * as * g) / 2;
					// Time averaging for B field.
					BfieldSquared += s.grid.getBsquaredFromLinks(index, w, 0) / (as * g * as * g) / 4.0;
					BfieldSquared += s.grid.getBsquaredFromLinks(index, w, 1) / (as * g * as * g) / 4.0;
					// get color:
					double color = s.grid.getE(index, w).get(0);
					red += color * color;
					color = s.grid.getE(index, w).get(3);
					red += color * color;
					color = s.grid.getE(index, w).get(6);
					red += color * color;

					color = s.grid.getE(index, w).get(1);
					green += color * color;
					color = s.grid.getE(index, w).get(4);
					green += color * color;
					color = s.grid.getE(index, w).get(7);
					green += color * color;

					color = s.grid.getE(index, w).get(2);
					blue += color * color;
					color = s.grid.getE(index, w).get(5);
					blue += color * color;
				}
				// Normalize
				double norm = red + green + blue;
				double value = Math.min(1, scale * (EfieldSquared + BfieldSquared));

				// Set color according to E-field, and brightness according
				// to total energy density:
				red = Math.sqrt(red / norm) * value;
				green = Math.sqrt(green / norm) * value;
				blue = Math.sqrt(blue / norm) * value;

				scaleProperties.putValue(EfieldSquared + BfieldSquared);

				gl2.glColor3d( red, green, blue );
				gl2.glVertex2f( xstart2, ystart2 );
				gl2.glVertex2f( xstart3, ystart2 );
			}
			gl2.glEnd();
		}
		scaleProperties.calculateAutomaticScale(1.0);
	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Energy density 2D (OpenGL) panel");
		scaleProperties.addComponents(box);
	}

	public ScaleProperties getScaleProperties() {
		return scaleProperties;
	}
}