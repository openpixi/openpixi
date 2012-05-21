package org.openpixi.pixi.interpreter;

public class PixiSwitch_EmptySwitch extends CommandSwitch {

	
	//private static CommandSwitchIndex enumIndex	= CommandSwitchIndex.LOAD_INITIAL_CONDITIONS;
	private static String strLongName	= EMPTY_NAME_STR;
	private static String strShortName 	= EMPTY_NAME_STR;
	private static int nMinArgs = 0;
	private static int nMaxArgs = 2;
	
	PixiSwitch_EmptySwitch() {
		super(strLongName,strShortName,nMinArgs,nMaxArgs);
	}
	
	PixiSwitch_EmptySwitch(String LName,String SName){
		//super(enumIndex,LName,SName,nMinArgs,nMaxArgs);
		super(LName,SName,nMinArgs,nMaxArgs);
	}
	
	protected void activate(){
		System.out.println("EMPTY_SWITCH" + " activated!");
		return;
	}
}
