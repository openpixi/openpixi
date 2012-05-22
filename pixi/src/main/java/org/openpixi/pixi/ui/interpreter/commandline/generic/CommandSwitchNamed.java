package org.openpixi.pixi.ui.interpreter.commandline.generic;

public abstract class CommandSwitchNamed extends CommandSwitch {

	protected CommandSwitchNamed(String longName,String shortName,int minArg,int maxArg){
		super(longName,shortName,minArg,maxArg);
	}
	
	@Override
	public final boolean isUnnamedSwitch(){
		return false;
	}
	
	// To be implemented by subclasses
	@Override
	public abstract void activate();
}
