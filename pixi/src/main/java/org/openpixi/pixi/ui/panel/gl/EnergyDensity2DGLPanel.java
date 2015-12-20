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

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.Box;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.ComboBoxProperties;
import org.openpixi.pixi.ui.panel.properties.CoordinateProperties;
import org.openpixi.pixi.ui.panel.properties.ScaleProperties;


/**
 * Displays 2D energy density.
 */
public class EnergyDensity2DGLPanel extends AnimationGLPanel {

	public final int INDEX_ENERGY_DENSITY = 0;
	public final int INDEX_ENERGY_DENSITY_CHANGE = 1;

	String[] typeLabel = new String[] {
			"Energy density",
			"dE/dt"
	};

	public final int RED = 0;
	public final int GREEN = 1;
	public final int BLUE = 2;

	public ComboBoxProperties typeProperties;
	public ScaleProperties scaleProperties;
	public CoordinateProperties showCoordinateProperties;

	/** Constructor */
	public EnergyDensity2DGLPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
		typeProperties = new ComboBoxProperties(simulationAnimation, "Type", typeLabel, 0);
		scaleProperties = new ScaleProperties(simulationAnimation);
		scaleProperties.setAutomaticScaling(true);
		showCoordinateProperties = new CoordinateProperties(simulationAnimation, CoordinateProperties.Mode.MODE_2D);
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

		int xAxisIndex = showCoordinateProperties.getXAxisIndex();
		int yAxisIndex = showCoordinateProperties.getYAxisIndex();
		int pos[] = showCoordinateProperties.getPositions();

		/** Scaling factor for the displayed panel in x-direction*/
		double sx = width / s.getSimulationBoxSize(xAxisIndex);
		/** Scaling factor for the displayed panel in y-direction*/
		double sy = height / s.getSimulationBoxSize(yAxisIndex);

		double[] color = new double[3];

		int typeIndex = typeProperties.getIndex();

		for(int i = 0; i < s.grid.getNumCells(xAxisIndex); i++) {

			gl2.glBegin( GL2.GL_QUAD_STRIP );
			for(int k = 0; k < s.grid.getNumCells(yAxisIndex); k++)
			{
				//float xstart = (float) (s.grid.getLatticeSpacing() * (i + 0.5) * sx);
				float xstart2 = (float)(s.grid.getLatticeSpacing() * i * sx);
				float xstart3 = (float)(s.grid.getLatticeSpacing() * (i + 1) * sx);
				//float ystart = (float) (s.grid.getLatticeSpacing() * (k + 0.5) * sy);
				float ystart2 = (float) (s.grid.getLatticeSpacing() * k * sy);

				pos[xAxisIndex] = i;
				pos[yAxisIndex] = k;
				int index = s.grid.getCellIndex(pos);

				double value = 0.0;
				color[RED] = 0.0;
				color[GREEN] = 0.0;
				color[BLUE] = 0.0;
				if(s.grid.isEvaluatable(index)) {
					switch(typeIndex) {
					case INDEX_ENERGY_DENSITY:
						value = getEnergyDensity(s, index);
						break;
					case INDEX_ENERGY_DENSITY_CHANGE:
						value = 0;
						break;
					}
					getColorFromEField(s, index, color);
				}
				// Normalize
				double norm = Math.max(color[RED] + color[GREEN] + color[BLUE], 10E-20);
				double limitedValue = Math.min(1, scale * Math.abs(value));

				// Set color according to E-field, and brightness according
				// to total energy density:
				color[RED] = Math.sqrt(color[RED] / norm) * limitedValue;
				color[GREEN] = Math.sqrt(color[GREEN] / norm) * limitedValue;
				color[BLUE] = Math.sqrt(color[BLUE] / norm) * limitedValue;

				scaleProperties.putValue(value);

				gl2.glColor3d( color[RED], color[GREEN], color[BLUE] );
				gl2.glVertex2f( xstart2, ystart2 );
				gl2.glVertex2f( xstart3, ystart2 );
			}
			gl2.glEnd();
		}
		scaleProperties.calculateAutomaticScale(1.0);
	}

	private double getEnergyDensity(Simulation s, int index) {
		double value = 0.0;

		// Lattice spacing and coupling constant
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		for (int w = 0; w < s.getNumberOfDimensions(); w++) {
			value += s.grid.getEsquaredFromLinks(index, w);
			// Time averaging for B field.
			value += 0.5 * s.grid.getBsquaredFromLinks(index, w, 0);
			value += 0.5 * s.grid.getBsquaredFromLinks(index, w, 1);
		};
		return value / (as * g * as * g) / 2;
	}

	private void getColorFromEField(Simulation s, int index,
			double[] color) {
		int colors = s.grid.getNumberOfColors();
		for (int w = 0; w < s.getNumberOfDimensions(); w++) {
			// get color:
			double c;
			for (int n = 0; n < colors * colors - 1; n++) {
				c = s.grid.getE(index, w).get(n);
				// cycle through colors if there are more than three
				switch (n % 3) {
					case 0:
						color[RED] += c * c;
						break;
					case 1:
						color[GREEN] += c * c;
						break;
					case 2:
						color[BLUE] += c * c;
						break;
				}
			}
		}
	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Energy density 2D (OpenGL) panel");
		typeProperties.addComponents(box);
		scaleProperties.addComponents(box);
		showCoordinateProperties.addComponents(box);
	}
}