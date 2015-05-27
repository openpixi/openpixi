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
import javax.swing.JButton;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.util.FrameRateDetector;


/**
 * Displays 2D particles.
 */
public class Particle2DPanel extends AnimationPanel {

	private boolean drawCurrentGrid = false;

	private boolean drawFields = false;

	public boolean showinfo = false;

	/** A state for the trace */
	public boolean paint_trace = false;

	private boolean reset_trace;

	Color darkGreen = new Color(0x00, 0x80, 0x00);

	/** Constructor */
	public Particle2DPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
	}

	public void clear() {
		reset_trace = true;
	}

	public void checkTrace() {
		paint_trace =! paint_trace;
		//startAnimation();
	}

	public void drawCurrentGrid() {
		drawCurrentGrid =! drawCurrentGrid;
	}

	public void drawFields() {
		drawFields =! drawFields;
	}

	/** Display the particles */
	public void paintComponent(Graphics graph1) {
		Graphics2D graph = (Graphics2D) graph1;
		setBackground(Color.white);
		graph.translate(0, this.getHeight());
		graph.scale(1, -1);
		double scale = 10;

		if(!paint_trace)
		{
			super.paintComponent(graph1);
		}
		if(reset_trace)
		{
			super.paintComponent(graph1);
			reset_trace = false;
		}

		Simulation s = getSimulationAnimation().getSimulation();

		/** Scaling factor for the displayed panel in x-direction*/
		double sx = getWidth() / s.getWidth();
		/** Scaling factor for the displayed panel in y-direction*/
		double sy = getHeight() / s.getHeight();

		for (int i = 0; i < s.particles.size(); i++) {
			IParticle par = (IParticle) s.particles.get(i);
			graph.setColor(par.getColor());
			double radius = par.getRadius();//double radius = par.getRadius()*(2 - 1.9*par.getZ()/s.getDepth());
			int width = (int) (2*sx*radius);
			int height = (int) (2*sy*radius);
			if(width > 2 && height > 2 && !paint_trace) {
				graph.fillOval((int) (par.getPosition(0)*sx) - width/2, (int) (par.getPosition(1)*sy) - height/2,  width,  height);
			}
			else {
				graph.drawRect((int) (par.getPosition(0)*sx), (int) (par.getPosition(1)*sy), 0, 0);
			}
		}
		
		int[] pos = new int[s.getNumberOfDimensions()];
		for(int w = 2; w < s.getNumberOfDimensions(); w++) {
			pos[w] = s.grid.getNumCells(w)/2;
		}
		
		int colorIndex = getSimulationAnimation().getColorIndex();
		int dirIndex = getSimulationAnimation().getDirectionIndex();
		
		if(drawCurrentGrid)
		{
			graph.setColor(Color.black);
			
			
			
			for(int i = 0; i < s.grid.getNumCells(0); i++)
				for(int k = 0; k < s.grid.getNumCells(1); k++)
				{
					int xstart = (int) (s.grid.getLatticeSpacing() * (i + 0.5) * sx);
					int xstart2 = (int)(s.grid.getLatticeSpacing() * i * sx);
					int ystart = (int) (s.grid.getLatticeSpacing() * (k + 0.5) * sy);
					int ystart2 = (int) (s.grid.getLatticeSpacing() * k * sy);
					
					pos[0] = i;
					pos[1] = k;
					//drawArrow(graph, xstart, ystart, (int) Math.round(s.grid.getJx(i,k)*sx + xstart), (int) Math.round(s.grid.getJy(i,k)*sy + ystart));
                                        drawArrow(graph, xstart, ystart2, (int) Math.round(s.grid.getJ(pos, 0).get(colorIndex)*sx+xstart), ystart2, Color.BLACK);
                                        drawArrow(graph, xstart2, ystart, xstart2, (int) Math.round(s.grid.getJ(pos, 1).get(colorIndex)*sy+ystart),Color.BLACK);
				}
			//return;
		}

		if(drawFields)
		{
			graph.setColor(Color.black);
			for(int i = 0; i < s.grid.getNumCells(0); i++)
				for(int k = 0; k < s.grid.getNumCells(1); k++)
				{
					int xstart = (int) (s.grid.getLatticeSpacing() * (i + 0.5) * sx);
                    int xstart2 = (int)(s.grid.getLatticeSpacing() * i * sx);
                    int ystart = (int) (s.grid.getLatticeSpacing() * (k + 0.5) * sy);
                    int ystart2 = (int) (s.grid.getLatticeSpacing() * k * sy);
                    
                    pos[0] = i;
					pos[1] = k;
					
//drawArrow(graph, xstart, ystart, (int) Math.round(scale * s.grid.getEx(i,k)*sx + xstart), (int) Math.round(scale* s.grid.getEy(i,k)*sy + ystart));
                    drawArrow(graph, xstart, ystart2, (int) Math.round(scale*s.grid.getE(pos, 0).get(colorIndex)*sx+xstart),ystart2, Color.BLACK);
                    drawArrow(graph, xstart2, ystart, xstart2, (int) Math.round(scale*s.grid.getE(pos, 1).get(colorIndex)*sy+ystart), Color.GREEN);
                    //drawArrow(graph, xstart, ystart, xstart, (int) Math.round(scale*s.grid.getBz(i,k)*sy+ystart), Color.RED);
				}
			//return;
		}

		FrameRateDetector frameratedetector = getSimulationAnimation().getFrameRateDetector();

		if (showinfo) {
			graph.translate(0.0, this.getHeight());
			graph.scale(1.0, -1.0);
			graph.setColor(darkGreen);
			graph.drawString("Frame rate: " + frameratedetector.getRateString() + " fps", 30, 30);
			graph.drawString("Time step: " + (float) s.tstep, 30, 50);
			graph.drawString("Total time: " + (float) s.tottime, 30, 70);

			Runtime runtime = Runtime.getRuntime();
			long maxMemory = runtime.maxMemory();
			long allocatedMemory = runtime.totalMemory();
			long freeMemory = runtime.freeMemory();

			int bottom = getHeight();
			graph.drawString("free memory: " + freeMemory / 1024, 30, bottom - 90);
			graph.drawString("allocated memory: " + allocatedMemory / 1024, 30, bottom - 70);
			graph.drawString("max memory: " + maxMemory /1024, 30, bottom - 50);
			graph.drawString("total free memory: " +
				(freeMemory + (maxMemory - allocatedMemory)) / 1024, 30, bottom - 30);
		}
	}


	private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2, Color col) {

		int ARR_SIZE = 5;

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        // get the old transform matrix
        AffineTransform old = g.getTransform();
        AffineTransform at = getTranslateInstance(x1, y1);
        at.concatenate(getRotateInstance(angle));
        g.transform(at);
        //g.setTransform(at);

        // Draw horizontal arrow starting in (0, 0)
        Color colold = g.getColor();
        g.setColor(col);
        g.drawLine(0, 0, (int) len, 0);
        if(Math.abs(x2 - x1) > 0 || Math.abs(y2 - y1) > 0)
        	g.fillPolygon(new int[] {len, len-ARR_SIZE, len-ARR_SIZE, len},
        				  new int[] {0, -ARR_SIZE, ARR_SIZE, 0}, 4);
        g.setColor(colold);

        // reset transformationmatrix
        g.setTransform(old);
     }

	public void addComponents(Box panel) {
		JButton openButton = new JButton("Open...");
		panel.add(openButton);
	}
}
