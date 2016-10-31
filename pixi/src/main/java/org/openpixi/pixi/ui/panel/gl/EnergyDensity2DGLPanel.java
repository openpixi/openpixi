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

import org.openpixi.pixi.diagnostics.methods.PoyntingTheoremBuffer;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.ComboBoxProperties;
import org.openpixi.pixi.ui.panel.properties.CoordinateProperties;
import org.openpixi.pixi.ui.panel.properties.ScaleProperties;


/**
 * Displays 2D energy density.
 */
public class EnergyDensity2DGLPanel extends AnimationGLPanel {

	public static final int INDEX_ENERGY_DENSITY = 0;
	public static final int INDEX_ENERGY_DENSITY_DERIVATIVE = 1;
	public static final int INDEX_DIV_POYNTING = 2;
	public static final int INDEX_B_ROT_E_MINUS_E_ROT_B = 3;
	public static final int INDEX_ENERGY_DENSITY_DERIVATIVE_DIV_POYNTING = 4;
	public static final int INDEX_CURRENT_ELECTRIC_FIELD = 5;
	public static final int INDEX_ENERGY_DENSITY_DERIVATIVE_DIV_POYNTING_CURRENT = 6;
	public static final int INDEX_ENERGY_DENSITY_DERIVATIVE_B_ROT_E_MINUS_E_ROT_B_CURRENT = 7;

	String[] dataLabel = new String[] {
			"Energy density",
			"dE/dt",
			"div S",
			"B rot E - E rot B",
			"dE/dt + div S",
			"j*E",
			"dE/dt + div S + j*E",
			"dE/dt + (B rot E - E rot B) + j*E"
	};

	public static final int RED = 0;
	public static final int GREEN = 1;
	public static final int BLUE = 2;

	public ComboBoxProperties dataProperties;
	public ScaleProperties scaleProperties;
	public CoordinateProperties showCoordinateProperties;

	private PoyntingTheoremBuffer poyntingTheorem;

	/** Constructor */
	public EnergyDensity2DGLPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
		dataProperties = new ComboBoxProperties(simulationAnimation, "Data", dataLabel, 0);
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
		poyntingTheorem = PoyntingTheoremBuffer.getOrAppendInstance(s);

		int xAxisIndex = showCoordinateProperties.getXAxisIndex();
		int yAxisIndex = showCoordinateProperties.getYAxisIndex();
		int pos[] = showCoordinateProperties.getPositions();

		/** Scaling factor for the displayed panel in x-direction*/
		double sx = width / s.getSimulationBoxSize(xAxisIndex);
		/** Scaling factor for the displayed panel in y-direction*/
		double sy = height / s.getSimulationBoxSize(yAxisIndex);

		double[] color = new double[3];

		int dataIndex = dataProperties.getIndex();

		for(int i = 0; i < s.grid.getNumCells(xAxisIndex); i++) {

			gl2.glBegin( GL2.GL_QUAD_STRIP );
			for(int k = 0; k < s.grid.getNumCells(yAxisIndex); k++)
			{
				//float xstart = (float) (s.grid.getLatticeSpacing() * (i + 0.5) * sx);
				float xstart2 = (float)(s.grid.getLatticeSpacing(xAxisIndex) * i * sx);
				float xstart3 = (float)(s.grid.getLatticeSpacing(xAxisIndex) * (i + 1) * sx);
				//float ystart = (float) (s.grid.getLatticeSpacing() * (k + 0.5) * sy);
				float ystart2 = (float) (s.grid.getLatticeSpacing(yAxisIndex) * k * sy);

				pos[xAxisIndex] = i;
				pos[yAxisIndex] = k;
				int index = s.grid.getCellIndex(pos);

				double value = 0;
				color[RED] = 0;
				color[GREEN] = 0;
				color[BLUE] = 0;
				if(s.grid.isEvaluatable(index)) {
					switch(dataIndex) {
					case INDEX_ENERGY_DENSITY:
						value = poyntingTheorem.getEnergyDensity(index);
						break;
					case INDEX_ENERGY_DENSITY_DERIVATIVE:
						value = poyntingTheorem.getEnergyDensityDerivative(index);
						break;
					case INDEX_DIV_POYNTING:
						value = poyntingTheorem.getDivPoyntingVector(index);
						break;
					case INDEX_B_ROT_E_MINUS_E_ROT_B:
						value = poyntingTheorem.getBrotEminusErotB(index);
						break;
					case INDEX_ENERGY_DENSITY_DERIVATIVE_DIV_POYNTING:
						value = poyntingTheorem.getEnergyDensityDerivative(index)
							+ poyntingTheorem.getDivPoyntingVector(index);
						break;
					case INDEX_CURRENT_ELECTRIC_FIELD:
						value = poyntingTheorem.getCurrentElectricField(index);
						break;
					case INDEX_ENERGY_DENSITY_DERIVATIVE_DIV_POYNTING_CURRENT:
						value = poyntingTheorem.getEnergyDensityDerivative(index)
							+ poyntingTheorem.getDivPoyntingVector(index)
							+ poyntingTheorem.getCurrentElectricField(index);
						break;
					case INDEX_ENERGY_DENSITY_DERIVATIVE_B_ROT_E_MINUS_E_ROT_B_CURRENT:
						value = poyntingTheorem.getEnergyDensityDerivative(index)
							+ poyntingTheorem.getBrotEminusErotB(index)
							+ poyntingTheorem.getCurrentElectricField(index);
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
		dataProperties.addComponents(box);
		scaleProperties.addComponents(box);
		showCoordinateProperties.addComponents(box);
	}
}