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
package org.openpixi.pixi.ui.panel;

import static java.awt.geom.AffineTransform.getRotateInstance;
import static java.awt.geom.AffineTransform.getTranslateInstance;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.Box;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.ScaleProperties;


/**
 * Displays 2D energy density.
 */
public class EnergyDensity2DPanel extends AnimationPanel {

	public ScaleProperties scaleProperties;
	
	/** Constructor */
	public EnergyDensity2DPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
		scaleProperties = new ScaleProperties(simulationAnimation);
	}

	/** Display the particles */
	public void paintComponent(Graphics graph1) {
		Graphics2D graph = (Graphics2D) graph1;
		setBackground(Color.white);
		graph.translate(0, this.getHeight());
		graph.scale(1, -1);

		super.paintComponent(graph1);
		
		double scale = scaleProperties.getScale();
		scaleProperties.resetAutomaticScale();

		Simulation s = getSimulationAnimation().getSimulation();

		/** Scaling factor for the displayed panel in x-direction*/
		double sx = getWidth() / s.getWidth();
		/** Scaling factor for the displayed panel in y-direction*/
		double sy = getHeight() / s.getHeight();
		
		// Lattice spacing and coupling constant
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();
		
		int[] pos = new int[s.getNumberOfDimensions()];
		for(int w = 2; w < s.getNumberOfDimensions(); w++) {
			pos[w] = s.grid.getNumCells(w)/2;
		}

		graph.setColor(Color.black);
		for (int i = 0; i < s.grid.getNumCells(0); i++) {
			for (int k = 0; k < s.grid.getNumCells(1); k++) {
				int xstart = (int) (s.grid.getLatticeSpacing() * (i + 0.5) * sx);
				int xstart2 = (int) (s.grid.getLatticeSpacing() * i * sx);
				int ystart = (int) (s.grid.getLatticeSpacing() * (k + 0.5) * sy);
				int ystart2 = (int) (s.grid.getLatticeSpacing() * k * sy);

				pos[0] = i;
				pos[1] = k;
				int index = s.grid.getCellIndex(pos);

				double EfieldSquared = 0.0;
				double BfieldSquared = 0.0;

				if(s.grid.isEvaluatable(index)) {
					for (int w = 0; w < s.getNumberOfDimensions(); w++) {
						EfieldSquared += s.grid.getEsquaredFromLinks(index, w)
								/ (as * g * as * g) / 2;
						// Time averaging for B field.
						BfieldSquared += s.grid.getBsquaredFromLinks(index, w, 0)
								/ (as * g * as * g) / 4.0;
						BfieldSquared += s.grid.getBsquaredFromLinks(index, w, 1)
								/ (as * g * as * g) / 4.0;
					}
				}
				scaleProperties.putValue(EfieldSquared + BfieldSquared);

				drawArrow(graph, xstart2, ystart2,
						(int) Math.round(scale * EfieldSquared * sx + xstart2),
						ystart2, Color.BLACK);
				drawArrow(graph, xstart2, ystart2, xstart2,
						(int) Math.round(scale * BfieldSquared * sy + ystart2),
						Color.GREEN);
				drawArrow(
						graph,
						xstart2,
						ystart2,
						xstart2,
						(int) Math.round(scale
								* (EfieldSquared + BfieldSquared) * sy
								+ ystart2), Color.RED);
			}
		}
		scaleProperties.calculateAutomaticScale(1.0);
	}

	private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2,
			Color col) {

		int ARR_SIZE = 5;

		double dx = x2 - x1, dy = y2 - y1;
		double angle = Math.atan2(dy, dx);
		int len = (int) Math.sqrt(dx * dx + dy * dy);
		// get the old transform matrix
		AffineTransform old = g.getTransform();
		AffineTransform at = getTranslateInstance(x1, y1);
		at.concatenate(getRotateInstance(angle));
		g.transform(at);
		// g.setTransform(at);

		// Draw horizontal arrow starting in (0, 0)
		Color colold = g.getColor();
		g.setColor(col);
		g.drawLine(0, 0, (int) len, 0);
		if (Math.abs(x2 - x1) > 0 || Math.abs(y2 - y1) > 0)
			g.fillPolygon(
					new int[] { len, len - ARR_SIZE, len - ARR_SIZE, len },
					new int[] { 0, -ARR_SIZE, ARR_SIZE, 0 }, 4);
		g.setColor(colold);

		// reset transformationmatrix
		g.setTransform(old);
	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Energy density 2D panel");
		scaleProperties.addComponents(box);
	}
}