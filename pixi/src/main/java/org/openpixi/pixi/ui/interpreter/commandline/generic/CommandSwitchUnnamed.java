package org.openpixi.pixi.ui.interpreter.commandline.generic;

public abstract class CommandSwitchUnnamed extends CommandSwitch {

	protected CommandSwitchUnnamed(int minArg,int maxArg) {
		super("","",minArg,maxArg);
	}
	
	@Override
	public final boolean isUnnamedSwitch(){
		return true;
	}
	
	// To be implemented by subclasses
	@Override
	public abstract void activate();
}
