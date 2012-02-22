package org.openpixi.pixi.ui;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.boundary.*;
import org.openpixi.pixi.physics.solver.*;
import org.openpixi.pixi.ui.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;


/**
 * Displays 2D particles.
 */
public class Particle2DPanel extends JPanel {
	
	private static final int xmax = 700;
	private static final int ymax = 500;

	public double step;

	private boolean reset_trace;
	
	private int algorithm_change = 0;
	
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

	Color darkGreen = new Color(0x00, 0x80, 0x00);

	/** Listener for timer */
	public class TimerListener implements ActionListener {

		public void actionPerformed(ActionEvent eve) {
			
			boundary.setBoundaries(0, 0, getWidth(), getHeight());
			for (int i = 0; i < NUM_PARTICLES; i++) {
				Particle2D par = (Particle2D) parlist.get(i);
				if(algorithm_change == 0)
					EulerRichardson.algorithm(par, f, step);
				else if(algorithm_change == 1)
						LeapFrog.algorithm(par, f, step);
					else if(algorithm_change == 2)
							LeapFrog.algorithmHalfStep(par, f, step);
				
				boundary.check(par);
			}
			frameratedetector.update();
			repaint();
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
			initMagnetic(3);
			break;
		}
		timer.start();
	}

	private void initRandomParticles(int count, int radius) {
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
			parlist.add(par);
		}
		f.reset();
		f.gy = -1; //-ConstantsSI.g;
		//f.bz = 1;
		
		setHardWallBoundary();
	}

	private void initGravity(int count) {
		initRandomParticles(count, 15);

		f.reset();
		f.gy = -1; // -ConstantsSI.g;
	}

	private void initMagnetic(int count) {
		initRandomParticles(count, 15);

		f.reset();
		f.bz = .1;
		
		setPeriodicBoundary();
	}
	
	public void checkTrace() {
		paint_trace =! paint_trace;
		startAnimation();
	}
	
	public void algorithmChange(int id)
	{
		algorithm_change = id;
	}

	public void setHardWallBoundary() {
		boundary = new HardWallBoundary();
	}

	public void setPeriodicBoundary() {
		boundary = new PeriodicBoundary();
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
}
