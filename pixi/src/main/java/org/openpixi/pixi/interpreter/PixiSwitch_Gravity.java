package org.openpixi.pixi.interpreter;

final class PixiSwitch_Gravity extends CommandSwitch {
	
	//private static CommandSwitchIndex enumIndex	= CommandSwitchIndex.LOAD_INITIAL_CONDITIONS;
	private static String strLongName	= "-gravity";
	private static String strShortName	= "-gravity";
	private static int nMinArgs = 1;
	private static int nMaxArgs = 3;
	
	PixiSwitch_Gravity() {
		super(strLongName,strShortName,nMinArgs,nMaxArgs);
	}
	
	PixiSwitch_Gravity(String LName,String SName){
		//super(enumIndex,LName,SName,nMinArgs,nMaxArgs);
		super(LName,SName,nMinArgs,nMaxArgs);
	}
	
	@Override
	protected void activate(){
		double[] dGVector = new double[3];
		int len = getArgs().length;
		
		System.out.println(strLongName + " activated!");
		
		for(int i=0; i < 3; i++){
			if(i<len)
				dGVector[i] = Double.valueOf(getArgs()[i]);
			else
				dGVector[i] = 0d;
		}
		
		System.out.print("GVector: |");
		for(double d : dGVector)
			System.out.print(d + "|");
		System.out.println();
		
		return;
	}
}