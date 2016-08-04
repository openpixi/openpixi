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

import java.awt.*;
import javax.swing.*;

import java.awt.event.*;
import java.util.Locale;

import javax.swing.event.*;

import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.tab.FileTab;
import org.openpixi.pixi.ui.tab.PropertiesTab;

/**
 * Displays the animation of particles.
 */
public class MainControlApplet extends JApplet
{
	private JButton startButton;
	private JButton stopButton;
	private JButton stepButton;
	private JButton resetButton;

	private JSlider speedSlider;

	protected JTabbedPane tabs;

	protected SimulationAnimation simulationAnimation;
	private PanelManager panelManager;

	protected PropertiesTab propertiesTab;
	private FileTab fileTab;

	private static final double speedSliderScaling = 0.07;
	private static final double stepSliderScaling = 0.01;

	String[] solverString = {
			"LeapFrog"};
	/**
	 * Listener for slider.
	 */
	class SliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent eve) {
			Timer timer = simulationAnimation.getTimer();
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				int delay = (int) (1000 * Math.exp(-source.getValue() * speedSliderScaling));
				timer.setDelay(delay);
			}
		}
	}

	/**
	 * Listener for start button.
	 */
	class StartListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			simulationAnimation.startAnimation();
		}
	}

	/**
	 * Listener for stop button.
	 */
	class StopListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			simulationAnimation.stopAnimation();
		}
	}

	/**
	 * Listener for step button.
	 */
	class StepListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			simulationAnimation.stepAnimation();
		}
	}

	/**
	 * Listener for reset button.
	 */
	class ResetListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			fileTab.applyTextAreaSettings();

			setSlidersValue();
		}
	}

	class StepListener2 implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			Simulation s = simulationAnimation.getSimulation();
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * stepSliderScaling;
				s.tstep = value;
			}
		}
	}

	/**
	 * Constructor.
	 */
	public MainControlApplet() {
		Debug.checkAssertsEnabled();

		// Set US locale for numeric output ("1.23"[US] instead of "1,23"[DE])
		Locale.setDefault(Locale.US);

		simulationAnimation = new SimulationAnimation();
		panelManager = new PanelManager(this);
		Simulation s = simulationAnimation.getSimulation();

		startButton = new JButton("Start");
		stopButton = new JButton("Stop");
		stepButton = new JButton("Step");
		resetButton = new JButton("Reset");

		/**one can also write a constructor for a JSlider as:
		 * JSlider slider = new JSlider(int min, int max, int value);
		 * where min is the minimal value (the same as setMinimum(int min),
		 * max is the maximal value (the same as setMinimum(int max),
		 * and value is the current value (the same as setValue(int value),
		 * and the code would be shorter,
		 * but they are written like this, so it is clearer and not so confusing
		 */

		speedSlider = new JSlider();
		speedSlider.addChangeListener(new SliderListener());
		speedSlider.setMinimum(0);
		speedSlider.setMaximum(100);
		speedSlider.setValue(30);
		speedSlider.setMajorTickSpacing(5);
		speedSlider.setMinorTickSpacing(1);
		speedSlider.setPaintTicks(true);
		JLabel speedLabel = new JLabel("Frame rate");
		Box speed = Box.createVerticalBox();
		speed.add(speedLabel);
		speed.add(speedSlider);

		startButton.addActionListener(new StartListener());
		stopButton.addActionListener(new StopListener());
		stepButton.addActionListener(new StepListener());
		resetButton.addActionListener(new ResetListener());

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		controlPanel.add(startButton);
		controlPanel.add(stopButton);
		controlPanel.add(stepButton);
		controlPanel.add(resetButton);
		controlPanel.add(Box.createHorizontalStrut(50));

		Box settingControls = Box.createVerticalBox();
		JLabel controlLabel = new JLabel("Global settings", SwingConstants.CENTER);
		settingControls.add(Box.createVerticalStrut(20));
		settingControls.add(controlLabel);
		settingControls.add(Box.createVerticalGlue());
		settingControls.add(speed);
		settingControls.add(Box.createVerticalGlue());

		// Change background color of tab from blue to system gray
		UIManager.put("TabbedPane.contentAreaColor", new Color(238, 238, 238));

		tabs = new JTabbedPane();

		this.propertiesTab = new PropertiesTab(MainControlApplet.this, panelManager);
		this.fileTab = new FileTab(MainControlApplet.this, simulationAnimation, panelManager);

		settingControls.setPreferredSize(new Dimension (300, 100));

		tabs.addTab("File", fileTab);
		tabs.addTab("Properties", propertiesTab);
		tabs.addTab("Settings", settingControls);

		Component mainPanel = panelManager.getDefaultPanel();

		this.setLayout(new BorderLayout());
		this.add(controlPanel, BorderLayout.SOUTH);
		this.add(mainPanel, BorderLayout.CENTER);
		this.add(tabs, BorderLayout.EAST);

		panelManager.replaceMainPanel(mainPanel);

	}

	public void setText(JTextArea text, String str, boolean onoff)
	{
		if(onoff)
			text.insert(str, 0);
		else
			text.replaceRange(" ", 0, text.getDocument().getLength());
	}

	public void setSlidersValue()
	{
		Simulation s = simulationAnimation.getSimulation();
		Timer timer = simulationAnimation.getTimer();

		speedSlider.setValue(50);
		timer.setDelay((int) (1000 * Math.exp(-50 * speedSliderScaling)));
	}

	@Override
	public void init() {
		super.init();

		Timer timer = simulationAnimation.getTimer();
		timer.start();
		setSlidersValue();
	}

	public static JFrame web;

	/**
	 * Entry point for java application.
	 */
	public static void main(String[] args) {

		web = new JFrame();
		web.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		web.setTitle("OpenPixi");
		MainControlApplet applet = new MainControlApplet();
		web.setContentPane(applet);

		web.pack();
		web.setVisible(true);
		web.setSize(800, 550);
		web.setResizable(true);

		applet.init();
	}

}
