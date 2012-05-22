package org.openpixi.pixi.ui.interpreter.commandline;

import org.openpixi.pixi.ui.MainControlApplet;
import org.openpixi.pixi.ui.interpreter.commandline.generic.CommandSwitchNamed;

public class SwitchBatch extends CommandSwitchNamed {

	SwitchBatch() {
		super("--batch","-b",0,0);
	}
	
	@Override
	public void activate(){
		MainControlApplet.bStartBatch = true;
	}
}
