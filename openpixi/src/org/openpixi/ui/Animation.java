package org.openpixi.ui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import javax.swing.event.*;

/**
 * Displays the animation of particles.
 */
public class Animation extends JApplet {

	private JButton startButton, stopButton, resetButton;
	private JSlider speedSlider;
	private JSlider efieldSlider;
	private JSlider bfieldSlider;
	private JSlider gfieldSlider;
	private JComboBox initComboBox;
	private JCheckBox traceCheck;
	private Particle2DPanel particlePanel;

	String[] initStrings = {
			"10 random particles",
			"100 random particles",
			"1000 random particles",
			"10000 random particles",
			"Single particle in gravity",
			"Three particles in magnetic field" };

	/**
	 * Listener for slider.
	 */
	class SliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if (!source.getValueIsAdjusting()) {
				int frames = (int) source.getValue();
				if (frames == 0) {
					if (!particlePanel.sl)
						particlePanel.stopAnimation();
				} else {
					int delay = (int) source.getMaximum()
							- (int) source.getValue();
					particlePanel.timer.stop();
					particlePanel.timer.setDelay(delay);
					particlePanel.timer.setInitialDelay(10 * delay);
					particlePanel.timer.start();
				}
			}
		}
	}
	
	class ComboBoxListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox) e.getSource();
			int id  = cb.getSelectedIndex();
			particlePanel.resetAnimation(id);
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
		}
	}
	
	class CheckListener implements ItemListener {
		public void itemStateChanged(ItemEvent eve){
			//AbstractButton abstractbutton = (AbstractButton) eve.getSource();
//			int state = eve.getStateChange();
	//		if(state == ItemEvent.SELECTED)
				particlePanel.checkTrace();
		//	if(state == ItemEvent.DESELECTED)
			//	particlePanel.checkTrace();
		}
		
	}

	/**
	 * Constructor.
	 */
	public Animation() {
		particlePanel = new Particle2DPanel();

		this.setVisible(true);
		this.setSize(700, 500);

		startButton = new JButton("start");
		stopButton = new JButton("stop");
		resetButton = new JButton("reset");

		speedSlider = new JSlider();
		speedSlider.addChangeListener(new SliderListener());
		speedSlider.setMinimum(0);
		speedSlider.setMaximum(50);
		speedSlider.setValue(30);
		
		efieldSlider = new JSlider();
		
		bfieldSlider = new JSlider();
		
		gfieldSlider = new JSlider();
		
		initComboBox = new JComboBox(initStrings);
		initComboBox.setSelectedIndex(0);
		initComboBox.addActionListener(new ComboBoxListener());
		
		startButton.addActionListener(new StartListener());
		stopButton.addActionListener(new StopListener());
		resetButton.addActionListener(new ResetListener());
		
		traceCheck = new JCheckBox("Trace");
		traceCheck.addItemListener(new CheckListener());

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		controlPanel.add(startButton);
		controlPanel.add(stopButton);
		controlPanel.add(resetButton);
		controlPanel.add(initComboBox);
		controlPanel.add(speedSlider);
		controlPanel.add(traceCheck);
		
		JLabel eFieldLabel = new JLabel("E Field", JLabel.CENTER);
		JLabel bFieldLabel = new JLabel("B Field", JLabel.CENTER);
		JLabel gFieldLabel = new JLabel("G Field", JLabel.CENTER);
		
		JPanel fieldsPanel = new JPanel();
		fieldsPanel.setLayout(new FlowLayout());
		fieldsPanel.add(efieldSlider);
		fieldsPanel.add(eFieldLabel);
		fieldsPanel.add(bfieldSlider);
		fieldsPanel.add(bFieldLabel);
		fieldsPanel.add(gfieldSlider);
		fieldsPanel.add(gFieldLabel);
		

		this.setLayout(new BorderLayout());
		this.add(controlPanel, BorderLayout.SOUTH);
		this.add(particlePanel, BorderLayout.CENTER);
		this.add(fieldsPanel, BorderLayout.NORTH);

	}

	/**
	 * Entry point for java application.
	 */
	public static void main(String[] args) {

		JFrame web = new JFrame();

		web.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		web.setTitle("Animation");
		web.setContentPane(new Animation());

		web.pack();
		web.setVisible(true);
		web.setSize(700, 500);
	}

}
