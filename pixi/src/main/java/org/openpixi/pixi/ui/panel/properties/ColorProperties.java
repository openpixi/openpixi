package org.openpixi.pixi.ui.panel.properties;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.openpixi.pixi.ui.SimulationAnimation;

/**
 * Common properties in connection with color fields
 *
 */
public class ColorProperties {

	private SimulationAnimation simulationAnimation;
	private int colorIndex = 0;
	private int directionIndex = 1;
	JComboBox colorIndexComboBox;

	String[] colorString;

	String[] directionString = {
			"x",
			"y",
			"z"};

	public ColorProperties(SimulationAnimation simulationAnimation) {
		this.simulationAnimation = simulationAnimation;
		updateColorEntries();
	}

	/**
	 * Updates the entries for the color selection.
	 */
	private void updateColorEntries() {
		int colors = simulationAnimation.getSimulation().getNumberOfColors();
		int generators = colors * colors - 1; // Number of SU(N) generators
		colorString = new String[generators];
		for (int i = 0; i < generators; i++) {
			colorString[i] = "" + (i+1);
		}

		if (colorIndexComboBox == null) {
			colorIndexComboBox = new JComboBox(colorString);
		} else {
			colorIndexComboBox.removeAllItems();
			for (int i = 0; i < generators; i++) {
				colorIndexComboBox.addItem(colorString[i]);
			}
		}

		if (colorIndex > generators) {
			colorIndex = generators - 1;
		}
		colorIndexComboBox.setSelectedIndex(colorIndex);
	}

	/**
	 * Checks whether the selectable number of colors coincides with the number of colors
	 * of the simulation. If not, update the number of selectable colors.
	 */
	public void checkConsistency() {
		int colors = simulationAnimation.getSimulation().getNumberOfColors();
		int generators = colors * colors - 1; // Number of SU(N) generators
		if (generators != colorString.length) {
			updateColorEntries();
		}
	}

	public int getColorIndex() {
		return colorIndex;
	}

	public int getDirectionIndex() {
		return directionIndex;
	}

	public void setColorIndex(int id)
	{
		this.colorIndex = id;
	}

	public void setDirectionIndex(int id)
	{
		this.directionIndex = id;
	}

	public void addComponents(Box box) {

		Box settingControls = Box.createVerticalBox();

		colorIndexComboBox.addActionListener(new ColorListener());
		colorIndexComboBox.setSelectedIndex(colorIndex);
		colorIndexComboBox.setPreferredSize(new Dimension(colorIndexComboBox.getPreferredSize().width, 5));
		JLabel colorLabel = new JLabel("Color index");
		Box colorBox = Box.createVerticalBox();
		colorBox.add(colorLabel);
		colorBox.add(colorIndexComboBox);

		JComboBox directionIndexComboBox;
		directionIndexComboBox = new JComboBox(directionString);
		directionIndexComboBox.addActionListener(new DirectionListener());
		directionIndexComboBox.setSelectedIndex(directionIndex);
		directionIndexComboBox.setPreferredSize(new Dimension(directionIndexComboBox.getPreferredSize().width, 5));
		JLabel directionLabel = new JLabel("Field direction");
		Box directionBox = Box.createVerticalBox();
		directionBox.add(directionLabel);
		directionBox.add(directionIndexComboBox);

		settingControls.add(colorBox);
		settingControls.add(Box.createVerticalGlue());
		settingControls.add(directionBox);
		settingControls.add(Box.createVerticalGlue());

		box.add(settingControls);
	}

	class ColorListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			JComboBox cbox = (JComboBox) eve.getSource();
			int id = cbox.getSelectedIndex();
			ColorProperties.this.colorIndex = id;
			simulationAnimation.repaint();
		}
	}

	class DirectionListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			JComboBox cbox = (JComboBox) eve.getSource();
			int id = cbox.getSelectedIndex();
			ColorProperties.this.directionIndex = id;
			simulationAnimation.repaint();
		}
	}
}
