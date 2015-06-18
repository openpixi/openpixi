package org.openpixi.pixi.ui.panel.properties;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JCheckBox;

public class GaugeProperties {

	private boolean coulombGauge = false;

	public boolean isCoulombGauge() {
		return coulombGauge;
	}

	public void setCoulombGauge(boolean coulombGauge) {
		this.coulombGauge = coulombGauge;
	}

	public void addComponents(Box box) {

		JCheckBox currentgridCheck;
		currentgridCheck = new JCheckBox("Coulomb gauge");
		currentgridCheck.addItemListener(new DrawCurrentListener());
		currentgridCheck.setSelected(coulombGauge);

		Box cellSettings = Box.createVerticalBox();
		cellSettings.add(currentgridCheck);
		cellSettings.add(Box.createVerticalGlue());

		box.add(cellSettings);
	}

	class DrawCurrentListener implements ItemListener {
		public void itemStateChanged(ItemEvent event){
			GaugeProperties.this.coulombGauge = 
					(event.getStateChange() == ItemEvent.SELECTED);
		}
	}
}
