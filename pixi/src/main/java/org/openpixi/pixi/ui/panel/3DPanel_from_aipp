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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.particles.Particle;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.util.FrameRateDetector;
import org.openpixi.pixi.ui.util.projection.LineObject;
import org.openpixi.pixi.ui.util.projection.Projection;
import org.openpixi.pixi.ui.util.projection.Scene;
import org.openpixi.pixi.ui.util.projection.SphereObject;


/**
 * Displays 2D particles.
 */
public class Particle3DPanel extends AnimationPanel {

	private boolean drawCurrentGrid = false;

	private boolean drawFields = false;

	/** Whether to combine x- and y-components of the fields into a single vector
	 * or whether to keep them separate. */
	private boolean combinefields = false;

	public boolean showinfo = false;

	/** A state for the trace */
	public boolean paint_trace = false;

	private boolean reset_trace;

	Color darkGreen = new Color(0x00, 0x80, 0x00);

	private Projection projection = new Projection();
	private LineObject object = new LineObject();
	private LineObject fields = new LineObject();
	private SphereObject spheres = new SphereObject();
	private Scene scene = new Scene();

	/** Constructor */
	public Particle3DPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);

		Simulation s = simulationAnimation.getSimulation();
		projection.deltaX = -s.getWidth()/2;
		projection.deltaY = -s.getHeight()/2;
		projection.deltaZ = -s.getDepth()/2;
		projection.screenDeltaX = s.getWidth()/2;
		projection.screenDeltaY = s.getHeight()/2;
		projection.distance = 2 * s.getWidth();
		projection.scale = .7;
		projection.phi = 0;
		projection.theta = 0;

		object.addCube(s.getWidth(), Color.black);

		scene.add(object);
		scene.add(fields);
		scene.add(spheres);

		MouseListener l = new MouseListener();
		addMouseListener(l);
		addMouseMotionListener(l);
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

		projection.updateRotationMatrix();

		spheres.clear();

		for (Particle p : s.particles) {
			double x = p.getX();
			double y = p.getY();
			double z = p.getZ();
			double r = p.getRadius();
			Color color = p.getColor();
			spheres.addSphere(x, y, z, r, color);
		}

		fields.clear();

		if(drawCurrentGrid) {
			for(int i = 0; i < s.grid.getNumCellsX(); i++) {
				for(int k = 0; k < s.grid.getNumCellsY(); k++) {
					for(int j = 0; j < s.grid.getNumCellsZ(); j++) {
						double xstart = s.grid.getCellWidth() * (i + 0.5);
						double ystart = s.grid.getCellHeight() * (k + 0.5);
						double zstart = s.grid.getCellDepth() * (j + 0.5);
						double jx = scale * s.grid.getJx(i,k,j);
						double jy = scale * s.grid.getJy(i,k,j);
						double jz = scale * s.grid.getJz(i,k,j);
						if (combinefields) {
							// Combine x- and y-components of current
							fields.addLineDelta(xstart, ystart, zstart,
									jx, jy, jz,
									Color.BLACK);
						} else {
							// Show x- and y-components of current separately
							double xstart2 = s.grid.getCellWidth() * i;
							double ystart2 = s.grid.getCellHeight() * k;
							fields.addLineDelta(xstart, ystart2, zstart,
									jx, 0, 0,
									Color.BLACK);
							fields.addLineDelta(xstart2, ystart, zstart,
									0, jy, 0,
									Color.BLACK);
							// TODO: Add z-component
						}
					}
				}
			}
		}

		if(drawFields)
		{
			graph.setColor(Color.black);
			for(int i = 0; i < s.grid.getNumCellsX(); i++) {
				for(int k = 0; k < s.grid.getNumCellsY(); k++) {
					for(int j = 0; j < s.grid.getNumCellsZ(); j++) {
						double xstart = s.grid.getCellWidth() * (i + 0.5);
						double ystart = s.grid.getCellHeight() * (k + 0.5);
						double zstart = s.grid.getCellDepth() * (j + 0.5);
						double ex = scale * s.grid.getEx(i,k,j);
						double ey = scale * s.grid.getEy(i,k,j);
						double ez = scale * s.grid.getEz(i,k,j);
						double bx = scale * s.grid.getBx(i,k,j);
						double by = scale * s.grid.getBy(i,k,j);
						double bz = scale * s.grid.getBz(i,k,j);
						if (combinefields) {
							// Draw combined E- and B-fields
							fields.addLineDelta(xstart, ystart, zstart,
									ex, ey, ez,
									Color.green);
							fields.addLineDelta(xstart, ystart, zstart,
									bx, by, bz,
									Color.red);
						} else {
							// Draw x- and y-components of E- and B-fields separately
							double xstart2 = s.grid.getCellWidth() * i;
							double ystart2 = s.grid.getCellHeight() * k;
							fields.addLineDelta(xstart, ystart2, zstart,
									ex, 0, 0,
									Color.green);
							fields.addLineDelta(xstart2, ystart, zstart,
									0, ey, 0,
									Color.green);
							fields.addLineDelta(xstart, ystart, zstart,
									0, 0, bz,
									Color.red);
						}
					}
				}
			}
		}

		scene.paint(projection, graph, sx, sy);

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
			projection.phi -= factor * deltaX;
			projection.theta -= factor * deltaY;
			mouseOldX = e.getX();
			mouseOldY = e.getY();
			super.mouseDragged(e);
		}

		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
		}
	}
}