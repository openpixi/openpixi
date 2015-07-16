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

import org.openpixi.pixi.diagnostics.methods.OccupationNumbersInTime;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.BooleanProperties;
import org.openpixi.pixi.ui.panel.properties.IntegerProperties;
import org.openpixi.pixi.ui.panel.properties.ScaleProperties;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.*;


/**
 * Displays 2D occupation numbers.
 */
public class OccupationNumbers2DGLPanel extends AnimationGLPanel {

	ScaleProperties scaleProperties = new ScaleProperties();
	BooleanProperties colorfulProperties = new BooleanProperties("Colorful occupation numbers", true);
	IntegerProperties frameSkipProperties = new IntegerProperties("Skipped frames:", 2);

	OccupationNumbersInTime diagnostic;
	Simulation simulation;

	private int frameCounter;
	private int frameSkip;

	/** Constructor */
	public OccupationNumbers2DGLPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
		scaleProperties.setAutomaticScaling(true);
		frameCounter = 0;

		simulation = this.simulationAnimation.getSimulation();
		diagnostic = new OccupationNumbersInTime(1.0, "none", "", true);
		diagnostic.initialize(simulation);

	}

	@Override
	public void display(GLAutoDrawable glautodrawable) {
		frameSkip = (frameSkipProperties.getValue() > 1) ? frameSkipProperties.getValue() : 1;
		if( frameCounter % frameSkip == 0)
		{
			if(simulation != simulationAnimation.getSimulation()) {
				simulation = this.simulationAnimation.getSimulation();
				diagnostic.initialize(simulation);
			}

			GL2 gl2 = glautodrawable.getGL().getGL2();
			int width = glautodrawable.getWidth();
			int height = glautodrawable.getHeight();
			gl2.glClear( GL.GL_COLOR_BUFFER_BIT );
			gl2.glLoadIdentity();

			double scale = scaleProperties.getScale();
			scaleProperties.resetAutomaticScale();
			Simulation s = getSimulationAnimation().getSimulation();

			/** Scaling factor for the displayed panel in x-direction*/
			double sx = width / s.getSimulationBoxSize(0);
			/** Scaling factor for the displayed panel in y-direction*/
			double sy = height / s.getSimulationBoxSize(1);

			int[] pos = new int[s.getNumberOfDimensions()];
			for(int w = 2; w < s.getNumberOfDimensions(); w++) {
				pos[w] = s.grid.getNumCells(w)/2;
			}

			diagnostic.calculate(simulation.grid, simulation.particles, 0);

			for(int i = 0; i < s.grid.getNumCells(0); i++) {
				gl2.glBegin( GL2.GL_QUAD_STRIP );
				for(int k = 0; k < s.grid.getNumCells(1); k++)
				{
					int xstart2 = (int)(s.grid.getLatticeSpacing() * i * sx);
					int xstart3 = (int)(s.grid.getLatticeSpacing() * (i + 1) * sx);
					int ystart2 = (int) (s.grid.getLatticeSpacing() * k * sy);

					pos[0] = i;
					pos[1] = k;
					int index = this.getMomentumIndex(pos);

					if(colorfulProperties.getValue())
					{
						double red = diagnostic.occupationNumbers[index][0];
						double green = diagnostic.occupationNumbers[index][1];
						double blue = diagnostic.occupationNumbers[index][2];
						double norm = red + green + blue;
						double value = Math.min(1.0, scale * norm);

						red = Math.sqrt(red / norm) * value;
						green = Math.sqrt(green / norm) * value;
						blue = Math.sqrt(blue / norm) * value;

						scaleProperties.putValue(norm);

						gl2.glColor3d( red, green, blue );
						gl2.glVertex2f( xstart2, ystart2 );
						gl2.glVertex2f( xstart3, ystart2 );
					} else {
						double occ = diagnostic.occupationNumbers[index][0]
								+ diagnostic.occupationNumbers[index][1]
								+ diagnostic.occupationNumbers[index][2];
						double value = Math.min(1.0, scale * occ);

						scaleProperties.putValue(occ);

						gl2.glColor3d( value, value, value );
						gl2.glVertex2f( xstart2, ystart2 );
						gl2.glVertex2f( xstart3, ystart2 );
					}

				}
				gl2.glEnd();
			}
			scaleProperties.calculateAutomaticScale(1.0);
		}
		frameCounter++;
	}

	private int getMomentumIndex(int[] pos)
	{
		int[] numGridCells = simulation.grid.getNumCells();

		for(int i = 0; i < pos.length; i++)
		{
			pos[i] += numGridCells[i] / 2;
			pos[i] %= numGridCells[i];
			pos[i] = numGridCells[i] - pos[i];
		}

		return simulation.grid.getCellIndex(pos);
	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Occupation numbers 2D (OpenGL) panel");
		scaleProperties.addComponents(box);
		colorfulProperties.addComponents(box);
		frameSkipProperties.addComponents(box);
	}

	public ScaleProperties getScaleProperties() {
		return scaleProperties;
	}

	public BooleanProperties getColorfulPropteries() {
		return colorfulProperties;
	}
	public IntegerProperties getFrameSkipProperties() {
		return frameSkipProperties;
	}
}