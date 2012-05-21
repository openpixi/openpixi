package org.openpixi.pixi.interpreter;

final class PixiSwitch_LoadInitialConditions extends CommandSwitch {
	
	//private static CommandSwitchIndex enumIndex	= CommandSwitchIndex.LOAD_INITIAL_CONDITIONS;
	private static String strLongName	= "-initcond";
	private static String strShortName 	= "-ic";
	private static int nMinArgs = 1;
	private static int nMaxArgs = 1;
	
	PixiSwitch_LoadInitialConditions() {
		super(strLongName,strShortName,nMinArgs,nMaxArgs);
	}
	
	PixiSwitch_LoadInitialConditions(String LName,String SName){
		//super(enumIndex,LName,SName,nMinArgs,nMaxArgs);
		super(LName,SName,nMinArgs,nMaxArgs);
	}
	
	@Override
	protected void activate(){
		System.out.println(strLongName + " activated!");
		return;
	}
}