package org.openpixi.pixi.ui.panel.properties;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ScaleProperties {

	private double scaleFactor = 1;
	private double automaticScaleFactor = 1;
	private boolean automaticScaling = false;
	private double currentMaxValue = 0;

	private JLabel label;
	private JTextField textField;

	public double getScaleFactor() {
		return scaleFactor;
	}

	public boolean getAutomaticScaling() {
		return automaticScaling;
	}

	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	public void setAutomaticScaling(boolean automaticScaling) {
		this.automaticScaling = automaticScaling;
	}

	public double getScale() {
		if (automaticScaling) {
			return automaticScaleFactor;
		} else {
			return scaleFactor;
		}
	}

	public void resetAutomaticScale() {
		currentMaxValue = 0;
	}

	public void putValue(double value) {
		if (automaticScaling) {
			double abs = Math.abs(value);
			if (abs > currentMaxValue) {
				currentMaxValue = abs;
			}
		}
	}

	public void calculateAutomaticScale(double maxsize) {
		if (automaticScaling && currentMaxValue > 0) {
			automaticScaleFactor = maxsize / currentMaxValue;
			if (textField != null) {
				textField.setText(Double.toString(automaticScaleFactor));
			}
		}
	}

	public void addComponents(Box box) {
		Box settingControls = Box.createVerticalBox();

		JCheckBox scaleCheck;
		scaleCheck = new JCheckBox("Automatic scaling");
		scaleCheck.addItemListener(new CheckListener());
		scaleCheck.setSelected(automaticScaling);

		label = new JLabel("Scale:", SwingConstants.CENTER);

		textField = new JTextField();
		textField.setText(Double.toString(scaleFactor));
		textField.addActionListener(new TextFieldListener());
		//textField.setPreferredSize(new Dimension(10,10));
		textField.setMaximumSize(
				new Dimension(400/*Integer.MAX_VALUE*/, textField.getPreferredSize().height));

		settingControls.add(scaleCheck);
		settingControls.add(Box.createVerticalGlue());
		settingControls.add(label);
		settingControls.add(textField);
		settingControls.add(Box.createVerticalGlue());

		updateTextFieldStatus();

		box.add(settingControls);
	}

	void updateTextFieldStatus() {
		if (textField != null) {
			textField.setEnabled(!automaticScaling);
			label.setEnabled(!automaticScaling);
			if (automaticScaling) {
				textField.setText(Double.toString(automaticScaleFactor));
			} else {
				textField.setText(Double.toString(scaleFactor));
			}
		}
	}

	class CheckListener implements ItemListener {
		public void itemStateChanged(ItemEvent event){
			ScaleProperties.this.automaticScaling =
					(event.getStateChange() == ItemEvent.SELECTED);
			ScaleProperties.this.updateTextFieldStatus();
		}
	}

	class TextFieldListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			scaleFactor = Double.parseDouble(textField.getText());
		}
	}
}
