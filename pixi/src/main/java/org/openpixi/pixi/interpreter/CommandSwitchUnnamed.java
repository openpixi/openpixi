package org.openpixi.pixi.interpreter;

public abstract class CommandSwitchUnnamed extends CommandSwitch {

	CommandSwitchUnnamed(int minArg,int maxArg) {
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
