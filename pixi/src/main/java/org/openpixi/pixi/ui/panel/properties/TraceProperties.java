package org.openpixi.pixi.ui.panel.properties;

import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JCheckBox;

public class TraceProperties {

	/** A state for the trace */
	public boolean paint_trace = false;

	private boolean reset_trace;

	public void clear() {
		reset_trace = true;
	}

	public boolean getPaintTrace() {
		return paint_trace;
	}

	public void addComponents(Box box) {
		Box settingControls = Box.createVerticalBox();

		JCheckBox traceCheck;
		traceCheck = new JCheckBox("Trace");
		traceCheck.addItemListener(new CheckListener());
		traceCheck.setSelected(paint_trace);

		settingControls.add(Box.createVerticalGlue());
		settingControls.add(traceCheck);
		settingControls.add(Box.createVerticalStrut(20));

		box.add(settingControls);
	}

	public boolean getCallSuper() {
		boolean callsuper = false;
		if(!paint_trace)
		{
			callsuper = true;
		}
		if(reset_trace)
		{
			callsuper = true;
			reset_trace = false;
		}
		return callsuper;
	}

	class CheckListener implements ItemListener {
		public void itemStateChanged(ItemEvent event){
			TraceProperties.this.paint_trace = 
					(event.getStateChange() == ItemEvent.SELECTED);
		}
	}

}
