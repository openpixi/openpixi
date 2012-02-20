package org.openpixi.pixi.ui;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.solver.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Displays 2D particles.
 */
public class Particle2DPanel extends JPanel {

	//private static final int step = 30;
	public int step;

	private boolean reset_trace;
	
	/** Milliseconds between updates */
	private int interval = step;

	/** Timer for animation */
	public Timer timer;

	/** Slider state ??? (do we really need this?) */
	public boolean sl = false;
	
	/** A state for the trace */
	public boolean paint_trace = false;

	private static int NUM_PARTICLES = 10;

	/** Constant force for particles */
	public Force f = new Force();

	/** Contains all particles */
	ArrayList<Particle2D> parlist = new ArrayList<Particle2D>();

	/** Listener for timer */
	public class TimerListener implements ActionListener {

		public void actionPerformed(ActionEvent eve) {
			for (int i = 0; i < NUM_PARTICLES; i++) {
				Particle2D par = (Particle2D) parlist.get(i);
				par.setBoundaries(getHeight(), getWidth());
				EulerRichardson.algorithm(par, f, 0.5);
			}
			repaint();
		}
	}

	/** Constructor */
	public Particle2DPanel() {
		timer = new Timer(interval, new TimerListener());

		// Set properties of the panel
		this.setVisible(true);
		this.setSize(700, 500);

		// Create all particles
		initRandomParticles(10, 15);
	}

	public void startAnimation() {
		timer.start();
		sl = false;
	}

	public void stopAnimation() {
		timer.stop();
		sl = true;
	}

	public void resetAnimation(int id) {
		// timer.restart();
		timer.stop();
		reset_trace = true;
		switch(id) {
		case 0:
			initRandomParticles(10, 15);
			break;
		case 1:
			initRandomParticles(100, 10);
			break;
		case 2:
			initRandomParticles(1000, 5);
			break;
		case 3:
			initRandomParticles(10000, 2);
			break;
		case 4:
			initGravity(1);
			break;
		case 5:
			initMagnetic(3);
			break;
		}
		sl = true;
		timer.start();
	}

	private void initRandomParticles(int count, int radius) {
		NUM_PARTICLES = count;
		parlist.clear();
		for (int k = 0; k < NUM_PARTICLES; k++) {
			Particle2D par = new Particle2D();
			par.x = 700 * Math.random();
			par.y = 500 * Math.random();
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
	}
	
	public void checkTrace() {
		paint_trace =! paint_trace;
		startAnimation();
	}

	/** Display the particles */
	public void paintComponent(Graphics graph1) {
		Graphics2D graph = (Graphics2D) graph1;
		setBackground(Color.gray);
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
			if(paint_trace)
			{
				int resize_factor = 0;
				switch((int) par.radius)
				{
				case 2:
					resize_factor = 1;
					break;
				case 5:
					resize_factor = 2;
					break;
				case 10:
					resize_factor = 3;
					break;
				case 15:
					resize_factor = 5;
					break;
				}
				graph.fillOval((int) par.x, (int) par.y, (int) par.radius / resize_factor, (int) par.radius / resize_factor);
			}
			else
			graph.fillOval((int) par.x, (int) par.y, (int) par.radius, (int) par.radius);
		}
		
	}
}
