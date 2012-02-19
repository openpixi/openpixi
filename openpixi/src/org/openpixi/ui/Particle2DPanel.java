package org.openpixi.ui;

import org.openpixi.physics.*;
import org.openpixi.physics.solver.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Displays 2D particles.
 */
public class Particle2DPanel extends JPanel {

	private static final int step = 30;

	/** Milliseconds between updates */
	private int interval = step;

	/** Timer for animation */
	public Timer timer;

	/** Slider state ??? (do we really need this?) */
	public boolean sl = false;

	// defining the initial conditions for the particle
	// private static final double x = 0.0;
	// private static final double y = 400.0;
	// private static final double vx = 30.0;
	// private static final double vy = 30.0;

	private static int NUM_PARTICLES = 10;

	/** Constant force for particles */
	private Force f = new Force(0.0, 1.1, 1.2, 1.3);

	/** Contains all particles */
	//ArrayList<Particle2D> parlist = new ArrayList<Particle2D>();

	/** Contains all particles */
	ArrayList<Euler> parlist = new ArrayList<Euler>();
			
	/** Listener for timer */
	/*public class TimerListener implements ActionListener {

		public void actionPerformed(ActionEvent eve) {
			for (int i = 0; i < NUM_PARTICLES; i++) {
				Particle2D par = (Particle2D) parlist.get(i);
				par.setBoundaries(getHeight(), getWidth());
				par.algorithm(0.5, f);
			}
			repaint();
		}
	}*/
	public class TimerListener implements ActionListener {

		public void actionPerformed(ActionEvent eve) {
			for (int i = 0; i < NUM_PARTICLES; i++) {
				Euler par = (Euler) parlist.get(i);
				par.getParticle2D().setBoundaries(getHeight(), getWidth());
				par.algorithm(0.5);
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
		initRandomParticles(10);
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
		switch(id) {
		case 0:
			initRandomParticles(10);
			break;
		case 1:
			initRandomParticles(100);
			break;
		case 2:
			initRandomParticles(1000);
			break;
		case 3:
			initRandomParticles(10000);
			break;
		case 4:
			initGravity(1);
			break;
		}
		sl = true;
		timer.start();
	}

	private void initRandomParticles(int count) {
		NUM_PARTICLES = count;
		parlist.clear();
		for (int k = 0; k < NUM_PARTICLES; k++) {
			Particle2D par = new Particle2D();
			par.x = Math.random();
			par.y = Math.random();
			par.vx = 100 * Math.random();
			par.vy = 100 * Math.random();
			par.setMass(1);
			par.setCharge(1);
			
			Euler euler = new Euler(par, f);
			parlist.add(euler);
		}
		for (int i = 0; i < NUM_PARTICLES; i++) {
			Particle2D part = new Particle2D(Math.random(), Math.random(), 100 * Math
					.random(), 100 * Math.random(), 0.0, 0.0,
					Math.random() + 1, 10 * Math.random());
			parlist.add(new Euler(part, f));
		}

		f = new Force(0.0, 1.1, 1.2, 1.3);
	}

	private void initGravity(int count) {
		initRandomParticles(count);

		f = new Force(0.0, 0.0, 0.0, 0.0);
	}

	/** Display the particles */
	/*public void paintComponent(Graphics graph) {
		setBackground(Color.gray);
		super.paintComponent(graph);
		for (int i = 0; i < NUM_PARTICLES; i++) {
			Particle2D par = (Particle2D) parlist.get(i);
			graph.setColor(Color.blue);
			graph.fillOval((int) par.x, (int) par.y, 15, 15);
		}
	}*/
	/** Display the particles */
	public void paintComponent(Graphics graph) {
		setBackground(Color.gray);
		super.paintComponent(graph);
		for (int i = 0; i < NUM_PARTICLES; i++) {
			Euler par = (Euler) parlist.get(i);
			graph.setColor(Color.blue);
			graph.fillOval((int) par.getParticle2D().x, (int) par.getParticle2D().y, 15, 15);
		}
	}

}