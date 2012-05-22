package org.openpixi.pixi.interpreter;

import org.openpixi.pixi.ui.MainControlApplet;

public class PixiSwitch_Batch extends CommandSwitchNamed {

	PixiSwitch_Batch() {
		super("--batch","-b",0,0);
	}
	
	@Override
	public void activate(){
		MainControlApplet.bStartBatch = true;
	}
}
