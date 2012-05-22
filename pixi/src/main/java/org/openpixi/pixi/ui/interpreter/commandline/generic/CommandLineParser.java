package org.openpixi.pixi.ui.interpreter.commandline.generic;

import java.util.ArrayList;

public class CommandLineParser {
	private enum ErrorFlags{
		NO_ERROR,
		MISSING_ARG,
		BAD_ARG,
		UNKNOWN_SWITCH
	}
	private CommandSwitch errorSwitch = null;
	private ErrorFlags errorFlag = ErrorFlags.NO_ERROR;
	private int errorPosition = -1;
	
	private String[] switchStart	= null;
	private String[] passedStrings 	= null;
	
	private CommandSwitch[] knownSwitches = null;
	private CommandSwitch unnamedSwitch = null;
	private ArrayList<CommandSwitch> parsedSwitches = null;
	
	private boolean syntaxLoaded	= false;
	private boolean switchesLoaded	= false;

	private boolean failOnError		= false;
	private boolean exitParser		= false;

	
	public CommandLineParser(CommandSwitch[] switches) {
		knownSwitches = switches;
		// We cannot parse more switches than we know of:
		parsedSwitches = new ArrayList<CommandSwitch>(switches.length);
		switchesLoaded = true;
		findUnnamedSwitch();
	}
	
	private void findUnnamedSwitch(){
		for(CommandSwitch sw : knownSwitches){
			if(sw.isUnnamedSwitch()){
				unnamedSwitch = sw;
				return;
			}
		}
	}
	
	public void loadSyntax(
			String[] switch_start
			// Other required syntax
			// ...
			){
		if(!syntaxLoaded){
			switchStart = switch_start;
			// ...
			syntaxLoaded = true;
		}
	}
	
	public boolean isReady(){
		return(
				syntaxLoaded &&
				switchesLoaded
				// ...
				);
	}
	
	public void displayStatus(){
		System.out.println("Syntax loaded: " + syntaxLoaded);
		System.out.println("Switches loaded: " + switchesLoaded);
		// ...
	}
	
	public void clean(){
		clearError();
		parsedSwitches.clear();
	}
	
	public CommandSwitch[] parseArguments(String[] args){
		if(!isReady()){
			System.out.println("CMDLineParser not ready");
			displayStatus();
			return new CommandSwitch[0];
		}
				
		clean();
		
		passedStrings = args;
		int nLen = args.length;
		CommandSwitch sw = null;
		int nSwitchPos = 0;
		
		// Parse leading arguments
		nSwitchPos = nextSwitchPosition(0, passedStrings, false);
		if( (nSwitchPos>0) && (unnamedSwitch!=null) ){
			parseAdditionalArgs(0, passedStrings, unnamedSwitch);
			if(errorFlag==ErrorFlags.NO_ERROR){
				parsedSwitches.add(unnamedSwitch);
			}else{
				handleError();
			}
		}
		// Parse the rest of the passed arguments
		while( (nSwitchPos < nLen) && (!exitParser) ){
			
			sw = getSwitch(passedStrings[nSwitchPos]);
			
			if(sw==null){
				errorFlag = ErrorFlags.UNKNOWN_SWITCH;
				errorPosition = nSwitchPos;
				handleError();
			}else{
				parseAdditionalArgs(nSwitchPos+1, passedStrings, sw);
				if(errorFlag==ErrorFlags.NO_ERROR){
					parsedSwitches.add(sw);
				}else{
					handleError();
				}
			}	
			nSwitchPos = nextSwitchPosition(nSwitchPos+1, passedStrings, false);
		}
		return parsedSwitches.toArray(new CommandSwitch[parsedSwitches.size()]);
	}
		
	private void parseAdditionalArgs(int start, String[] args, CommandSwitch sw) {
		String testStr	= null; 
		int nLen		= args.length;
		int nIndex		= 0;
		int nMaxIndex	= nLen-1;
		int nMin		= sw.getMinArgs();
		int nMax		= sw.getMaxArgs();
		
		// Check if array is too short
		if(nMin > nLen - start){
			errorSwitch	= sw;
			errorFlag 	= ErrorFlags.MISSING_ARG;
			errorPosition = nLen;
			return;
		}
		
		for(int found = 0; found < nMax; found++){
			nIndex = start + found;

			if(nIndex > nMaxIndex){
				if(found < nMin){
					errorSwitch	= sw;
					errorFlag 	= ErrorFlags.MISSING_ARG;
					errorPosition = nIndex;
				}
				return;
			}
			
			testStr = args[nIndex];

			if(!isSwitch(testStr, false)){
				sw.addAdditionalArg(testStr);
				continue;
			}else{
				if(found < nMin){
					errorSwitch	= sw;
					errorFlag 	= ErrorFlags.BAD_ARG;
					errorPosition = nIndex;
				}
				return;
			}
		}
	}
	
	private void handleError() {
		String errStr = new String();
		
		switch(errorFlag){
		case NO_ERROR:
			return;
		case MISSING_ARG:
			errStr = "Additional argument(s) expected for " + errorSwitch.getLongName() + ".";
			break;
		case UNKNOWN_SWITCH:
			errStr = "Unknown switch found: " + passedStrings[errorPosition] + ".";
			break;
		case BAD_ARG:
			errStr = "Bad argument for " + errorSwitch.getLongName() + ": " + passedStrings[errorPosition] + ".";
			break;
		default:
			assert false;
			break;
		}
		System.out.println(errStr);
		
		if(failOnError){
			exitParser = true;
		}else{
			clearError();
		}
	}

	private void clearError() {
		errorFlag		= ErrorFlags.NO_ERROR;
		errorSwitch	= null;
		errorPosition	= -1;
		exitParser	= false;
	}

	private CommandSwitch getSwitch(String str) {
		for(CommandSwitch sw : knownSwitches){
			if(sw.isCalled(str)){
				return sw;
			}
		}
		return null;
	}
		
	private boolean isSwitch(String arg, boolean mustBeKnown){
		if(mustBeKnown){
			for(CommandSwitch sw : knownSwitches){
				if(sw.isCalled(arg)){
					return true;
				}
			}
		}else{
			for(String str : switchStart){
				if(arg.startsWith(str)){
					return true;
				}
			}
		}
		return false;
	}
	
	private int nextSwitchPosition(int start, String[] args, boolean mustBeKnown){
		int length = args.length;
		for(int i = start; i < length; i++){
			if(isSwitch(args[i],mustBeKnown)){
				return i;
			}
		}
		return length;
	}
	
	/*
	 * Getters
	 */
	
	public CommandSwitch[] getParsedSwitches(){
		return parsedSwitches.toArray(new CommandSwitch[parsedSwitches.size()]);
	}
}
