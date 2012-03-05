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

/**
 * Displays the animation of particles.
 */
public class MainControlApplet extends JApplet {

	private JButton startButton, stopButton, resetButton;
	private JSlider speedSlider;
	private JSlider stepSlider;
	
	private JSlider  dragSlider;
	
	private JSlider efieldXSlider;
	private JSlider efieldYSlider;
	private JSlider bfieldZSlider;
	private JSlider gfieldXSlider;
	private JSlider gfieldYSlider;
	
	private JCheckBox framerateCheck;
	
	private JComboBox initComboBox;
	private JCheckBox traceCheck;
	private Particle2DPanel particlePanel;

	private static final double speedSliderScaling = 0.07;
	private static final double stepSliderScaling = 0.01;
	private static final double dragSliderScaling = 0.01;
	private static final double exSliderScaling = 0.05;
	private static final double eySliderScaling = 0.05;
	private static final double bzSliderScaling = 0.005;
	private static final double gxSliderScaling = 0.01;
	private static final double gySliderScaling = 0.01;

	String[] initStrings = {
			"10 random particles",
			"100 random particles",
			"1000 random particles",
			"10000 random particles",
			"Single particle in gravity",
			"3 part. in magnetic field" };

	/**
	 * Listener for slider.
	 */
	class SliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				int delay = (int) (1000 * Math.exp(-source.getValue() * speedSliderScaling));
				particlePanel.timer.setDelay(delay);
			}
		}
	}
	
	class ComboBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox) e.getSource();
			int id  = cb.getSelectedIndex();
			particlePanel.resetAnimation(id);
			particlePanel.resetAnimation(initComboBox.getSelectedIndex());
			setSlidersValue();
		}
	}

	/**
	 * Listener for start button.
	 */
	class StartListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			particlePanel.startAnimation();
		}
	}

	/**
	 * Listener for stop button.
	 */
	class StopListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			particlePanel.stopAnimation();
		}
	}

	/**
	 * Listener for reset button.
	 */
	class ResetListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			particlePanel.resetAnimation(initComboBox.getSelectedIndex());
			setSlidersValue();
		}
	}
	
	class CheckListener implements ItemListener {
		public void itemStateChanged(ItemEvent eve){
				particlePanel.checkTrace();
		}
	}
	
	class FrameListener implements ItemListener {
		public void itemStateChanged(ItemEvent eve) {
			if(eve.getStateChange() == ItemEvent.SELECTED) {
				particlePanel.showinfo = true;
			} else if(eve.getStateChange() == ItemEvent.DESELECTED) {
				particlePanel.showinfo = false;
			}
		}
	}
	
	
	class DragListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * dragSliderScaling;
				particlePanel.f.drag = value;
			}
		}
	}
	
	class EFieldXListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * exSliderScaling;
				particlePanel.f.ex = value;
			}
		}
	}
	
	class EFieldYListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * eySliderScaling;
				particlePanel.f.ey = value;
			}
		}
	}
	
	class BFieldZListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * bzSliderScaling;
				particlePanel.f.bz = value;
			}
		}
	}
	
	class GFieldXListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * gxSliderScaling;
				particlePanel.f.gx = value;
			}
		}
	}
	
	class GFieldYListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * gySliderScaling;
				particlePanel.f.gy = value;
			}
		}
	}
	
	class StepListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * stepSliderScaling;
				particlePanel.step = value;
			}
		}
	}
	
	/**
	 * Constructor.
	 */
	public MainControlApplet() {
		particlePanel = new Particle2DPanel();

		this.setVisible(true);
		this.setSize(1000, 500);

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
		speed.add(speedSlider);
		speed.add(speedLabel);
		
		stepSlider = new JSlider();
		stepSlider.addChangeListener(new StepListener());
		stepSlider.setMinimum(1);
		stepSlider.setMaximum(100);
		stepSlider.setValue((int) (100 * (particlePanel.step = 0.5)));
		stepSlider.setMajorTickSpacing(10);
		stepSlider.setMinorTickSpacing(2);
		stepSlider.setPaintTicks(true);
		JLabel stepLabel = new JLabel("Size of time step");
		Box step = Box.createVerticalBox();
		step.add(stepSlider);
		step.add(stepLabel);
		
		dragSlider = new JSlider();
		dragSlider.addChangeListener(new DragListener());
		dragSlider.setMinimum(0);
		dragSlider.setMaximum(100);
		dragSlider.setValue((int) particlePanel.f.drag);
		dragSlider.setMajorTickSpacing(10);
		dragSlider.setMinorTickSpacing(2);
		dragSlider.setPaintTicks(true);
		
		efieldXSlider = new JSlider();
		efieldXSlider.addChangeListener(new EFieldXListener());
		efieldXSlider.setMinimum(-100);
		efieldXSlider.setMaximum(100);
		efieldXSlider.setValue((int) particlePanel.f.ex);
		efieldXSlider.setMajorTickSpacing(20);
		efieldXSlider.setMinorTickSpacing(5);
		efieldXSlider.setPaintTicks(true);
		
		efieldYSlider = new JSlider();
		efieldYSlider.addChangeListener(new EFieldYListener());
		efieldYSlider.setMinimum(-100);
		efieldYSlider.setMaximum(100);
		efieldYSlider.setValue((int) particlePanel.f.ey);
		efieldYSlider.setMajorTickSpacing(20);
		efieldYSlider.setMinorTickSpacing(5);
		efieldYSlider.setPaintTicks(true);
		
		bfieldZSlider = new JSlider();
		bfieldZSlider.addChangeListener(new BFieldZListener());
		bfieldZSlider.setMinimum(-100);
		bfieldZSlider.setMaximum(100);
		bfieldZSlider.setValue((int) particlePanel.f.bz);
		bfieldZSlider.setMajorTickSpacing(20);
		bfieldZSlider.setMinorTickSpacing(5);
		bfieldZSlider.setPaintTicks(true);
		
		gfieldXSlider = new JSlider();
		gfieldXSlider.addChangeListener(new GFieldXListener());
		gfieldXSlider.setMinimum(-100);
		gfieldXSlider.setMaximum(100);
		gfieldXSlider.setValue((int) particlePanel.f.gx);
		gfieldXSlider.setMajorTickSpacing(20);
		gfieldXSlider.setMinorTickSpacing(5);
		gfieldXSlider.setPaintTicks(true);
		
		gfieldYSlider = new JSlider();
		gfieldYSlider.addChangeListener(new GFieldYListener());
		gfieldYSlider.setMinimum(-100);
		gfieldYSlider.setMaximum(100);
		gfieldYSlider.setValue((int) particlePanel.f.gy);
		gfieldYSlider.setMajorTickSpacing(20);
		gfieldYSlider.setMinorTickSpacing(5);
		gfieldYSlider.setPaintTicks(true);
		
		initComboBox = new JComboBox(initStrings);
		initComboBox.setSelectedIndex(0);
		initComboBox.addActionListener(new ComboBoxListener());
		
		startButton.addActionListener(new StartListener());
		stopButton.addActionListener(new StopListener());
		resetButton.addActionListener(new ResetListener());
		
		traceCheck = new JCheckBox("Trace");
		traceCheck.addItemListener(new CheckListener());
		
		framerateCheck = new JCheckBox("Info");
		framerateCheck.addItemListener(new FrameListener());

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		controlPanel.add(startButton);
		controlPanel.add(stopButton);
		controlPanel.add(resetButton);
		controlPanel.add(initComboBox);
		controlPanel.add(speed);
		controlPanel.add(step);
		controlPanel.add(traceCheck);
		controlPanel.add(framerateCheck);
		
		JLabel dragLabel = new JLabel("Drag coefficient");
		
		JLabel eFieldXLabel = new JLabel("Electric Field in x - direction");
		JLabel eFieldYLabel = new JLabel("Electric Field in y - direction");
		JLabel bFieldZLabel = new JLabel("Magnetic Field in z - direction");
		JLabel gFieldXLabel = new JLabel("Gravitation in x - direction Field");
		JLabel gFieldYLabel = new JLabel("Gravitation in y - direction Field");
		
		Box fieldsBox = Box.createVerticalBox();
		fieldsBox.add(eFieldXLabel);
		fieldsBox.add(efieldXSlider);
		fieldsBox.add(eFieldYLabel);
		fieldsBox.add(efieldYSlider);
		fieldsBox.add(Box.createVerticalGlue());
		fieldsBox.add(bFieldZLabel);
		fieldsBox.add(bfieldZSlider);
		fieldsBox.add(Box.createVerticalGlue());
		fieldsBox.add(gFieldXLabel);
		fieldsBox.add(gfieldXSlider);
		fieldsBox.add(gFieldYLabel);
		fieldsBox.add(gfieldYSlider);
		fieldsBox.add(Box.createVerticalGlue());
		fieldsBox.add(dragLabel);
		fieldsBox.add(dragSlider);
		fieldsBox.add(Box.createVerticalGlue());
		
		this.setLayout(new BorderLayout());
		this.add(controlPanel, BorderLayout.SOUTH);
		this.add(particlePanel, BorderLayout.CENTER);
		this.add(fieldsBox, BorderLayout.EAST);

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
		particlePanel.step = 0.5;
		stepSlider.setValue(50);
		efieldXSlider.setValue((int) (particlePanel.f.ex / exSliderScaling));
		efieldYSlider.setValue((int) (particlePanel.f.ey / eySliderScaling));
		bfieldZSlider.setValue((int) (particlePanel.f.bz / bzSliderScaling));
		gfieldXSlider.setValue((int) (particlePanel.f.gx / gxSliderScaling));
		gfieldYSlider.setValue((int) (particlePanel.f.gy / gySliderScaling));
		dragSlider.setValue((int) (particlePanel.f.drag / dragSliderScaling));
		int delay = particlePanel.timer.getDelay();
		speedSlider.setValue((int) (-Math.log(delay / 1000.) / speedSliderScaling));
	}


	@Override
	public void init() {
		super.init();

		particlePanel.timer.start();
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
		web.setSize(1000, 500);
		
		applet.init();
	}

}
