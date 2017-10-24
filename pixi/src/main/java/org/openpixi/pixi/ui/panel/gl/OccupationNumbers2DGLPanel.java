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
import org.openpixi.pixi.physics.util.GridFunctions;
import org.openpixi.pixi.ui.GridManager;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.*;


/**
 * Displays 2D occupation numbers.
 */
public class OccupationNumbers2DGLPanel extends AnimationGLPanel {

	public ScaleProperties scaleProperties;
	public BooleanProperties colorfulProperties;
	public BooleanProperties mirrorProperties;
	public IntegerProperties frameSkipProperties;
	public CoordinateProperties showCoordinateProperties;

	public BooleanProperties useConeProperties;
	public DoubleProperties collisionTimeDoubleProperties;
	public CoordinateProperties collisionCoordinateProperties;
	public CoordinateProperties velocityCoordinateProperties;
	public BooleanProperties useGaussianWindowProperties;
	public BooleanProperties useTukeyWindowProperties;
	public DoubleProperties tukeyWidthProperties;

	OccupationNumbersInTime diagnostic;
	Simulation simulation;
	GridManager gridManager;
	GridManager.LabeledGrid mirrorLabeledGrid;
	GridManager.LabeledGrid gaugeMirrorLabeledGrid;
	GridManager.LabeledGrid gaugeLabeledGrid;
	GridManager.LabeledGrid finalLabeledGrid;

	private int frameCounter;
	private int frameSkip;
	private int mirrorDirection = 0;
	private boolean oldUseMirror;
	private boolean oldUseCone;
	private double oldCollisionTime;
	private double[] oldCollisionPosition;
	private double[] oldConeVelocity;
	private boolean oldUseGaussianWindow;
	private boolean oldUseTukeyWindow;
	private double oldTukeyWidth;

	/** Constructor */
	public OccupationNumbers2DGLPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
		scaleProperties = new ScaleProperties(simulationAnimation);
		scaleProperties.setAutomaticScaling(true);
		colorfulProperties = new BooleanProperties(simulationAnimation, "Colorful occupation numbers", true);
		mirrorProperties = new BooleanProperties(simulationAnimation, "Mirror x-direction", true);
		oldUseMirror = mirrorProperties.getValue();
		frameSkipProperties = new IntegerProperties(simulationAnimation, "Skipped frames:", 2);
		showCoordinateProperties = new CoordinateProperties(simulationAnimation, CoordinateProperties.Mode.MODE_2D);
		useConeProperties = new BooleanProperties(simulationAnimation, "Use cone restriction:", false);
		collisionTimeDoubleProperties = new DoubleProperties(simulationAnimation, "Collision time:", 0.);
		collisionCoordinateProperties = new CoordinateProperties(simulationAnimation, "Collision center:", "0, 0, 0");
		velocityCoordinateProperties = new CoordinateProperties(simulationAnimation, "Cut cone velocity:", "0., 0., 0.");
		useGaussianWindowProperties = new BooleanProperties(simulationAnimation, "Gaussian window", false);
		useTukeyWindowProperties = new BooleanProperties(simulationAnimation, "Tukey window", false);
		tukeyWidthProperties = new DoubleProperties(simulationAnimation, "Tukey width", 0.);
		frameCounter = 0;

		simulation = this.simulationAnimation.getSimulation();
		updateDiagnostic();
		diagnostic.calculate(simulation.grid, simulation.particles, 0);

		gridManager = simulationAnimation.getMainControlApplet().getGridManager();
		mirrorLabeledGrid = gridManager.add("Occupation numbers (mirror)", simulation.grid);
		gaugeMirrorLabeledGrid = gridManager.add("Occupation numbers (gauge + mirror)", simulation.grid);
		gaugeLabeledGrid = gridManager.add("Occupation numbers (gauge)", simulation.grid);
		finalLabeledGrid = gridManager.add("Occupation numbers (final)", simulation.grid);
	}

	@Override
	public void display(GLAutoDrawable glautodrawable) {

		boolean useCone = useConeProperties.getValue();
		double collisionTime = collisionTimeDoubleProperties.getValue();
		double[] collisionPosition = collisionCoordinateProperties.getDoublePositions();
		double[] coneVelocity = velocityCoordinateProperties.getDoublePositions();
		boolean useGaussianWindow = useGaussianWindowProperties.getValue();
		boolean useTukeyWindow = useTukeyWindowProperties.getValue();
		double tukeyWidth = tukeyWidthProperties.getValue();

		// Compute occupation numbers
		if(mirrorProperties.getValue() != oldUseMirror || simulation != simulationAnimation.getSimulation()
				|| useCone != oldUseCone
				|| collisionTime != oldCollisionTime
				|| ! collisionPosition.equals(oldCollisionPosition)
				|| ! coneVelocity.equals(oldConeVelocity)
				|| useGaussianWindow != oldUseGaussianWindow
				|| useTukeyWindow != oldUseTukeyWindow
				|| tukeyWidth != oldTukeyWidth) {
			oldUseMirror = mirrorProperties.getValue();
			simulation = simulationAnimation.getSimulation();
			oldUseCone = useCone;
			oldCollisionTime = collisionTime;
			oldCollisionPosition = collisionPosition;
			oldConeVelocity = coneVelocity;
			oldUseGaussianWindow = useGaussianWindow;
			oldUseTukeyWindow = useTukeyWindow;
			oldTukeyWidth = tukeyWidth;
			updateDiagnostic();
		}
		frameSkip = (frameSkipProperties.getValue() > 1) ? frameSkipProperties.getValue() : 1;
		if( frameCounter % frameSkip == 0)
		{
			diagnostic.calculate(simulation.grid, simulation.particles, 0);

			mirrorLabeledGrid.grid = diagnostic.getMirrorGrid();
			gaugeMirrorLabeledGrid.grid = diagnostic.getGaugeMirrorGrid();
			gaugeLabeledGrid.grid = diagnostic.getGaugeGrid();
			finalLabeledGrid.grid = diagnostic.getFinalWindowGrid();
			finalLabeledGrid.occupationNumbers = diagnostic.occupationNumbers;
		}
		frameCounter++;

		// Display
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

		int xAxisNumCells = s.grid.getNumCells(xAxisIndex);
		int yAxisNumCells = s.grid.getNumCells(yAxisIndex);

		/** Scaling factor for the displayed panel in x-direction*/
		double sx = width / s.getSimulationBoxSize(xAxisIndex);
		/** Scaling factor for the displayed panel in y-direction*/
		double sy = height / s.getSimulationBoxSize(yAxisIndex);

		for(int i = 0; i < xAxisNumCells; i++) {
			gl2.glBegin( GL2.GL_QUAD_STRIP );
			for(int k = 0; k < yAxisNumCells; k++)
			{
				int xstart2 = (int)(s.grid.getLatticeSpacing() * i * sx);
				int xstart3 = (int)(s.grid.getLatticeSpacing() * (i + 1) * sx);
				int ystart2 = (int) (s.grid.getLatticeSpacing() * k * sy);
				int ystart3 = (int) (s.grid.getLatticeSpacing() * (k + 1) * sy);

				pos[xAxisIndex] = i;
				pos[yAxisIndex] = k;
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
					gl2.glVertex2f( xstart2, ystart3 );
					gl2.glVertex2f( xstart3, ystart3 );
				} else {
					double occ = diagnostic.occupationNumbers[index][0]
							+ diagnostic.occupationNumbers[index][1]
							+ diagnostic.occupationNumbers[index][2];
					double value = Math.min(1.0, scale * occ);

					scaleProperties.putValue(occ);

					gl2.glColor3d( value, value, value );
					gl2.glVertex2f( xstart2, ystart2 );
					gl2.glVertex2f( xstart3, ystart2 );
					gl2.glVertex2f( xstart2, ystart3 );
					gl2.glVertex2f( xstart3, ystart3 );
				}

			}
			gl2.glEnd();
		}

		scaleProperties.calculateAutomaticScale(1.0);
	}

	private void updateDiagnostic() {
		boolean useCone = useConeProperties.getValue();
		double collisionTime = collisionTimeDoubleProperties.getValue();
		double[] collisionPosition = collisionCoordinateProperties.getDoublePositions();
		double[] coneVelocity = velocityCoordinateProperties.getDoublePositions();
		boolean useGaussianWindow = useGaussianWindowProperties.getValue();
		boolean useTukeyWindow = useTukeyWindowProperties.getValue();
		double tukeyWidth = tukeyWidthProperties.getValue();

		boolean useMirroredGrid = mirrorProperties.getValue();

		diagnostic = new OccupationNumbersInTime(1.0, "none", "", true,
				useMirroredGrid, mirrorDirection,
				useCone, collisionTime, collisionPosition, coneVelocity, useGaussianWindow, useTukeyWindow, tukeyWidth);
		diagnostic.initialize(simulation);
	}

	private int getMomentumIndex(int[] pos)
	{
		int[] numGridCells = simulation.grid.getNumCells().clone();
		int[] pos2 = new int[pos.length];
		System.arraycopy(pos, 0, pos2, 0, pos.length);

		for(int i = 0; i < pos.length; i++)
		{
			pos2[i] += numGridCells[i] / 2;
			pos2[i] %= numGridCells[i];
			pos2[i] = numGridCells[i] - pos2[i];
		}

		return GridFunctions.getCellIndex(pos2, numGridCells);
	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Occupation numbers 2D (OpenGL) panel");
		scaleProperties.addComponents(box);
		colorfulProperties.addComponents(box);
		frameSkipProperties.addComponents(box);
		showCoordinateProperties.addComponents(box);
		mirrorProperties.addComponents(box);
		useConeProperties.addComponents(box);
		collisionTimeDoubleProperties.addComponents(box);
		collisionCoordinateProperties.addComponents(box);
		velocityCoordinateProperties.addComponents(box);
		useGaussianWindowProperties.addComponents(box);
		useTukeyWindowProperties.addComponents(box);
		tukeyWidthProperties.addComponents(box);
	}

	@Override
	public void destruct() {
		gridManager.remove(gaugeLabeledGrid);
		gridManager.remove(mirrorLabeledGrid);
		gridManager.remove(gaugeMirrorLabeledGrid);
		gridManager.remove(finalLabeledGrid);
		super.destruct();
	}
}