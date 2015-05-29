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

import javax.swing.Box;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.particles.IParticle;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.ColorProperties;
import org.openpixi.pixi.ui.panel.properties.FieldProperties;
import org.openpixi.pixi.ui.panel.properties.InfoProperties;
import org.openpixi.pixi.ui.util.projection.LineObject;
import org.openpixi.pixi.ui.util.projection.Projection;
import org.openpixi.pixi.ui.util.projection.Scene;
import org.openpixi.pixi.ui.util.projection.SphereObject;


/**
 * Displays 2D particles.
 */
public class Particle3DPanel extends AnimationPanel {

	ColorProperties colorProperties = new ColorProperties();
	FieldProperties fieldProperties = new FieldProperties();
	InfoProperties infoProperties = new InfoProperties();

	/** Whether to combine the spatial components of the fields into a single vector
	 * or whether to keep them separate. */
	private boolean combinefields = true;

	/** A state for the trace */
	public boolean paint_trace = false;

	private boolean reset_trace;

	private int gridstep = 1;
	private int gridstepadjusted = 0;
	private long currentrendertime = 0;

	private Projection projection = new Projection();
	private LineObject cuboid = new LineObject();
	private LineObject fields = new LineObject();
	private SphereObject spheres = new SphereObject();
	private Scene scene = new Scene();

	/** Constructor */
	public Particle3DPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);

		Simulation s = simulationAnimation.getSimulation();
		projection.phi = 0;
		projection.theta = 0;

		scene.add(cuboid);
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
		double screenscale = sx;
		if (sy < screenscale) {
			screenscale = sy;
		}
		double maxsize = s.getWidth();
		if (s.getHeight() > maxsize) {
			maxsize = s.getHeight();
		}
		if (s.getDepth() > maxsize) {
			maxsize = s.getDepth();
		}
		projection.screenDeltaX = getWidth()/2;
		projection.screenDeltaY = getHeight()/2;
		projection.distance = 2 * maxsize;
		projection.scale = .7;
		projection.screenZoom = screenscale;
		projection.deltaX = -s.getWidth()/2;
		projection.deltaY = -s.getHeight()/2;
		projection.deltaZ = -s.getDepth()/2;

		projection.updateRotationMatrix();

		cuboid.clear();
		cuboid.addCuboid(s.getWidth(), s.getHeight(), s.getDepth(), Color.black);

		spheres.clear();

		for (IParticle p : s.particles) {
			double x = p.getPosition(0);
			double y = p.getPosition(1);
			double z = p.getPosition(2);
			double r = p.getRadius();
			Color color = p.getColor();
			spheres.addSphere(x, y, z, r, color);
		}

		fields.clear();

		// Adaptive gridstep based on timeout in drawing routine
		if (scene.drawtimeout) {
			gridstep++;
			gridstepadjusted = 0;
		} else {
			if (gridstepadjusted == 1) {
				// First successful time measurement with new grid step
				currentrendertime = scene.lastrendertime;
			} else {
				// If suddenly rendering is more than twice as fast
				if (currentrendertime > 2 * scene.lastrendertime) {
					// adjust the gridstep again
					gridstep--;
					if (gridstep < 1) {
						gridstep = 1;
					}
				}
			}
			gridstepadjusted++;
		}

		int[] pos = new int[s.getNumberOfDimensions()];
		for(int w = 3; w < s.getNumberOfDimensions(); w++) {
			pos[w] = s.grid.getNumCells(w)/2;
		}
		
		int colorIndex = colorProperties.getColorIndex();
		int directionIndex = colorProperties.getDirectionIndex();
		
		if(fieldProperties.getDrawCurrentGrid()) {
			for(int i = 0; i < s.grid.getNumCells(0); i += gridstep) {
				for(int j = 0; j < s.grid.getNumCells(1); j += gridstep) {
					for(int k = 0; k < s.grid.getNumCells(2); k += gridstep) {
						double xstart = s.grid.getLatticeSpacing() * i;
						double ystart = s.grid.getLatticeSpacing() * j;
						double zstart = s.grid.getLatticeSpacing() * k;
						
						pos[0] = i;
						pos[1] = j;
						pos[2] = k;
						double jx = scale * s.grid.getJ(pos, 0).get(colorIndex);
						double jy = scale * s.grid.getJ(pos, 1).get(colorIndex);
						double jz = scale * s.grid.getJ(pos, 2).get(colorIndex);
						if (combinefields) {
							// Combine spatial components of current
							// Current vectors are placed on the grid points.
							fields.addLineDelta(xstart, ystart, zstart,
									jx, jy, jz,
									Color.BLACK);
						} else {
							// Show spatial components of current separately
							// Assume that field components are placed on the lattice links.
							double xstartCenter = s.grid.getLatticeSpacing() * (i + 0.5);
							double ystartCenter = s.grid.getLatticeSpacing() * (j + 0.5);
							double zstartCenter = s.grid.getLatticeSpacing() * (k + 0.5);
							fields.addLineDelta(xstartCenter, ystart, zstart,
									jx, 0, 0,
									Color.BLACK);
							fields.addLineDelta(xstart, ystartCenter, zstart,
									0, jy, 0,
									Color.BLACK);
							fields.addLineDelta(xstart, ystart, zstartCenter,
									0, 0, jz,
									Color.BLACK);
						}
					}
				}
			}
		}

		if(fieldProperties.getDrawFields())
		{
			graph.setColor(Color.black);
			for(int i = 0; i < s.grid.getNumCells(0); i += gridstep) {
				for(int j = 0; j < s.grid.getNumCells(1); j += gridstep) {
					for(int k = 0; k < s.grid.getNumCells(2); k += gridstep) {
						double xstart = s.grid.getLatticeSpacing() * i;
						double ystart = s.grid.getLatticeSpacing() * j;
						double zstart = s.grid.getLatticeSpacing() * k;
						
						pos[0] = i;
						pos[1] = j;
						pos[2] = k;

						double ex = scale * s.grid.getE(pos, 0).get(colorIndex);
						double ey = scale * s.grid.getE(pos, 1).get(colorIndex);
						double ez = scale * s.grid.getE(pos, 2).get(colorIndex);

						if (combinefields) {
							// Draw combined E- and B-fields
							// Electric fields are placed on the grid points.
							fields.addLineDelta(xstart, ystart, zstart,
									ex, ey, ez,
									Color.green);
							//TODO: Draw magnetic fields.
						} else {
							// Draw spatial components of E- and B-fields separately.
							// Assume that field components are placed on the lattice links.
							double xstartCenter = s.grid.getLatticeSpacing() * (i + 0.5);
							double ystartCenter = s.grid.getLatticeSpacing() * (j + 0.5);
							double zstartCenter = s.grid.getLatticeSpacing() * (k + 0.5);
							fields.addLineDelta(xstartCenter, ystart, zstart,
									ex, 0, 0,
									Color.green);
							fields.addLineDelta(xstart, ystartCenter, zstart,
									0, ey, 0,
									Color.green);
							fields.addLineDelta(xstart, ystart, zstartCenter,
									0, 0, ez,
									Color.green);
							//TODO: Draw magnetic fields.
						}
					}
				}
			}
		}

		scene.paint(projection, graph);

		infoProperties.showInfo(graph, this);
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

	public void addComponents(Box box) {
		addLabel(box, "Particle 3D panel");
		colorProperties.addComponents(box);
		fieldProperties.addComponents(box);
		infoProperties.addComponents(box);
	}

}