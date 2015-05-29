package org.openpixi.pixi.ui.panel.properties;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * Common properties in connection with color fields
 *
 */
public class ColorProperties {

	private int colorIndex = 0;

	private int directionIndex = 1;

	String[] colorString = {
			"1",
			"2",
			"3"};

	String[] directionString = {
			"x",
			"y",
			"z"};

	public int getColorIndex() {
		return colorIndex;
	}

	public int getDirectionIndex() {
		return directionIndex;
	}

	public void colorIndexSet(int id)
	{
		this.colorIndex = id;
	}

	public void directionSet(int id)
	{
		this.directionIndex = id;
	}

	public void addComponents(Box box) {

		Box settingControls = Box.createVerticalBox();

		JComboBox colorIndexComboBox;
		colorIndexComboBox = new JComboBox(colorString);
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
		}
	}

	class DirectionListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			JComboBox cbox = (JComboBox) eve.getSource();
			int id = cbox.getSelectedIndex();
			ColorProperties.this.directionIndex = id;
		}
	}
}
