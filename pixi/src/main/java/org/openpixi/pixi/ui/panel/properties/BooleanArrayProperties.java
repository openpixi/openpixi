package org.openpixi.pixi.ui.panel.properties;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A generic list of checkboxes for setting boolean array values.
 */
public class BooleanArrayProperties {

	private boolean[] valueList;
	private String[] nameList;
	private JCheckBox[] boolCheckList;

	public BooleanArrayProperties(String[] nameList, boolean[] initialValueList)
	{
		this.nameList = nameList;
		boolCheckList = new JCheckBox[this.nameList.length];
		valueList = new boolean[this.nameList.length];
		for (int i = 0; i < this.nameList.length; i++) {
			boolCheckList[i] = new JCheckBox(nameList[i]);
			this.setValue(i, initialValueList[i]);
		}
	}

	public void addComponents(Box box)
	{
		Box settingControls = Box.createVerticalBox();

		for (int i = 0; i < nameList.length; i++) {
			boolCheckList[i].addItemListener(new CheckListener(i));
			boolCheckList[i].setSelected(valueList[i]);
			settingControls.add(boolCheckList[i]);
		}
		settingControls.add(Box.createVerticalGlue());

		box.add(settingControls);
	}

	public int getSize() {
		return nameList.length;
	}

	public boolean getValue(int index)
	{
		return valueList[index];
	}

	public void setValue(int index, boolean value)
	{
		this.valueList[index] = value;
		boolCheckList[index].setSelected(value);
	}

	/**
	 * Return string array for values that are set.
	 * @return
	 */
	public String[] getStringArrayFromValues() {
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < nameList.length; i++) {
			if (valueList[i]) {
				result.add(nameList[i]);
			}
		}
		return result.toArray(new String[0]);
	}

	/**
	 * Set values according to string array provided.
	 * @param names
	 */
	public void setValuesFromStringArray(String[] names) {
		List<String> list = Arrays.asList(names);
		for (int i = 0; i < nameList.length; i++) {
			setValue(i, list.contains(nameList[i]));
		}
	}

	class CheckListener implements ItemListener {
		private int index;

		public CheckListener(int index) {
			this.index = index;
		}

		public void itemStateChanged(ItemEvent event){
			BooleanArrayProperties.this.valueList[index] =
					(event.getStateChange() == ItemEvent.SELECTED);
		}
	}
}

