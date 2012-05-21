package org.openpixi.pixi.interpreter;

final class PixiSwitch_Example extends CommandSwitch {
	
	//private static CommandSwitchIndex enumIndex	= CommandSwitchIndex.LOAD_INITIAL_CONDITIONS;
	private static String strLongName	= "-example";
	private static String strShortName	= "-ex";
	private static int nMinArgs = 1;
	private static int nMaxArgs = 2;
	
	PixiSwitch_Example() {
		super(strLongName,strShortName,nMinArgs,nMaxArgs);
	}
	
	PixiSwitch_Example(String LName,String SName){
		//super(enumIndex,LName,SName,nMinArgs,nMaxArgs);
		super(LName,SName,nMinArgs,nMaxArgs);
	}
	
	@Override
	protected void activate(){
		System.out.println(strLongName + " activated!");
		return;
	}
}