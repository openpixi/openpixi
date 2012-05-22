package org.openpixi.pixi.interpreter;

public abstract class CommandSwitchNamed extends CommandSwitch {

	protected CommandSwitchNamed(String LName,String SName,int minArg,int maxArg){
		super(LName,SName,minArg,maxArg);
	}
	
	@Override
	public final boolean isUnnamedSwitch(){
		return false;
	}
	
	// To be implemented by subclasses
	@Override
	public abstract void activate();
}
