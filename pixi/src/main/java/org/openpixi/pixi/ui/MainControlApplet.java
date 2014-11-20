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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.event.*;

import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.force.*;
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaryType;
import org.openpixi.pixi.physics.solver.*;
import org.openpixi.pixi.physics.solver.relativistic.*;
import org.openpixi.pixi.ui.panel.Particle2DPanel;
import org.openpixi.pixi.ui.panel.PhaseSpacePanel;
import org.openpixi.pixi.ui.panel.ElectricFieldPanel;

/**
 * Displays the animation of particles.
 */
public class MainControlApplet extends JApplet {

	private JButton startButton;
	private JButton stopButton;
	private JButton resetButton;

	private JSlider speedSlider;
	private JSlider stepSlider;

	private JSlider  dragSlider;

	private JSlider efieldXSlider;
	private JSlider efieldYSlider;
	private JSlider bfieldZSlider;
	private JSlider gfieldXSlider;
	private JSlider gfieldYSlider;

	private JCheckBox framerateCheck;
	private JCheckBox currentgridCheck;
	private JCheckBox drawFieldsCheck;
	private JCheckBox calculateFieldsCheck;
	private JCheckBox relativisticCheck;

	private JTextField xboxentry;
	private JTextField yboxentry;

	private JComboBox initComboBox;
	private JComboBox algorithmComboBox;
	private JCheckBox traceCheck;
	private JComboBox collisionComboBox;
	private JComboBox collisionAlgorithm;

	private JRadioButton hardBoundaries;
	private JRadioButton periodicBoundaries;

	JFileChooser fc;
	private JButton openButton;
	private JButton saveButton;
	private JButton applyButton;
	private JTextArea fileTextArea;

	private JTabbedPane tabs;
	private JSplitPane splitPane;

	private SimulationAnimation simulationAnimation;

	private Particle2DPanel particlePanel;
	private PhaseSpacePanel phaseSpacePanel;
	private ElectricFieldPanel electricFieldPanel;

	private static final double speedSliderScaling = 0.07;
	private static final double stepSliderScaling = 0.01;
	private static final double dragSliderScaling = 0.01;
	private static final double exSliderScaling = 0.5;
	private static final double eySliderScaling = 0.5;
	private static final double bzSliderScaling = 0.05;
	private static final double gxSliderScaling = 0.01;
	private static final double gySliderScaling = 0.01;

	private ConstantForce force = null;

	String[] initStrings = {
			"10 random particles",
			"100 random particles",
			"1000 random particles",
			"10000 random particles",
			"Single particle in gravity",
			"Single particle in el. Field",
			"3 part. in magnetic field",
            "Pair of particles",
            "Two stream instability"};

	String[] solverString = {
			"Euler Richardson",
			"LeapFrog",
			"LeapFrog Damped",
			"LeapFrog Half Step",
			"Boris",
			"Boris Damped",
			"Semi Implicit Euler",
			"Euler"};

	String[] collisionsString = {
			"No collisions",
			"All particles",
			"Sweep & Prune"
	};

	String[] collisionalgorithmString = {
			"Simple collision",
			"With vectors",
			"With matrices"
	};



	private void linkConstantForce() {
		Simulation s = simulationAnimation.getSimulation();
		force = getFirstConstantForce(s.f);
		if(force == null) {
			force = new ConstantForce();
			s.f.add(force);
		}
		assert force != null : "no force found";
	}

	/**
	 * Returns the first constant force encountered. Scans recursively through
	 * all CombindeForces.
	 * @param force
	 * @return
	 */
	private ConstantForce getFirstConstantForce(Force force) {
		ConstantForce firstconstantforce = null;
		if (force instanceof ConstantForce) {
			firstconstantforce = (ConstantForce) force;
		} else if (force instanceof CombinedForce) {
			for (Force f : ((CombinedForce) force).forces) {
				firstconstantforce = getFirstConstantForce(f);
				if (firstconstantforce != null) {
					break;
				}
			}
		}
		return firstconstantforce;
	}

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

	class ComboBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox) e.getSource();
			int id  = cb.getSelectedIndex();
			simulationAnimation.resetAnimation(id);
			linkConstantForce();
			setSlidersValue();
		}
	}

	class AlgorithmListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			JComboBox cbox = (JComboBox) eve.getSource();
			int id = cbox.getSelectedIndex();
			simulationAnimation.algorithmChange(id);
			if ((id == 1) || (id == 4) || (id == 6)) {
				relativisticCheck.setEnabled(true);
			}
			else {
				relativisticCheck.setEnabled(false);
			}
		}
	}
	class Collisions implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			JComboBox cbox = (JComboBox) eve.getSource();
			int i = cbox.getSelectedIndex();
			simulationAnimation.collisionChange(i);
			if(i == 0) {
				collisionAlgorithm.setEnabled(false);
				collisionAlgorithm.addItem("Enable collisions first");
				collisionAlgorithm.setSelectedItem("Enable collisions first");
			} else {
				collisionAlgorithm.setEnabled(true);
				//setSelectedIndex() automatically calls collisionAlgorithm.actionPerformed()!
				collisionAlgorithm.setSelectedIndex(0);
				collisionAlgorithm.removeItem("Enable collisions first");
			}
		}
	}

	class CollisionAlgorithm implements ActionListener {
		
		public void actionPerformed(ActionEvent eve) {
			JComboBox cbox = (JComboBox) eve.getSource();
			int i = cbox.getSelectedIndex();
			int j = collisionComboBox.getSelectedIndex();
			if (j == 0) {
				collisionAlgorithm.setSelectedItem("Enable collisions first");
			} else {
				simulationAnimation.algorithmCollisionChange(i);
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
	 * Listener for reset button.
	 */
	class ResetListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			simulationAnimation.resetAnimation(initComboBox.getSelectedIndex());
			linkConstantForce();
			setSlidersValue();
		}
	}

	class CheckListener implements ItemListener {
		public void itemStateChanged(ItemEvent eve){
				particlePanel.checkTrace();
		}
	}

	class SelectBoundaries implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			AbstractButton abut = (AbstractButton) eve.getSource();
			if(abut.equals(hardBoundaries)) {
				simulationAnimation.boundariesChange(0);
			}
			else if(abut.equals(periodicBoundaries)) {
				simulationAnimation.boundariesChange(1);
			}
		}
	}

	class RelativisticEffects implements ItemListener {
		public void itemStateChanged(ItemEvent eve){
			int i = (int)algorithmComboBox.getSelectedIndex();
			simulationAnimation.relativisticEffects(i);
			linkConstantForce();
		}
	}

	class DrawCurrentGridListener implements ItemListener {
		public void itemStateChanged(ItemEvent eve){
				particlePanel.drawCurrentGrid();
		}
	}

	class DrawFieldsListener implements ItemListener {
		public void itemStateChanged(ItemEvent eve){
				particlePanel.drawFields();
		}
	}

	class CalculateFieldsListener implements ItemListener {
		public void itemStateChanged(ItemEvent eve){
				simulationAnimation.calculateFields();
				if(eve.getStateChange() == ItemEvent.SELECTED) {
					currentgridCheck.setEnabled(true);
					drawFieldsCheck.setEnabled(true);
				}
				if(eve.getStateChange() == ItemEvent.DESELECTED) {
					currentgridCheck.setEnabled(false);
					drawFieldsCheck.setEnabled(false);
				}
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
				force.drag = value;
			}
		}
	}

	class EFieldXListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * exSliderScaling;
				force.ex = value;
			}
		}
	}

	class EFieldYListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * eySliderScaling;
				force.ey = value;
			}
		}
	}

	class BFieldZListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * bzSliderScaling;
				force.bz = value;
			}
		}
	}

	class GFieldXListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * gxSliderScaling;
				force.gx = value;
			}
		}
	}

	class GFieldYListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * gySliderScaling;
				force.gy = value;
			}
		}
	}

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

	class BoxDimension implements ActionListener{
		public void actionPerformed(ActionEvent eve) {
			Simulation s = simulationAnimation.getSimulation();
			int xbox = Integer.parseInt(xboxentry.getText());
			int ybox = Integer.parseInt(yboxentry.getText());
			double width = s.getWidth();
			double height = s.getHeight();
			s.grid.changeSize(xbox, ybox, width, height);
		}
	}

	class OpenButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {

			int returnVal = fc.showOpenDialog(MainControlApplet.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				try {
					String content = readFile(file);
					fileTextArea.setText(content);
				} catch (IOException e) {
					// TODO Error message
				}
			} else {
				// Open command cancelled by user
			}
		}
	}

	class SaveButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {

			int returnVal = fc.showSaveDialog(MainControlApplet.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				// This is where a real application would open the file.
				// log.append("Opening: " + file.getName() + "." + newline);
			} else {
				// log.append("Open command cancelled by user." + newline);
			}
		}
	}

	class ApplyButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {

		}
	}

	private String readFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}

		return stringBuilder.toString();
	}

	/**
	 * Constructor.
	 */
	public MainControlApplet() {
		Debug.checkAssertsEnabled();

		simulationAnimation = new SimulationAnimation();
		particlePanel = new Particle2DPanel(simulationAnimation);
		phaseSpacePanel = new PhaseSpacePanel(simulationAnimation);
		electricFieldPanel = new ElectricFieldPanel(simulationAnimation);
		Simulation s = simulationAnimation.getSimulation();
		linkConstantForce();

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

		stepSlider = new JSlider();
		stepSlider.addChangeListener(new StepListener());
		stepSlider.setMinimum(1);
		stepSlider.setMaximum(100);
		stepSlider.setValue((int)(s.tstep / stepSliderScaling));
		stepSlider.setMajorTickSpacing(10);
		stepSlider.setMinorTickSpacing(2);
		stepSlider.setPaintTicks(true);
		JLabel stepLabel = new JLabel("Size of time step");
		Box step = Box.createVerticalBox();
		step.add(stepLabel);
		step.add(stepSlider);

		dragSlider = new JSlider();
		dragSlider.addChangeListener(new DragListener());
		dragSlider.setMinimum(0);
		dragSlider.setMaximum(100);
		dragSlider.setValue((int) force.drag);
		dragSlider.setMajorTickSpacing(50);
		dragSlider.setMinorTickSpacing(10);
		dragSlider.setPaintTicks(true);
		dragSlider.setPaintLabels(true);
		JLabel dragLabel = new JLabel("Drag coefficient");

		efieldXSlider = new JSlider();
		efieldXSlider.addChangeListener(new EFieldXListener());
		efieldXSlider.setMinimum(-100);
		efieldXSlider.setMaximum(100);
		efieldXSlider.setValue((int) force.ex);
		efieldXSlider.setMajorTickSpacing(50);
		efieldXSlider.setMinorTickSpacing(10);
		efieldXSlider.setPaintTicks(true);
		efieldXSlider.setPaintLabels(true);

		efieldYSlider = new JSlider();
		efieldYSlider.addChangeListener(new EFieldYListener());
		efieldYSlider.setMinimum(-100);
		efieldYSlider.setMaximum(100);
		efieldYSlider.setValue((int) force.ey);
		efieldYSlider.setMajorTickSpacing(50);
		efieldYSlider.setMinorTickSpacing(10);
		efieldYSlider.setPaintTicks(true);
		efieldYSlider.setPaintLabels(true);

		bfieldZSlider = new JSlider();
		bfieldZSlider.addChangeListener(new BFieldZListener());
		bfieldZSlider.setMinimum(-100);
		bfieldZSlider.setMaximum(100);
		bfieldZSlider.setValue((int) force.bz);
		bfieldZSlider.setMajorTickSpacing(50);
		bfieldZSlider.setMinorTickSpacing(10);
		bfieldZSlider.setPaintTicks(true);
		bfieldZSlider.setPaintLabels(true);

		gfieldXSlider = new JSlider();
		gfieldXSlider.addChangeListener(new GFieldXListener());
		gfieldXSlider.setMinimum(-100);
		gfieldXSlider.setMaximum(100);
		gfieldXSlider.setValue((int) force.gx);
		gfieldXSlider.setMajorTickSpacing(50);
		gfieldXSlider.setMinorTickSpacing(10);
		gfieldXSlider.setPaintTicks(true);
		gfieldXSlider.setPaintLabels(true);

		gfieldYSlider = new JSlider();
		gfieldYSlider.addChangeListener(new GFieldYListener());
		gfieldYSlider.setMinimum(-100);
		gfieldYSlider.setMaximum(100);
		gfieldYSlider.setValue((int) force.gy);
		gfieldYSlider.setMajorTickSpacing(50);
		gfieldYSlider.setMinorTickSpacing(10);
		gfieldYSlider.setPaintTicks(true);
		gfieldYSlider.setPaintLabels(true);

		initComboBox = new JComboBox(initStrings);
		initComboBox.setSelectedIndex(0);
		initComboBox.addActionListener(new ComboBoxListener());
		JLabel initComboBoxLabel = new JLabel("Initial conditions");
		Box initBox = Box.createHorizontalBox();
		initBox.add(initComboBoxLabel);
		initBox.add(Box.createHorizontalGlue());
		initBox.add(initComboBox);

		algorithmComboBox = new JComboBox(solverString);
		algorithmComboBox.setSelectedIndex(0);
		algorithmComboBox.addActionListener(new AlgorithmListener());
		algorithmComboBox.setPreferredSize(new Dimension(algorithmComboBox.getPreferredSize().width, 5));
		JLabel algorithmLabel = new JLabel("Algorithm");
		Box algorithmBox = Box.createVerticalBox();
		algorithmBox.add(algorithmLabel);
		algorithmBox.add(algorithmComboBox);

		collisionComboBox = new JComboBox(collisionsString);
		collisionComboBox.setSelectedIndex(0);
		collisionComboBox.addActionListener(new Collisions());
		//collisionComboBox.setPreferredSize(new Dimension(collisionComboBox.getPreferredSize().width, 5));
		JLabel collisionsLabel = new JLabel("Collisions");

		collisionAlgorithm = new JComboBox(collisionalgorithmString);
		collisionAlgorithm.setSelectedIndex(0);
		collisionAlgorithm.addActionListener(new CollisionAlgorithm());
		JLabel colAlgorithmLabel = new JLabel("Algorithm for the collisions");

		Box collisionBox = Box.createVerticalBox();
		collisionBox.add(collisionsLabel);
		collisionBox.add(collisionComboBox);
		collisionBox.add(Box.createVerticalGlue());
		collisionBox.add(colAlgorithmLabel);
		collisionBox.add(collisionAlgorithm);
		collisionBox.add(Box.createVerticalStrut(170));

		startButton.addActionListener(new StartListener());
		stopButton.addActionListener(new StopListener());
		resetButton.addActionListener(new ResetListener());

		relativisticCheck = new JCheckBox("Relativistic Version");
		relativisticCheck.addItemListener(new RelativisticEffects());
		relativisticCheck.setEnabled(false);

		traceCheck = new JCheckBox("Trace");
		traceCheck.addItemListener(new CheckListener());

		currentgridCheck = new JCheckBox("Current");
		currentgridCheck.addItemListener(new DrawCurrentGridListener());
		currentgridCheck.setEnabled(false);

		drawFieldsCheck = new JCheckBox("Draw fields");
		drawFieldsCheck.addItemListener(new DrawFieldsListener());
		drawFieldsCheck.setEnabled(false);

		calculateFieldsCheck = new JCheckBox("Calculate Fields");
		calculateFieldsCheck.addItemListener(new CalculateFieldsListener());

		framerateCheck = new JCheckBox("Info");
		framerateCheck.addItemListener(new FrameListener());

		xboxentry = new JTextField(2);
		xboxentry.setText("10");
		xboxentry.addActionListener(new BoxDimension());

		yboxentry = new JTextField(2);
		yboxentry.setText("10");
		yboxentry.addActionListener(new BoxDimension());

		hardBoundaries = new JRadioButton("Hardwall");
		periodicBoundaries = new JRadioButton("Periodic");

		ButtonGroup bgroup = new ButtonGroup();

		bgroup.add(hardBoundaries);
		bgroup.add(periodicBoundaries);

		hardBoundaries.addActionListener(new SelectBoundaries());
		periodicBoundaries.addActionListener(new SelectBoundaries());

		JPanel boundaries = new JPanel();
		boundaries.add(hardBoundaries);
		boundaries.add(periodicBoundaries);
		JLabel boundariesLabel = new JLabel("Boundaries");

		JLabel xboxentryLabel = new JLabel("Cell width");
		JLabel yboxentryLabel = new JLabel("Cell height");

		JPanel controlPanelUp = new JPanel();
		controlPanelUp.setLayout(new FlowLayout());
		controlPanelUp.add(startButton);
		controlPanelUp.add(stopButton);
		controlPanelUp.add(resetButton);
		controlPanelUp.add(Box.createHorizontalStrut(25));
		controlPanelUp.add(initBox);
		controlPanelUp.add(Box.createHorizontalStrut(25));
		Box settingControls = Box.createVerticalBox();
		JPanel controlPanelDown = new JPanel();
		controlPanelDown.setLayout(new FlowLayout());
		settingControls.add(algorithmBox);
		settingControls.add(Box.createVerticalGlue());
		settingControls.add(relativisticCheck);
		settingControls.add(Box.createVerticalGlue());
		settingControls.add(speed);
		settingControls.add(Box.createVerticalGlue());
		settingControls.add(step);
		settingControls.add(Box.createVerticalGlue());
		settingControls.add(boundariesLabel);
		settingControls.add(boundaries);
		settingControls.add(traceCheck);
		settingControls.add(framerateCheck);

		Box panelBox = Box.createVerticalBox();
		panelBox.add(controlPanelUp);
		panelBox.add(controlPanelDown);

		JLabel eFieldXLabel = new JLabel("Electric Field in x - direction");
		JLabel eFieldYLabel = new JLabel("Electric Field in y - direction");
		JLabel bFieldZLabel = new JLabel("Magnetic Field in z - direction");
		JLabel gFieldXLabel = new JLabel("Gravitation in x - direction Field");
		JLabel gFieldYLabel = new JLabel("Gravitation in y - direction Field");

		// Change background color of tab from blue to system gray
		UIManager.put("TabbedPane.contentAreaColor", new Color(238, 238, 238));

		tabs = new JTabbedPane();

		Box fieldsBox = Box.createVerticalBox();
		fieldsBox.add(eFieldXLabel);
		fieldsBox.add(efieldXSlider);
		fieldsBox.add(Box.createVerticalStrut(5));
		fieldsBox.add(eFieldYLabel);
		fieldsBox.add(efieldYSlider);
		fieldsBox.add(Box.createVerticalGlue());
		fieldsBox.add(bFieldZLabel);
		fieldsBox.add(bfieldZSlider);
		fieldsBox.add(Box.createVerticalGlue());
		fieldsBox.add(gFieldXLabel);
		fieldsBox.add(gfieldXSlider);
		fieldsBox.add(Box.createVerticalStrut(5));
		fieldsBox.add(gFieldYLabel);
		fieldsBox.add(gfieldYSlider);
		fieldsBox.add(Box.createVerticalGlue());
		fieldsBox.add(dragLabel);
		fieldsBox.add(dragSlider);
		fieldsBox.add(Box.createVerticalGlue());

		Box xbox = Box.createHorizontalBox();
		Box ybox = Box.createHorizontalBox();
		xbox.add(Box.createHorizontalStrut(50));
		xbox.add(xboxentryLabel);
		xbox.add(Box.createHorizontalStrut(100));
		xbox.add(xboxentry);
		ybox.add(Box.createHorizontalStrut(50));
		ybox.add(yboxentryLabel);
		ybox.add(Box.createHorizontalStrut(96));
		ybox.add(yboxentry);

		Box cellSettings = Box.createVerticalBox();
		cellSettings.add(Box.createVerticalStrut(20));
		cellSettings.add(calculateFieldsCheck);
		cellSettings.add(Box.createVerticalGlue());
		cellSettings.add(currentgridCheck);
		cellSettings.add(Box.createVerticalGlue());
		cellSettings.add(drawFieldsCheck);
		cellSettings.add(Box.createVerticalStrut(20));
		cellSettings.add(xbox);
		cellSettings.add(Box.createVerticalStrut(10));
		cellSettings.add(ybox);
		cellSettings.add(Box.createVerticalStrut(200));

		fc = new JFileChooser();
		File workingDirectory = new File(System.getProperty("user.dir"));
		fc.setCurrentDirectory(workingDirectory);

		openButton = new JButton("Open...");
		openButton.addActionListener(new OpenButtonListener());
		saveButton = new JButton("Save...");
		saveButton.addActionListener(new SaveButtonListener());
		applyButton = new JButton("Apply");
		applyButton.addActionListener(new ApplyButtonListener());
		fileTextArea = new JTextArea();
		JScrollPane scrollpane = new JScrollPane(fileTextArea);

		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(openButton);
		buttonBox.add(saveButton);
		buttonBox.add(applyButton);

		Box fileTab = Box.createVerticalBox();
		fileTab.add(buttonBox);
		fileTab.add(scrollpane);

		fieldsBox.setPreferredSize(new Dimension(300, 100));
		settingControls.setPreferredSize(new Dimension (300, 100));
		collisionBox.setPreferredSize(new Dimension (300, 100));

		tabs.addTab("Fields", fieldsBox);
		tabs.addTab("Settings", settingControls);
		tabs.addTab("Coll.", collisionBox);
		tabs.addTab("Cell", cellSettings);
		tabs.addTab("File", fileTab);

		leftComponent = particlePanel;
		rightComponent = electricFieldPanel;
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftComponent, rightComponent);
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);

		this.setLayout(new BorderLayout());
		this.add(panelBox, BorderLayout.SOUTH);
		this.add(splitPane, BorderLayout.CENTER);
		this.add(tabs, BorderLayout.EAST);

		// start JSplitPane in collapsed state:
		electricFieldPanel.setSize(new Dimension());
		splitPane.setResizeWeight(1.0);

		particlePanel.addMouseListener(new PopupClickListener());
		phaseSpacePanel.addMouseListener(new PopupClickListener());
		electricFieldPanel.addMouseListener(new PopupClickListener());
	}

	Component leftComponent;
	Component rightComponent;

	JMenuItem itemParticle2DPanel;
	JMenuItem itemPhaseSpacePanel;
	JMenuItem itemElectricFieldPanel;

	class PopupMenu extends JPopupMenu {

		public PopupMenu() {
			itemParticle2DPanel = new JMenuItem("Particles");
			itemParticle2DPanel.addActionListener(new MenuSelected());
			add(itemParticle2DPanel);

			itemPhaseSpacePanel = new JMenuItem("Phase space");
			itemPhaseSpacePanel.addActionListener(new MenuSelected());
			add(itemPhaseSpacePanel);

			itemElectricFieldPanel = new JMenuItem("Electric field");
			itemElectricFieldPanel.addActionListener(new MenuSelected());
			add(itemElectricFieldPanel);
		}
	}

	Component clickComponent;

	class PopupClickListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger())
				doPop(e);
		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger())
				doPop(e);
		}

		private void doPop(MouseEvent e) {
			clickComponent = e.getComponent();
			PopupMenu menu = new PopupMenu();
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	class MenuSelected implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			// TODO: This method creates new instances of the panels
			// (which is nice so there can be two identical panels next
			// to each other), but it does not delete previous panels.
			// They should be unregistered in simulationAnimation if not
			// in use anymore.

			Component component = null;
			if (event.getSource() == itemParticle2DPanel) {
				particlePanel = new Particle2DPanel(simulationAnimation);
				particlePanel.addMouseListener(new PopupClickListener());
				component = particlePanel;
			} else if (event.getSource() == itemPhaseSpacePanel) {
				phaseSpacePanel = new PhaseSpacePanel(simulationAnimation);
				phaseSpacePanel.addMouseListener(new PopupClickListener());
				component = phaseSpacePanel;
			} else if (event.getSource() == itemElectricFieldPanel) {
				electricFieldPanel = new ElectricFieldPanel(simulationAnimation);
				electricFieldPanel.addMouseListener(new PopupClickListener());
				component = electricFieldPanel;
			}
			if (component != null) {
				int dividerLocation = splitPane.getDividerLocation();
				if (clickComponent == leftComponent) {
					splitPane.setLeftComponent(component);
					leftComponent = component;
				} else if (clickComponent == rightComponent) {
					splitPane.setRightComponent(component);
					rightComponent = component;
				}
				splitPane.setDividerLocation(dividerLocation);
			}
		}

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

		stepSlider.setValue((int)(s.tstep / stepSliderScaling));
		efieldXSlider.setValue((int) (force.ex / exSliderScaling));
		efieldYSlider.setValue((int) (force.ey / eySliderScaling));
		bfieldZSlider.setValue((int) (force.bz / bzSliderScaling));
		gfieldXSlider.setValue((int) (force.gx / gxSliderScaling));
		gfieldYSlider.setValue((int) (force.gy / gySliderScaling));
		dragSlider.setValue((int) (force.drag / dragSliderScaling));
		//int delay = particlePanel.timer.getDelay();
		//speedSlider.setValue((int) (-Math.log(delay / 1000.) / speedSliderScaling));
		speedSlider.setValue(50);
		timer.setDelay((int) (1000 * Math.exp(-50 * speedSliderScaling)));
		xboxentry.setText("10");
		yboxentry.setText("10");
		if(s.getParticleMover().getBoundaryType() == ParticleBoundaryType.Hardwall) {
			hardBoundaries.setSelected(true);
			periodicBoundaries.setSelected(false);
		}
		else if(s.getParticleMover().getBoundaryType() == ParticleBoundaryType.Periodic) {
			hardBoundaries.setSelected(false);
			periodicBoundaries.setSelected(true);
		}
		
		//ordering of these two is important!
		collisionComboBox.setSelectedIndex(0);
		collisionAlgorithm.setSelectedIndex(0);

		// Set algorithm UI according to current setting
		Solver solver = s.getParticleMover().getSolver();
		if (solver instanceof Boris) {
			algorithmComboBox.setSelectedIndex(4);
			relativisticCheck.setSelected(false);
		} else if (solver instanceof BorisRelativistic) {
			algorithmComboBox.setSelectedIndex(4);
			relativisticCheck.setSelected(true);
		} else if (solver instanceof EulerRichardson) {
			algorithmComboBox.setSelectedIndex(0);
			relativisticCheck.setSelected(false);
		}
		// TODO: Implement this for other solvers.
		// (Currently only implemented for solvers used in InitialConditions.)

		linkConstantForce();
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
