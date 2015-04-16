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
import org.openpixi.pixi.physics.movement.boundary.ParticleBoundaryType;
import org.openpixi.pixi.physics.solver.*;
import org.openpixi.pixi.physics.solver.relativistic.*;
import org.openpixi.pixi.ui.panel.AnimationPanel;
import org.openpixi.pixi.ui.panel.Particle2DPanel;
import org.openpixi.pixi.ui.panel.Particle3DPanel;
import org.openpixi.pixi.ui.panel.PhaseSpacePanel;
import org.openpixi.pixi.ui.panel.ElectricFieldPanel;
import org.openpixi.pixi.ui.tab.FileTab;

/**
 * Displays the animation of particles.
 */
public class MainControlApplet extends JApplet
{
	private JButton startButton;
	private JButton stopButton;
	private JButton resetButton;

	private JSlider speedSlider;
	private JSlider stepSlider;

	private JCheckBox framerateCheck;
	private JCheckBox currentgridCheck;
	private JCheckBox drawFieldsCheck;
	private JCheckBox calculateFieldsCheck;
	private JCheckBox relativisticCheck;

	private JTextField xboxentry;
	private JTextField yboxentry;
	private JTextField zboxentry;

	private JComboBox initComboBox;
	private JComboBox algorithmComboBox;
	private JCheckBox traceCheck;
	private JRadioButton hardBoundaries;
	private JRadioButton periodicBoundaries;

	private JTabbedPane tabs;
	private JSplitPane splitPane;

	private SimulationAnimation simulationAnimation;

	private Particle2DPanel particlePanel;
	private Particle3DPanel particle3DPanel;
	private PhaseSpacePanel phaseSpacePanel;
	private ElectricFieldPanel electricFieldPanel;

	private static final double speedSliderScaling = 0.07;
	private static final double stepSliderScaling = 0.01;

	String[] initStrings = {
			"10 random particles",
			"100 random particles",
			"1000 random particles",
			"10000 random particles",
			"Single particle in gravity",
			"Single particle in el. Field",
			"3 part. in magnetic field",
            "Pair of particles",
            "Two stream instability",
            "Weibel instability",
            "One particle test",
            "Wave propagation test",
            "Two particles in 3D",
            "Two stream instability in 3D",
            "Weibel instability in 3D"};

	String[] solverString = {
			"Euler Richardson",
			"LeapFrog",
			"LeapFrog Damped",
			"LeapFrog Half Step",
			"Boris",
			"Boris Damped",
			"Semi Implicit Euler",
			"Euler"};

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
			int i = algorithmComboBox.getSelectedIndex();
			simulationAnimation.relativisticEffects(i);
		}
	}

	class DrawCurrentGridListener implements ItemListener {
		public void itemStateChanged(ItemEvent eve){
			if (particlePanel != null) {
				particlePanel.drawCurrentGrid();
			}
			if (particle3DPanel != null) {
				particle3DPanel.drawCurrentGrid();
			}
		}
	}

	class DrawFieldsListener implements ItemListener {
		public void itemStateChanged(ItemEvent eve){
			if (particlePanel != null) {
				particlePanel.drawFields();
			}
			if (particle3DPanel != null) {
				particle3DPanel.drawFields();
			}
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
			int zbox = Integer.parseInt(zboxentry.getText());
			double width = s.getWidth();
			double height = s.getHeight();
			double depth = s.getDepth();
			s.grid.changeSize(xbox, ybox, zbox, width, height, depth);
		}
	}

	/**
	 * Constructor.
	 */
	public MainControlApplet() {
		Debug.checkAssertsEnabled();

		simulationAnimation = new SimulationAnimation();
		particlePanel = new Particle2DPanel(simulationAnimation);
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
		
		zboxentry = new JTextField(2);
		zboxentry.setText("10");
		zboxentry.addActionListener(new BoxDimension());

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
		JLabel zboxentryLabel = new JLabel("Cell depth");

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

		// Change background color of tab from blue to system gray
		UIManager.put("TabbedPane.contentAreaColor", new Color(238, 238, 238));

		tabs = new JTabbedPane();

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

		FileTab fileTab = new FileTab(MainControlApplet.this, simulationAnimation);

		settingControls.setPreferredSize(new Dimension (300, 100));

		tabs.addTab("Settings", settingControls);
		tabs.addTab("Cell", cellSettings);
		tabs.addTab("File", fileTab);

		this.setLayout(new BorderLayout());
		this.add(panelBox, BorderLayout.SOUTH);
		this.add(particlePanel, BorderLayout.CENTER);
		this.add(tabs, BorderLayout.EAST);

		popupClickListener = new PopupClickListener();
		particlePanel.addMouseListener(popupClickListener);
	}

	PopupClickListener popupClickListener;

	JMenuItem itemSplitHorizontally;
	JMenuItem itemSplitVertically;
	JMenuItem itemClosePanel;
	JMenuItem itemParticle2DPanel;
	JMenuItem itemParticle3DPanel;
	JMenuItem itemPhaseSpacePanel;
	JMenuItem itemElectricFieldPanel;

	class PopupMenu extends JPopupMenu {

		public PopupMenu() {
			itemSplitHorizontally = new JMenuItem("Split horizontally");
			itemSplitHorizontally.addActionListener(new MenuSelected());
			add(itemSplitHorizontally);

			itemSplitVertically = new JMenuItem("Split vertically");
			itemSplitVertically.addActionListener(new MenuSelected());
			add(itemSplitVertically);

			if (clickComponent != null && clickComponent.getParent() instanceof JSplitPane) {
				itemClosePanel = new JMenuItem("Close panel");
				itemClosePanel.addActionListener(new MenuSelected());
				add(itemClosePanel);
			}

			add(new JSeparator());

			itemParticle2DPanel = new JMenuItem("Particles");
			itemParticle2DPanel.addActionListener(new MenuSelected());
			add(itemParticle2DPanel);

			itemParticle3DPanel = new JMenuItem("Particles 3D");
			itemParticle3DPanel.addActionListener(new MenuSelected());
			add(itemParticle3DPanel);

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

			if (event.getSource() == itemSplitHorizontally) {
				splitPanel(JSplitPane.HORIZONTAL_SPLIT);
			} else if (event.getSource() == itemSplitVertically) {
				splitPanel(JSplitPane.VERTICAL_SPLIT);
			} else if (event.getSource() == itemClosePanel) {
				closePanel();
			} else if (event.getSource() == itemParticle2DPanel) {
				particlePanel = new Particle2DPanel(simulationAnimation);
				component = particlePanel;
			} else if (event.getSource() == itemParticle3DPanel) {
				particle3DPanel = new Particle3DPanel(simulationAnimation);
				component = particle3DPanel;
			} else if (event.getSource() == itemPhaseSpacePanel) {
				phaseSpacePanel = new PhaseSpacePanel(simulationAnimation);
				component = phaseSpacePanel;
			} else if (event.getSource() == itemElectricFieldPanel) {
				electricFieldPanel = new ElectricFieldPanel(simulationAnimation);
				component = electricFieldPanel;
			}
			if (component != null) {
				replacePanel(component);
			}
		}

		private void replacePanel(Component component) {
			component.addMouseListener(popupClickListener);
			Component parent = clickComponent.getParent();
			if (parent != null) {
				if (parent instanceof JSplitPane) {
					JSplitPane parentsplitpane = (JSplitPane) parent;
					Component parentleft = parentsplitpane.getLeftComponent();

					int dividerLocation = parentsplitpane.getDividerLocation();
					if (parentleft == clickComponent) {
						parentsplitpane.setLeftComponent(component);
					} else {
						parentsplitpane.setRightComponent(component);
					}
					parentsplitpane.setDividerLocation(dividerLocation);
				} else if (parent instanceof JPanel) {
					// top level
					MainControlApplet.this.remove(clickComponent);
					MainControlApplet.this.add(component, BorderLayout.CENTER);
					MainControlApplet.this.validate();
				}
			}
		}

		/**
		 * Split current panel either horizontally or vertically
		 *
		 * @param orientation
		 *            Either JSplitPane.HORIZONTAL_SPLIT or
		 *            JSplitPane.VERTICAL_SPLIT.
		 */
		private void splitPanel(int orientation) {
			Component parent = clickComponent.getParent();

			particlePanel = new Particle2DPanel(simulationAnimation);
			Component newcomponent = particlePanel;
			newcomponent.addMouseListener(popupClickListener);

			if (parent != null) {
				if (parent instanceof JSplitPane) {
					JSplitPane parentsplitpane = (JSplitPane) parent;
					Component parentleft = parentsplitpane.getLeftComponent();

					int dividerLocation = parentsplitpane.getDividerLocation();

					JSplitPane s = new JSplitPane(orientation,
								clickComponent, newcomponent);
					s.setOneTouchExpandable(true);
					s.setContinuousLayout(true);
					s.setResizeWeight(0.5);

					if (parentleft == clickComponent) {
						parentsplitpane.setLeftComponent(s);
					} else {
						parentsplitpane.setRightComponent(s);
					}
					parentsplitpane.setDividerLocation(dividerLocation);
				} else if (parent instanceof JPanel) {
					// top level
					JSplitPane s = new JSplitPane(orientation,
							clickComponent, newcomponent);
					s.setOneTouchExpandable(true);
					s.setContinuousLayout(true);
					s.setResizeWeight(0.5);

					MainControlApplet.this.remove(clickComponent);
					MainControlApplet.this.add(s, BorderLayout.CENTER);
					MainControlApplet.this.validate();
				}
			}
		}

		private void closePanel() {
			Component parent = clickComponent.getParent();
			if (parent != null) {
				if (parent instanceof JSplitPane) {
					JSplitPane parentsplitpane = (JSplitPane) parent;
					Component parentleft = parentsplitpane.getLeftComponent();
					Component parentright = parentsplitpane.getRightComponent();
					Component grandparent = parent.getParent();

					Component othercomponent = parentleft;
					if (parentleft == clickComponent) {
						othercomponent = parentright;
					}

					if (grandparent != null) {
						if (grandparent instanceof JSplitPane) {
							JSplitPane grandparentsplitpane = (JSplitPane) grandparent;
							Component left = grandparentsplitpane.getLeftComponent();
							if (left == parentsplitpane) {
								grandparentsplitpane.setLeftComponent(othercomponent);
							} else {
								grandparentsplitpane.setRightComponent(othercomponent);
							}
						} else if (grandparent instanceof JPanel) {
							parentsplitpane.removeAll();
							MainControlApplet.this.remove(parentsplitpane);
							MainControlApplet.this.add(othercomponent, BorderLayout.CENTER);
							MainControlApplet.this.validate();
						}
						clickComponent.removeMouseListener(popupClickListener);
						if (clickComponent instanceof AnimationPanel) {
							((AnimationPanel) clickComponent).destruct();
						}
					}
				}
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
		speedSlider.setValue(50);
		timer.setDelay((int) (1000 * Math.exp(-50 * speedSliderScaling)));
		xboxentry.setText("10");
		yboxentry.setText("10");
		zboxentry.setText("10");
		if(s.getParticleMover().getBoundaryType() == ParticleBoundaryType.Hardwall) {
			hardBoundaries.setSelected(true);
			periodicBoundaries.setSelected(false);
		}
		else if(s.getParticleMover().getBoundaryType() == ParticleBoundaryType.Periodic) {
			hardBoundaries.setSelected(false);
			periodicBoundaries.setSelected(true);
		}


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
