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

	private static int NUM_PARTICLES = 10;

	/** Constant force for particles */
	private Force f = new Force();

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
		case 5:
			initMagnetic(1);
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
			par.radius = 15;
			par.vx = 10 * Math.random();
			par.vy = 10 * Math.random();
			par.mass = 1;
			par.charge = 1;
			parlist.add(par);
		}
		f.reset();
		f.gy = -ConstantsSI.g;
		//f.bz = 1;
	}

	private void initGravity(int count) {
		initRandomParticles(count);

		f.reset();
		f.gy = -ConstantsSI.g;
	}

	private void initMagnetic(int count) {
		initRandomParticles(count);

		f.reset();
		f.bz = .1;
	}

	/** Display the particles */
	public void paintComponent(Graphics graph1) {
		super.paintComponent(graph1);
		Graphics2D graph = (Graphics2D) graph1;
		setBackground(Color.gray);
		graph.translate(0.0, 426);
		graph.scale(1.0, -1.0);
		for (int i = 0; i < NUM_PARTICLES; i++) {
			Particle2D par = (Particle2D) parlist.get(i);
			graph.setColor(Color.blue);
			graph.fillOval((int) par.x, (int) par.y, 15, 15);
		}
	}

}