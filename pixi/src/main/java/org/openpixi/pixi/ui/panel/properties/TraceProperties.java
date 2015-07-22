package org.openpixi.pixi.ui.panel.properties;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JCheckBox;

import org.openpixi.pixi.ui.SimulationAnimation;

public class TraceProperties {

	private SimulationAnimation simulationAnimation;
	/** A state for the trace */
	public boolean showTrace = false;

	private boolean resetTrace;

	public TraceProperties(SimulationAnimation simulationAnimation) {
		this.simulationAnimation = simulationAnimation;
	}

	public void clear() {
		resetTrace = true;
	}

	public boolean isShowTrace() {
		return showTrace;
	}

	public void setShowTrace(boolean showTrace) {
		this.showTrace = showTrace;
	}

	public void addComponents(Box box) {
		Box settingControls = Box.createVerticalBox();

		JCheckBox traceCheck;
		traceCheck = new JCheckBox("Trace");
		traceCheck.addItemListener(new CheckListener());
		traceCheck.setSelected(showTrace);

		settingControls.add(traceCheck);
		settingControls.add(Box.createVerticalGlue());

		box.add(settingControls);
	}

	public boolean isCallSuper() {
		boolean callsuper = false;
		if(!showTrace)
		{
			callsuper = true;
		}
		if(resetTrace)
		{
			callsuper = true;
			resetTrace = false;
		}
		return callsuper;
	}

	class CheckListener implements ItemListener {
		public void itemStateChanged(ItemEvent event){
			TraceProperties.this.showTrace = 
					(event.getStateChange() == ItemEvent.SELECTED);
			simulationAnimation.repaint();
		}
	}

}
