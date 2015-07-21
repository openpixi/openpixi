package org.openpixi.pixi.ui.panel.properties;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JCheckBox;

import org.openpixi.pixi.ui.SimulationAnimation;

public class FieldProperties {

	private SimulationAnimation simulationAnimation;
	private boolean drawCurrent = false;
	private boolean drawFields = false;

	public FieldProperties(SimulationAnimation simulationAnimation) {
		this.simulationAnimation = simulationAnimation;
	}

	public boolean isDrawCurrent() {
		return drawCurrent;
	}

	public boolean isDrawFields() {
		return drawFields;
	}

	public void setDrawCurrent(boolean drawCurrent) {
		this.drawCurrent = drawCurrent;
	}

	public void setDrawFields(boolean drawFields) {
		this.drawFields = drawFields;
	}

	public void addComponents(Box box) {

		JCheckBox currentgridCheck;
		currentgridCheck = new JCheckBox("Current");
		currentgridCheck.addItemListener(new DrawCurrentListener());
		currentgridCheck.setSelected(drawCurrent);

		JCheckBox drawFieldsCheck;
		drawFieldsCheck = new JCheckBox("Draw fields");
		drawFieldsCheck.addItemListener(new DrawFieldsListener());
		drawFieldsCheck.setSelected(drawFields);

		Box cellSettings = Box.createVerticalBox();
		cellSettings.add(currentgridCheck);
		cellSettings.add(Box.createVerticalGlue());
		cellSettings.add(drawFieldsCheck);
		cellSettings.add(Box.createVerticalGlue());

		box.add(cellSettings);
	}

	class DrawCurrentListener implements ItemListener {
		public void itemStateChanged(ItemEvent event){
			FieldProperties.this.drawCurrent = 
					(event.getStateChange() == ItemEvent.SELECTED);
			simulationAnimation.repaint();
		}
	}

	class DrawFieldsListener implements ItemListener {
		public void itemStateChanged(ItemEvent event){
			FieldProperties.this.drawFields = 
					(event.getStateChange() == ItemEvent.SELECTED);
			simulationAnimation.repaint();
		}
	}
}
