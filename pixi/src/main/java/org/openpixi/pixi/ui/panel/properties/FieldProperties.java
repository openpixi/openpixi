package org.openpixi.pixi.ui.panel.properties;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JCheckBox;

public class FieldProperties {

	private boolean drawCurrentGrid = false;

	private boolean drawFields = false;

	public boolean getDrawCurrentGrid() {
		return drawCurrentGrid;
	}

	public boolean getDrawFields() {
		return drawFields;
	}


	public void addComponents(Box box) {

		JCheckBox currentgridCheck;
		currentgridCheck = new JCheckBox("Current");
		currentgridCheck.addItemListener(new DrawCurrentGridListener());
		currentgridCheck.setSelected(drawCurrentGrid);

		JCheckBox drawFieldsCheck;
		drawFieldsCheck = new JCheckBox("Draw fields");
		drawFieldsCheck.addItemListener(new DrawFieldsListener());
		drawFieldsCheck.setSelected(drawFields);

		Box cellSettings = Box.createVerticalBox();
		cellSettings.add(Box.createVerticalStrut(20));
		cellSettings.add(currentgridCheck);
		cellSettings.add(Box.createVerticalGlue());
		cellSettings.add(drawFieldsCheck);
		cellSettings.add(Box.createVerticalStrut(200));

		box.add(cellSettings);
	}

	class DrawCurrentGridListener implements ItemListener {
		public void itemStateChanged(ItemEvent event){
			FieldProperties.this.drawCurrentGrid = 
					(event.getStateChange() == ItemEvent.SELECTED);
		}
	}

	class DrawFieldsListener implements ItemListener {
		public void itemStateChanged(ItemEvent event){
			FieldProperties.this.drawFields = 
					(event.getStateChange() == ItemEvent.SELECTED);
		}
	}
}
