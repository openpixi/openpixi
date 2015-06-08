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
    //Test commit.

	private JButton startButton;
	private JButton stopButton;
	private JButton resetButton;

	private JSlider speedSlider;
//	private JSlider stepSlider;

//	private JCheckBox relativisticCheck;

//	private JComboBox algorithmComboBox;

	private JTabbedPane tabs;

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

//	class AlgorithmListener implements ActionListener {
//		public void actionPerformed(ActionEvent eve) {
//			JComboBox cbox = (JComboBox) eve.getSource();
//			int id = cbox.getSelectedIndex();
//			simulationAnimation.algorithmChange(id);
//			if (id == 0) {
//				relativisticCheck.setEnabled(true);
//			}
//			else {
//				relativisticCheck.setEnabled(false);
//			}
//		}
//	}

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
	 * Listener for reset button.
	 */
	class ResetListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			//simulationAnimation.resetAnimation(initComboBox.getSelectedIndex());
			fileTab.applyTextAreaSettings();

			setSlidersValue();
		}
	}

//	class RelativisticEffects implements ItemListener {
//		public void itemStateChanged(ItemEvent eve){
//			int i = algorithmComboBox.getSelectedIndex();
//			simulationAnimation.relativisticEffects(i);
//		}
//	}

	class StepListener implements ChangeListener{
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

		simulationAnimation = new SimulationAnimation();
		panelManager = new PanelManager(this);
		Simulation s = simulationAnimation.getSimulation();

		startButton = new JButton("start");
		stopButton = new JButton("stop");
		resetButton = new JButton("reset");

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

//		stepSlider = new JSlider();
//		stepSlider.addChangeListener(new StepListener());
//		stepSlider.setMinimum(1);
//		stepSlider.setMaximum(100);
//		stepSlider.setValue((int)(s.tstep / stepSliderScaling));
//		stepSlider.setMajorTickSpacing(10);
//		stepSlider.setMinorTickSpacing(2);
//		stepSlider.setPaintTicks(true);
//		JLabel stepLabel = new JLabel("Size of time step");
//		Box step = Box.createVerticalBox();
//		step.add(stepLabel);
//		step.add(stepSlider);

//		algorithmComboBox = new JComboBox(solverString);
//		algorithmComboBox.setSelectedIndex(0);
//		algorithmComboBox.addActionListener(new AlgorithmListener());
//		algorithmComboBox.setPreferredSize(new Dimension(algorithmComboBox.getPreferredSize().width, 5));
//		JLabel algorithmLabel = new JLabel("Algorithm");
//		Box algorithmBox = Box.createVerticalBox();
//		algorithmBox.add(algorithmLabel);
//		algorithmBox.add(algorithmComboBox);

		startButton.addActionListener(new StartListener());
		stopButton.addActionListener(new StopListener());
		resetButton.addActionListener(new ResetListener());

//		relativisticCheck = new JCheckBox("Relativistic Version");
//		relativisticCheck.addItemListener(new RelativisticEffects());
//		relativisticCheck.setEnabled(false);

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		controlPanel.add(startButton);
		controlPanel.add(stopButton);
		controlPanel.add(resetButton);
		controlPanel.add(Box.createHorizontalStrut(50));

		Box settingControls = Box.createVerticalBox();
		JLabel controlLabel = new JLabel("Global settings", SwingConstants.CENTER);
		settingControls.add(Box.createVerticalStrut(20));
		settingControls.add(controlLabel);
//		settingControls.add(algorithmBox);
//		settingControls.add(Box.createVerticalGlue());
//		settingControls.add(relativisticCheck);
		settingControls.add(Box.createVerticalGlue());
		settingControls.add(speed);
		settingControls.add(Box.createVerticalGlue());
//		settingControls.add(step);
//		settingControls.add(Box.createVerticalGlue());

		// Change background color of tab from blue to system gray
		UIManager.put("TabbedPane.contentAreaColor", new Color(238, 238, 238));

		tabs = new JTabbedPane();

		this.propertiesTab = new PropertiesTab(MainControlApplet.this, panelManager);
		this.fileTab = new FileTab(MainControlApplet.this, simulationAnimation, panelManager);

		settingControls.setPreferredSize(new Dimension (300, 100));

		tabs.addTab("Settings", settingControls);
		tabs.addTab("Properties", propertiesTab);
		tabs.addTab("File", fileTab);

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

//		stepSlider.setValue((int)(s.tstep / stepSliderScaling));
		speedSlider.setValue(50);
		timer.setDelay((int) (1000 * Math.exp(-50 * speedSliderScaling)));


//		// Set algorithm UI according to current setting
//		Solver solver = s.getParticleMover().getSolver();
//		if (solver instanceof LeapFrog)
//		{
//			algorithmComboBox.setSelectedIndex(0);
//			relativisticCheck.setSelected(false);
//		} else if (solver instanceof LeapFrogRelativistic)
//		{
//			algorithmComboBox.setSelectedIndex(0);
//			relativisticCheck.setSelected(true);
//		}
	}

	@Override
	public void init() {
		super.init();

		Timer timer = simulationAnimation.getTimer();
		timer.start();
		setSlidersValue();
	}

	/**
	 * Entry point for java application.
	 */
	public static void main(String[] args) {

		JFrame web = new JFrame();

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
