package org.openpixi.pixi.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Timer;

import org.openpixi.pixi.physics.InitialConditions;
import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.panel.Particle2DPanel;
import org.openpixi.pixi.ui.util.FrameRateDetector;

/**
 * Wrapper for the simulation class in GUI applications.
 */
public class SimulationAnimation {

	private Simulation s;

//	private boolean relativistic = true;

//	private boolean calculateFields = false;

	/** Milliseconds between updates */
	private int interval = 30;

	/** Timer for animation */
	private Timer timer;

	private FrameRateDetector frameratedetector;

	private ArrayList<SimulationAnimationListener> listeners = new ArrayList<SimulationAnimationListener>();

	/** Constructor */
	public SimulationAnimation() {
		timer = new Timer(interval, new TimerListener());
		frameratedetector = new FrameRateDetector(500);
		s = InitialConditions.initEmptySimulation();
	}

	/** Listener for timer */
	public class TimerListener implements ActionListener {

		public void actionPerformed(ActionEvent eve) {
			try {
				s.step();
			} catch (FileNotFoundException ex) {
				Logger.getLogger(Particle2DPanel.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException ex2) {
				Logger.getLogger(Particle2DPanel.class.getName()).log(Level.SEVERE, null, ex2);
			}
			frameratedetector.update();
			repaint();
		}
	}

	public void startAnimation() {
		timer.start();
	}

	public void stopAnimation() {
		timer.stop();
		//test = false;
	}

	public Simulation getSimulation() {
		return s;
	}

	public FrameRateDetector getFrameRateDetector() {
		return frameratedetector;
	}

	public Timer getTimer() {
		return timer;
	}

	/**
	 * Add Listener for repaint() event.
	 * @param listener
	 */
	public void addListener(SimulationAnimationListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes Listener for repaint() event.
	 * @param listener
	 */
	public void removeListener(SimulationAnimationListener listener) {
		listeners.remove(listener);
	}

	private void repaint() {
		// Let all listeners know
		for (SimulationAnimationListener l : listeners) {
			l.repaint();
		}
	}

	private void clear() {
		// Let all listeners know
		for (SimulationAnimationListener l : listeners) {
			l.clear();
		}
	}

	public void resetAnimation(int id) {
		// timer.restart();
		timer.stop();
		clear();
		switch(id) {
			case 0:
				s = InitialConditions.initEmptySimulation();
				break;
		}
//		updateFieldForce();
		s.prepareAllParticles();
		s.turnGridForceOn();
		timer.start();
	}

	/**
	 * Reset animation according to settings
	 *
	 * @param settings New settings for animation.
	 */
	public void resetAnimation(Settings settings) {
		// timer.restart();
		timer.stop();
		clear();
		s = new Simulation(settings);
		//updateFieldForce();
		//s.prepareAllParticles();
		//s.turnGridForceOn();
		timer.start();
	}

//	public void calculateFields() {
//		calculateFields =! calculateFields;
//		updateFieldForce();
//	}

//	private void updateFieldForce() {
//
//		if(calculateFields) {
//			s.turnGridForceOn();
//		}
//		else {
//			s.turnGridForceOff();
//		}
//	}

//	public void algorithmChange(int id)
//	{
//		s.completeAllParticles();
//
//		switch(id)
//		{
//			case 0:
//				s.getParticleMover().setSolver(new LeapFrog());
//				break;
//		}
//
//		s.prepareAllParticles();
//	}

//	public void relativisticEffects(int i) {
//		relativistic =! relativistic;
//
//		if(relativistic == false) {
//			s.relativistic = false;
//			if (s.f instanceof CombinedForce) {
//				ArrayList<Force> forces = s.f.forces;
//				for (int j = 0; j < forces.size(); j++) {
//					if (forces.get(j) instanceof ConstantForceRelativistic){
//						forces.set(j, new ConstantForce());
//					}
//					if (forces.get(j) instanceof SimpleGridForceRelativistic){
//						forces.set(j, new SimpleGridForce());
//					}
//				}
//			}
//			switch(i) {
//			case 0:
//				s.getParticleMover().setSolver(new LeapFrog());
//				break;
//			}
//		}
//
//		if(relativistic == true) {
//			s.relativistic = true;
//			//System.out.println("relativistic version on");
//			if (s.f instanceof CombinedForce) {
//				ArrayList<Force> forces = s.f.forces;
//				for (int j = 0; j < forces.size(); j++) {
//					if (forces.get(j) instanceof ConstantForce){
//						forces.set(j, new ConstantForceRelativistic(s));
//					}
//					if (forces.get(j) instanceof SimpleGridForce){
//						forces.set(j, new SimpleGridForceRelativistic(s));
//					}
//				}
//			}
//			switch(i)
//			{
//				case 0:
//					s.getParticleMover().setSolver(new LeapFrogRelativistic(s));
//					break;
//			}
//		}
//
//	}

}
