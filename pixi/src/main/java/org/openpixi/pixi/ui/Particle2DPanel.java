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
package org.openpixi.pixi.ui;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.boundary.*;
import org.openpixi.pixi.physics.collision.*;
import org.openpixi.pixi.physics.solver.*;
import org.openpixi.pixi.ui.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import static java.awt.geom.AffineTransform.*;
import java.util.ArrayList;
import java.lang.Math;


/**
 * Displays 2D particles.
 */
public class Particle2DPanel extends JPanel {
	
	private static final int xmax = 700;
	private static final int ymax = 500;
	
	public String fileName;
	
	private WriteFile file = new WriteFile();
	
	public double step;

	private boolean reset_trace;
	
	private boolean test = false;
	
	private boolean drawCurrentGrid = false;
	
	private boolean writePosition = false;

	private Solver s = new EulerRichardson();
	
	//private Collision collision = new ElasticCollision();
	
	/** Milliseconds between updates */
	private int interval = 30;

	/** Timer for animation */
	public Timer timer;
	
	public boolean showinfo = false;
	private FrameRateDetector frameratedetector;

	/** A state for the trace */
	public boolean paint_trace = false;

	private static int NUM_PARTICLES = 10;

	/** Constant force for particles */
	public Force f = new Force();
	
	private Boundary boundary = new HardWallBoundary();

	/** Contains all particles */
	ArrayList<Particle2D> parlist = new ArrayList<Particle2D>();
	
	public CurrentGrid currentGrid = new CurrentGrid();

	Color darkGreen = new Color(0x00, 0x80, 0x00);

	/** Listener for timer */
	public class TimerListener implements ActionListener {

		public void actionPerformed(ActionEvent eve) {
			
			boundary.setBoundaries(0, 0, getWidth(), getHeight());
			for (int i = 0; i < NUM_PARTICLES; i++) {
				Particle2D par = (Particle2D) parlist.get(i);
				if(test && i == 0)
					for(int k = 0; k < 100; k++)
					{
						s.step(par, f, step / 100);
						boundary.check(par, f, s, step / 100);
					}
				else {
					s.step(par, f, step);				
					boundary.check(par, f, s, step);
				}
			}
			//collision.check(parlist, f, s, step);
			frameratedetector.update();
			repaint();
			if(writePosition)
			{
				Particle2D par = (Particle2D) parlist.get(0);
				System.out.println(par.x + " " + par.y);
				file.writeFile(fileName, par.x + " " + par.y);
			}
		}
	}

	/** Constructor */
	public Particle2DPanel() {
		timer = new Timer(interval, new TimerListener());

		// Set properties of the panel
		this.setVisible(true);
		this.setSize(xmax, ymax);

		// Create all particles
		initRandomParticles(10, 8);
		
		frameratedetector = new FrameRateDetector(500);
	}

	public void startAnimation() {
		timer.start();
	}

	public void stopAnimation() {
		timer.stop();
		//test = false;
	}

	public void resetAnimation(int id) {
		// timer.restart();
		timer.stop();
		reset_trace = true;
		switch(id) {
		case 0:
			initRandomParticles(10, 8);
			break;
		case 1:
			initRandomParticles(100, 5);
			break;
		case 2:
			initRandomParticles(1000, 3);
			break;
		case 3:
			initRandomParticles(10000, 1);
			break;
		case 4:
			initGravity(1);
			break;
		case 5:
			initElectric(1);
			break;
		case 6:
			initMagnetic(3);
			break;
		case 7:
			initSpring(1);
			break;
		}
		timer.start();
	}
	
	public void testSolver()
	{
		test = true;
		createRandomParticles(2, 10);
		f.reset();
		for (int i = 0; i < NUM_PARTICLES; i++) {
			Particle2D par = (Particle2D) parlist.get(i);
			par.x = (100);
			par.y = (100 + 100 * i);
			par.vx = 10;
			par.vy = 0;
			par.mass = 1;
			if(i == 0)
				par.charge = 1;
			else
				par.charge = -1;
		}
		setPeriodicBoundary();
	}
	

	private void initRandomParticles(int count, int radius) {
		f = new Force();
		f.reset();
		f.gy = - 1; //-ConstantsSI.g;
		//f.bz = 1;
		
		createRandomParticles(count, radius);
		setHardWallBoundary();
	}

	private void initGravity(int count) {
		f = new Force();
		f.reset();
		f.gy = -1; // -ConstantsSI.g;
		
		createRandomParticles(count, 15);
		setHardWallBoundary();
	}
	
	private void initElectric(int count) {
		f = new Force();
		f.reset();
		f.ey = -1;
		
		createRandomParticles(count, 15);
		setHardWallBoundary();
	}

	private void initMagnetic(int count) {
		f = new Force();
		f.reset();
		f.bz = .1;
		
		createRandomParticles(count, 15);
		setPeriodicBoundary();
	}
	
	private void initSpring(int count) {
		NUM_PARTICLES = count;
		parlist.clear();
		f = new SpringForce();
		f.reset();
		
		for (int k = 0; k < NUM_PARTICLES; k++) {
			Particle2D par = new Particle2D();
			par.x = xmax * Math.random();
			par.y = ymax * Math.random();
			par.radius = 15;
			par.vx = 10 * Math.random();
			par.vy = 0;
			par.mass = 1;
			par.charge = 0;
			s.prepare(par, f, step);
			parlist.add(par);
		}

		setPeriodicBoundary();
	}
	
	private void createRandomParticles(int count, int radius) {
		NUM_PARTICLES = count;
		parlist.clear();
		for (int k = 0; k < NUM_PARTICLES; k++) {
			Particle2D par = new Particle2D();
			par.x = xmax * Math.random();
			par.y = ymax * Math.random();
			par.radius = radius;
			par.vx = 10 * Math.random();
			par.vy = 10 * Math.random();
			par.mass = 1;
			if (Math.random() > 0.5) {
				par.charge = 1;
			} else {
				par.charge = -1;
			}
			s.prepare(par, f, step);
			parlist.add(par);
		}
	}
	
	public void setHardWallBoundary() {
		boundary = new HardWallBoundary();
	}

	public void setPeriodicBoundary() {
		boundary = new PeriodicBoundary();
	}
	
	public void checkTrace() {
		paint_trace =! paint_trace;
		startAnimation();
	}
	
	public void drawCurrentGrid() {
		drawCurrentGrid =! drawCurrentGrid;
	}
	
	public void writePosition() {
		writePosition =! writePosition;
		if(writePosition)
		{
			createRandomParticles(1, 10);
			Particle2D par = (Particle2D) parlist.get(0);
			par.x = 0;
			par.y = this.getHeight() * 0.5;
			par.vx = 10;
			par.vy = 10;
			par.mass = 1;
			par.charge = 1;
		}
		else
			resetAnimation(0);
	}
	
	public void algorithmChange(int id)
	{
		for (int i = 0; i < NUM_PARTICLES; i++) {
			Particle2D par = (Particle2D) parlist.get(i);
			s.complete(par, f, step);				
		}
		
		switch(id) {
		case 0:
			s = new EulerRichardson();
			break;
		case 1:
			s = new LeapFrog();
			break;
		case 2:
			s = new LeapFrogDamped();
			break;
		case 3:
			s = new LeapFrogHalfStep();
			break;
		case 4:
			s = new Boris();
			break;
		case 5:
			s = new BorisDamped();
			break;
		case 6:
			s = new SemiImplicitEuler();
			break;
		case 7:
			s = new Euler();
			break;
			}
		
		for (int i = 0; i < NUM_PARTICLES; i++) {
			Particle2D par = (Particle2D) parlist.get(i);
			s.prepare(par, f, step);				
		}
	}

	/** Display the particles */
	public void paintComponent(Graphics graph1) {
		Graphics2D graph = (Graphics2D) graph1;
		setBackground(Color.white);
		graph.translate(0.0, this.getHeight());
		graph.scale(1.0, -1.0);
		currentGrid.setGrid(getWidth(), getHeight());

		if(!paint_trace)
		{
			super.paintComponent(graph1);
		}
		if(reset_trace)
		{
			super.paintComponent(graph1);
			reset_trace = false;
		}
		
		//if(!drawCurrentGrid) {
		for (int i = 0; i < NUM_PARTICLES; i++) {
			Particle2D par = (Particle2D) parlist.get(i);
			if (par.charge > 0) {
				graph.setColor(Color.blue);
			} else {
				graph.setColor(Color.red);
			}
			int resize = 2 * (int) par.radius ;
			if(paint_trace)
			{
				resize = resize / 5;
			}
			if(resize > 2)
			{
				graph.fillOval((int) par.x - resize /2, (int) par.y - resize / 2,  resize,  resize);
			}
			else {
				// drawRect(x,y,0,0) is about 20% faster than fillRect(x,y,1,1)
				//graph.fillRect((int) par.x, (int) par.y, 1, 1);
				graph.drawRect((int) par.x, (int) par.y, 0, 0);
			}
		}
		
		if(drawCurrentGrid)
		{
			graph.setColor(Color.black);
			currentGrid.updateGrid(parlist);
			for(int i = 0; i < currentGrid.X_BOX; i++)
				for(int k = 0; k < currentGrid.Y_BOX; k++)
				{
					int xstart = (int) (currentGrid.cellWidth * (i + 0.5));
					int ystart = (int) (currentGrid.cellHeight * (k + 0.5));
					drawArrow(graph, xstart, ystart, (int) Math.round(currentGrid.jx[i][k] + xstart), (int) Math.round(currentGrid.jy[i][k] + ystart));
				}
			//return;
		}

		if (showinfo) {
			graph.translate(0.0, this.getHeight());
			graph.scale(1.0, -1.0);
			graph.setColor(darkGreen);
			graph.drawString("Frame rate: " + frameratedetector.getRateString() + " fps", 30, 30);
			graph.drawString("Time step: " + (float) step, 30, 50);

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
	private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
		
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
        g.drawLine(0, 0, (int) len, 0);
        if(Math.abs(x2 - x1) > 0 || Math.abs(y2 - y1) > 0)
        	g.fillPolygon(new int[] {len, len-ARR_SIZE, len-ARR_SIZE, len},
        				  new int[] {0, -ARR_SIZE, ARR_SIZE, 0}, 4);
        
        // reset transformationmatrix
        g.setTransform(old);
     }

}
