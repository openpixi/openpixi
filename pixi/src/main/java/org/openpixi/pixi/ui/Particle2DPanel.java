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
import org.openpixi.pixi.physics.boundary.PeriodicBoundary;
import org.openpixi.pixi.physics.collision.algorithms.*;
import org.openpixi.pixi.physics.collision.detectors.*;
import org.openpixi.pixi.physics.force.*;
import org.openpixi.pixi.physics.force.relativistic.*;
import org.openpixi.pixi.physics.solver.*;
import org.openpixi.pixi.physics.solver.relativistic.*;
import org.openpixi.pixi.ui.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import static java.awt.geom.AffineTransform.*;
import java.lang.Math;
import java.util.ArrayList;

import org.openpixi.pixi.physics.grid.*;


/**
 * Displays 2D particles.
 */
public class Particle2DPanel extends JPanel {
	
	public Simulation s;

	public String fileName;
	
	public String fileDirectory;
	
	private WriteFile file = new WriteFile();
	
	private boolean relativistic = false;

	private boolean reset_trace;
	
	private boolean test = false;
	
	private boolean drawCurrentGrid = false;
	
	private boolean drawFields = false;
	
	private boolean calculateFields = false;
	
	private boolean writePosition = false;

	/** Milliseconds between updates */
	private int interval = 30;

	/** Timer for animation */
	public Timer timer;
	
	public boolean showinfo = false;
	private FrameRateDetector frameratedetector;

	/** A state for the trace */
	public boolean paint_trace = false;

	Color darkGreen = new Color(0x00, 0x80, 0x00);

	/** Listener for timer */
	public class TimerListener implements ActionListener {

		public void actionPerformed(ActionEvent eve) {

			updateSimulationSize();
//			for (int i = 0; i < Simulation.particles.size(); i++) {
//				Particle2D par = (Particle2D) Simulation.particles.get(i);
//				if(test && i == 0)
//					for(int k = 0; k < 100; k++)
//					{
//						ParticleMover.solver.step(par, Simulation.f, Simulation.tstep / 100);
//						Simulation.boundary.check(par, Simulation.f, ParticleMover.solver, Simulation.tstep / 100);
//					}
//				else {
//					ParticleMover.solver.step(par, Simulation.f, Simulation.tstep);
//					Simulation.boundary.check(par, Simulation.f, ParticleMover.solver, Simulation.tstep);
//				}
//			}
			s.step();
			frameratedetector.update();
			repaint();
			if(writePosition)
			{
				Particle2D par = (Particle2D) s.particles.get(0);
				System.out.println(par.x + " " + par.y);
				file.writeFile(fileName, fileDirectory, par.x + " " + par.y);
			}
		}
	}

	/** Constructor */
	public Particle2DPanel() {
		timer = new Timer(interval, new TimerListener());
		s = new Simulation(700, 500, 10, 8);
		
		// Set properties of the panel
		this.setVisible(true);
		//this.setSize(700, 500);
		updateSimulationSize();

		s.psolver = new EulerRichardson();

		frameratedetector = new FrameRateDetector(500);
	}

	private void updateSimulationSize() {
		s.setSize(getWidth(), getHeight());
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
			InitialConditions.initRandomParticles(s, 10, 8);
			break;
		case 1:
			InitialConditions.initRandomParticles(s, 100, 5);
			break;
		case 2:
			InitialConditions.initRandomParticles(s, 1000, 3);
			break;
		case 3:
			InitialConditions.initRandomParticles(s, 10000, 1);
			break;
		case 4:
			InitialConditions.initGravity(s, 1);
			break;
		case 5:
			InitialConditions.initElectric(s, 1);
			break;
		case 6:
			InitialConditions.initMagnetic(s, 3);
			break;
		case 7:
			InitialConditions.initSpring(s, 1);
			break;
		}
		updateFieldForce();
		ParticleMover.prepareAllParticles(s);
		timer.start();
	}
	
	public void testSolver()
	{
		test = true;
		InitialConditions.createRandomParticles(s, 2, 10);
		s.f.clear();
		for (int i = 0; i < s.particles.size(); i++) {
			Particle2D par = (Particle2D) s.particles.get(i);
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
		InitialConditions.setPeriodicBoundary(s);
	}
	

	public void checkTrace() {
		paint_trace =! paint_trace;
		startAnimation();
	}
	
	public void drawCurrentGrid() {
		drawCurrentGrid =! drawCurrentGrid;
	}
	
	public void drawFields() {
		drawFields =! drawFields;
	}
	
	public void calculateFields() {
		calculateFields =! calculateFields;
		updateFieldForce();
	}

	private void updateFieldForce() {
		
		if(calculateFields) {
			s.grid = null;
			s.grid = new YeeGrid(s);
			s.boundary = new PeriodicBoundary();
			updateSimulationSize();
		}
		else {
			s.grid = null;
			s.grid = new Grid(s);
			//clears forces ArrayList of all GridForces
			for (int i = 0; i < s.f.forces.size(); i++) {
				if (s.f.forces.get(i) instanceof SimpleGridForce){
					s.f.forces.remove(i);
				}
			}
			//clears Particle2DData variable
			for (Particle2D p : s.particles) {
				p.pd = null;
			}
		}
	}
	
	public void writePosition() {
		writePosition =! writePosition;
		if(writePosition)
		{
			s.f.clear();
			ConstantForce force = new ConstantForce();
			//force.bz = - 0.23; // -ConstantsSI.g;
			s.f.add(force);
			InitialConditions.createRandomParticles(s, 1, 10);
			Particle2D par = (Particle2D) s.particles.get(0);
			par.x = this.getWidth() * 0.5;;
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
		ParticleMover.completeAllParticles(s);
		
		switch(id) {
		case 0:
			s.psolver = new EulerRichardson();
			break;
		case 1:
			s.psolver = new LeapFrog();
			break;
		case 2:
			s.psolver = new LeapFrogDamped();
			break;
		case 3:
			s.psolver = new LeapFrogHalfStep();
			break;
		case 4:
			s.psolver = new Boris();
			break;
		case 5:
			s.psolver = new BorisDamped();
			break;
		case 6:
			s.psolver = new SemiImplicitEuler();
			break;
		case 7:
			s.psolver = new Euler();
			break;
			}

		ParticleMover.prepareAllParticles(s);
	}
	
	public void relativisticEffects(int i) {
		relativistic =! relativistic;
		
		if(relativistic == false) {
			for (int j = 0; j < s.f.forces.size(); j++) {
				if (s.f.forces.get(j) instanceof ConstantForceRelativistic){
					s.f.forces.set(j, new ConstantForce());
				}
				if (s.f.forces.get(j) instanceof SimpleGridForceRelativistic){
					s.f.forces.set(j, new SimpleGridForce(s));
				}
				if (s.f.forces.get(j) instanceof SpringForceRelativistic){
					s.f.forces.set(j, new SpringForce());
				}
			}
			switch(i) {
			case 1:
				s.psolver = new LeapFrog();
			case 4:
				s.psolver = new Boris();
				break;
			case 6:
				s.psolver = new SemiImplicitEuler();
				break;
			}
		}
		
		if(relativistic == true) {
			//System.out.println("relativistic version on");
			for (int j = 0; j < s.f.forces.size(); j++) {
				if (s.f.forces.get(j) instanceof ConstantForce){
					s.f.forces.set(j, new ConstantForceRelativistic());
				}
				if (s.f.forces.get(j) instanceof SimpleGridForce){
					s.f.forces.set(j, new SimpleGridForceRelativistic(s));
				}
				if (s.f.forces.get(j) instanceof SpringForce){
					s.f.forces.set(j, new SpringForceRelativistic());
				}
			}
			switch(i) {
			case 1:
				s.psolver = new LeapFrogRelativistic();
			case 4:
				s.psolver = new BorisRelativistic();
				break;
			case 6:
				s.psolver = new SemiImplicitEulerRelativistic();
				break;
			}
		}
		
	}
	
	public void collisionChange(int i) {
		switch(i) {
		case 0:
			s.collisionBoolean = false;
			s.collision.det = new Detector();
			s.collision.alg = new CollisionAlgorithm();
			break;
		case 1:
			s.collisionBoolean = true;
			s.collision.det = new AllParticles();
			s.collision.det.resetEveryStep();
			s.collision.det.addEveryStep(s.particles);
			s.collision.alg = new SimpleCollision();
			break;
		}
	}
	
	public void detectorChange(int i) {
		switch(i) {
		case 0:
			
			s.collision.det = new AllParticles();
			s.collision.det.resetEveryStep();
			s.collision.det.addEveryStep(s.particles);
			break;
		case 1:
			s.collision.det = new SweepAndPrune();
			s.collision.det.reset();
			s.collision.det.add(s.particles);
			break;
		}
	}
	
	public void algorithmCollisionChange(int i) {
		switch(i) {
		case 0:
			s.collision.alg = new SimpleCollision();
			break;
		case 1:
			s.collision.alg = new VectorTransformation();
			break;
		case 2:
			s.collision.alg = new MatrixTransformation();
			break;
		}
	}
	
	public void boundariesChange(int i) {
		switch(i) {
		case 0:
			InitialConditions.setHardWallBoundary(s);
			break;
		case 1:
			InitialConditions.setPeriodicBoundary(s);
		}
		
	}

	/** Display the particles */
	public void paintComponent(Graphics graph1) {
		Graphics2D graph = (Graphics2D) graph1;
		setBackground(Color.white);
		graph.translate(0.0, this.getHeight());
		graph.scale(1.0, -1.0);

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
		for (int i = 0; i < s.particles.size(); i++) {
			Particle2D par = (Particle2D) s.particles.get(i);
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
			for(int i = 0; i < s.grid.numCellsX; i++)
				for(int k = 0; k < s.grid.numCellsY; k++)
				{
					int xstart = (int) (s.grid.cellWidth * (i + 0.5));
					int ystart = (int) (s.grid.cellHeight * (k + 0.5));
					drawArrow(graph, xstart, ystart, (int) Math.round(s.grid.jx[i][k] + xstart), (int) Math.round(s.grid.jy[i][k] + ystart));
				}
			//return;
		}
		
		if(drawFields)
		{
			graph.setColor(Color.black);
			for(int i = 0; i < s.grid.numCellsX; i++)
				for(int k = 0; k < s.grid.numCellsY; k++)
				{
					int xstart = (int) (s.grid.cellWidth * (i + 0.5));
					int ystart = (int) (s.grid.cellHeight * (k + 0.5));
					drawArrow(graph, xstart, ystart, (int) Math.round(s.grid.Ex[i][k] + xstart), (int) Math.round(s.grid.Ey[i][k] + ystart));
				}
			//return;
		}

		if (showinfo) {
			graph.translate(0.0, this.getHeight());
			graph.scale(1.0, -1.0);
			graph.setColor(darkGreen);
			graph.drawString("Frame rate: " + frameratedetector.getRateString() + " fps", 30, 30);
			graph.drawString("Time step: " + (float) s.tstep, 30, 50);

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
