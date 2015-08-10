package org.openpixi.pixi.ui.panel.properties;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.openpixi.pixi.ui.SimulationAnimation;

/**
 * A generic class for setting ComboBox properties.
 *
 */
public class ComboBoxProperties {

	private SimulationAnimation simulationAnimation;
	private JComboBox myComboBox;
	private int index = 0;
	private String label;
	private String[] entries = {};

	public ComboBoxProperties(SimulationAnimation simulationAnimation, String label, String[] entries, int index) {
		this.simulationAnimation = simulationAnimation;
		this.label = label;
		this.entries = entries;
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int id)
	{
		index = id;
		if (myComboBox != null) {
			myComboBox.setSelectedIndex(index);
		}
	}

	public void addComponents(Box box) {

		Box settingControls = Box.createVerticalBox();

		myComboBox = new JComboBox(entries);
		myComboBox.addActionListener(new MyListener());
		myComboBox.setSelectedIndex(index);
		myComboBox.setPreferredSize(new Dimension(myComboBox.getPreferredSize().width, 5));
		JLabel colorLabel = new JLabel(label);
		Box colorBox = Box.createVerticalBox();
		colorBox.add(colorLabel);
		colorBox.add(myComboBox);

		settingControls.add(colorBox);
		settingControls.add(Box.createVerticalGlue());

		box.add(settingControls);
	}

	/**
	 * Return string from currently selected entry.
	 * @return
	 */
	public String getStringFromEntry() {
		return entries[index];
	}

	/**
	 * Set values according to string array provided.
	 * @param names
	 */
	public void setEntryFromString(String entry) {
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].equals(entry)) {
				setIndex(i);
				break;
			}
		}
	}

	class MyListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			JComboBox cbox = (JComboBox) eve.getSource();
			int id = cbox.getSelectedIndex();
			index = id;
			simulationAnimation.repaint();
		}
	}

}
