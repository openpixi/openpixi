package org.openpixi.pixi.ui.interpreter.commandline.generic;

import java.util.ArrayList;

public abstract class CommandSwitch {
	private final String longName;
	private final String shortName;
	private final int minArgs;
	private final int maxArgs;
	
	private ArrayList<String> additionalArgs;
	
	/*
	 * Constructors
	 */
	
	// Disallow creation of unspecified switches
	@SuppressWarnings("unused")
	private CommandSwitch(){
		// Initialize final fields
		this.longName	= null;
		this.shortName	= null;
		this.minArgs	= 0;
		this.maxArgs	= 0;
	};
	
	// To be called by subclasses in the package
	CommandSwitch(
			String longName,
			String shortName,
			int minArg,
			int maxArg
			) {
		// TODO: Value check!
		this.longName	= longName;
		this.shortName	= shortName;
		this.minArgs	= minArg;
		this.maxArgs	= maxArg;
		this.additionalArgs = new ArrayList<String>(maxArg);
	}

	// To be implemented by subclasses
	public abstract void activate();
	
	/*
	 * Change state
	 */

	public final void addAdditionalArg(String arg){
		additionalArgs.add(arg);
	}
	
	/*
	 * Getters
	 */
	// To be implemented by subclasses in the package
	abstract boolean isUnnamedSwitch();
	
	@Override
	public String toString(){
		String str = new String();

		str = "\nSwitch: ";
		if( this.isUnnamedSwitch() ){
			str +=	"Unnamed";
		}else{
			str +=	longName 	+ "/" + shortName;
		}
		str +=	" (" + minArgs + "/" + maxArgs	+ ")\n" +
				"Args: ";
		
		for(String arg : additionalArgs){
			str += arg + " ";
		}
		return str;
	}
	
	public final String getLongName() {
		return longName;
	}

	public final String getShortName() {
		return shortName;
	}
	
	public final int getMinArgs(){
		return minArgs;
	}
	
	public final int getMaxArgs(){
		return maxArgs;
	}
	
	public final String[] getArgs(){
		return additionalArgs.toArray(new String[additionalArgs.size()]);
	}
	
	public final int getNumArgs(){
		return additionalArgs.size();
	}

	public final boolean isCalled(String str) {
		return longName.equals(str) || shortName.equals(str);
	}
}