package org.openpixi.pixi.ui.panel.properties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A generic text field for setting integer values.
 */
public class IntegerProperties {

	private int value;
	private String name;

	private JLabel label;
	private JTextField textField;

	public IntegerProperties(String name, int initialValue)
	{
		this.name = name;
		textField = new JTextField();
		this.setValue(initialValue);
	}

	public void addComponents(Box box)
	{
		Box settingControls = Box.createVerticalBox();

		label = new JLabel(name, SwingConstants.CENTER);

		textField.setText(Integer.toString(value));
		textField.addActionListener(new TextFieldListener());
		textField.setMaximumSize(
				new Dimension(400/*Integer.MAX_VALUE*/, textField.getPreferredSize().height));


		settingControls.add(label);
		settingControls.add(textField);
		settingControls.add(Box.createVerticalGlue());

		box.add(settingControls);
	}

	public int getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		this.value = value;
		textField.setText(Integer.toString(value));
	}

	class TextFieldListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			value = Integer.parseInt(textField.getText());
		}
	}
}
