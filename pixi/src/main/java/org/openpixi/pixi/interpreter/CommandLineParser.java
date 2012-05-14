package org.openpixi.pixi.interpreter;

import java.util.ArrayList;

public class CommandLineParser {
	private enum m_enumErrFlag{
		NO_ERROR,
		MISSING_ARG,
		BAD_ARG,
		UNKNOWN_SWITCH
	}
	private CommandSwitch m_ErrorSwitch	= null;
	private m_enumErrFlag m_ErrorFlag	= m_enumErrFlag.NO_ERROR;
	private int m_ErrorPosition 		= -1;
	
	private String[] m_arrStrSwitchStart	= null;
	private String[] m_arrPassedStrings 	= null;
	
	private CommandSwitch[] m_arrSwitches = null;
	private ArrayList<CommandSwitch> m_arrParsedSwitches = null;
	
	private boolean m_bSyntaxLoaded		= false;
	private boolean m_bSwitchesLoaded	= false;
	private boolean m_bHasEmptySwitch	= false;
	private boolean m_bFailOnError		= false;
	private boolean m_bExitParser		= false;

	private CommandSwitch m_EmptySwitch = null;

	public CommandLineParser(CommandSwitch[] switches) {
		m_arrSwitches = switches;
		m_arrParsedSwitches = new ArrayList<CommandSwitch>(switches.length);	// We cannot parse more switches than we know :-)
		m_bSwitchesLoaded = true;
		findEmptySwitch();
	}
	
	private void findEmptySwitch(){
		for(CommandSwitch sw : m_arrSwitches){
			if(sw.isCalled(CommandSwitch.EMPTY_NAME_STR)){
				m_EmptySwitch = sw;
				m_bHasEmptySwitch = true;
				return;
			}
		}
	}
	
	public boolean loadSyntax(
			String[] switch_start
			// Other required syntax
			// ...
			){
		if(m_bSyntaxLoaded){
			System.out.println("Syntax already loaded: nothing changed");
		}else{
			m_arrStrSwitchStart = switch_start;
			// ...
			m_bSyntaxLoaded = true;
		}
		return true;
	}
	
	public boolean isReady(){
		return(
				m_bSyntaxLoaded &&
				m_bSwitchesLoaded
				);
	}
	
	public void displayStatus(){
		System.out.println("Syntax loaded: " + m_bSyntaxLoaded);
		System.out.println("Switches loaded: " + m_bSwitchesLoaded);
		// ...
	}
	
	public CommandSwitch[] parseArguments(String[] args){
		
		if(!assertReady()){
			return returnArgs();
		}

		m_arrPassedStrings = args;		
		clearError();

		CommandSwitch sw = null;
		int nLen = args.length;
		int nSwitchPos = 0;
		
		///*
		// Parse leading arguments
		nSwitchPos = nextSwitchPosition(0, m_arrPassedStrings, false);
		if( (nSwitchPos>0) && (m_bHasEmptySwitch) ){
			parseAdditionalArgs(0, m_arrPassedStrings, m_EmptySwitch);
			if(m_ErrorFlag==m_enumErrFlag.NO_ERROR){
				m_arrParsedSwitches.add(m_EmptySwitch);
				notifyParsed(m_EmptySwitch);
			}else{
				handleError();
				//clearError();
			}
		}

		while( (nSwitchPos < nLen) && (!m_bExitParser) ){
			// Obtain switch
			sw = getSwitch(m_arrPassedStrings[nSwitchPos]);
			if(sw==null){
				// Unknown switch: raise error flag
				m_ErrorFlag = m_enumErrFlag.UNKNOWN_SWITCH;
				m_ErrorPosition = nSwitchPos;
				handleError();
				//nSwitchPos = nextSwitchPosition(nSwitchPos+1, m_arrPassedStrings, false);
				//continue;
			}else{
				// Known switch
				parseAdditionalArgs(nSwitchPos+1, m_arrPassedStrings, sw);
				if(m_ErrorFlag==m_enumErrFlag.NO_ERROR){
					m_arrParsedSwitches.add(sw);
					notifyParsed(sw);
				}else{
					handleError();
				}
				//nSwitchPos = nextSwitchPosition(nSwitchPos+1, m_arrPassedStrings, false);
			}
			// Jump to next switch
			nSwitchPos = nextSwitchPosition(nSwitchPos+1, m_arrPassedStrings, false);
		}

		// Convert to an array of the correct type
		return returnArgs();
	}
	
	private void handleError() {
		String errStr = new String();
		
		switch(m_ErrorFlag){
		case NO_ERROR:
			return;
		case MISSING_ARG:
			errStr = "Additional argument(s) expected for " + m_ErrorSwitch.getLongName() + ".";
			break;
		case UNKNOWN_SWITCH:
			errStr = "Unknown switch found: " + m_arrPassedStrings[m_ErrorPosition] + ".";
			break;
		case BAD_ARG:
			errStr = "Bad argument for " + m_ErrorSwitch.getLongName() + ": " + m_arrPassedStrings[m_ErrorPosition] + ".";
			break;
		default:
			assert false;
			break;
		}
		System.out.println(errStr);
		if(m_bFailOnError){
			m_bExitParser = true;
		}
		clearError();
	}

	private void clearError() {
		m_ErrorFlag		= m_enumErrFlag.NO_ERROR;
		m_ErrorSwitch		= null;
		m_ErrorPosition		= -1;
		m_bExitParser	= false;
	}

	private boolean assertReady(){
		if(isReady()){
			return true;
		}else{
			System.out.println("CMDLineParser not ready");
			displayStatus();
			return false;
		}
	}
	
	private CommandSwitch[] returnArgs(){
		return m_arrParsedSwitches.toArray(new CommandSwitch[m_arrParsedSwitches.size()]);
		/*
		if(m_ErrFlag == m_enumErrFlag.NO_ERROR){
			return m_arrParsedSwitches.toArray(new CommandSwitch[m_arrParsedSwitches.size()]);
		}else{
			return new CommandSwitch[0];
		}
		*/
	}
	
	private void notifyParsed(CommandSwitch sw) {
		if(sw.isEmptySwitch()){
			System.out.print("EMPTY_SWITCH");
		}else{
			System.out.print(sw.getLongName());
		}
		System.out.print(" parsed with " + sw.getNumArgs() + " argument(s): ");
		System.out.print('|');
		for(String str : sw.getArgs()){
			System.out.print(str + "|");
		}
		System.out.println();
	}
	/*
	private void notifyIgnoreRange(int start_incl, int end_excl, String[] args) {
		int len = args.length;
		for(int i=start_incl; (i < len) && (i < end_excl) && (i >= 0); i++){
			System.out.println(args[i] + " ignored.");
		}
	}
	*/
	private void parseAdditionalArgs(int start, String[] args, CommandSwitch sw) {
		String testStr	= ""; 
		int nLen		= args.length;
		int nIndex		= 0;
		int nMaxIndex	= nLen-1;
		int nMin		= sw.getMinArgs();
		int nMax		= sw.getMaxArgs();
		
		// Check if array is too short
		if(nMin > nLen - start){
			m_ErrorSwitch = sw;
			m_ErrorFlag 	= m_enumErrFlag.MISSING_ARG;
			m_ErrorPosition 	= nLen;
			return;
		}
		
		for(int found = 0; found < nMax; found++){
			nIndex = start + found;
			// index > array size ?
			if(nIndex > nMaxIndex){
				// Yes: already found enough?
				if(found < nMin){
					// No: raise error flag
					m_ErrorSwitch = sw;
					m_ErrorFlag 	= m_enumErrFlag.MISSING_ARG;
					m_ErrorPosition 	= nIndex;
				}
				return;
			}
			testStr = args[nIndex];
			// Does the current element have a switch-syntax?
			if(!isSwitch(testStr, false)){
				// No: add it as an additional argument and continue
				sw.addAdditionalArg(testStr);
				continue;
			}else{
				// Yes: check if we already have enough arguments for the current switch
				if(found < nMin){
					// No: raise error flag
					m_ErrorSwitch = sw;
					m_ErrorFlag 	= m_enumErrFlag.BAD_ARG;
					m_ErrorPosition 	= nIndex;
				}
				return;
			}
		}
	}
	
	private CommandSwitch getSwitch(String str) {
		for(CommandSwitch sw : m_arrSwitches){
			if(sw.isCalled(str)){
				return sw;
			}
		}
		return null;
	}
		
	private boolean isSwitch(String arg, boolean mustBeKnown){
		if(mustBeKnown){
			// Check among known/loaded switches
			for(CommandSwitch sw : m_arrSwitches){
				if(sw.isCalled(arg)){
					return true;
				}
			}
		}else{
			// Just check for switch syntax
			for(String str : m_arrStrSwitchStart){
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
		//return -1;
		return length;
	}
	
	public CommandSwitch[] getParsedSwitches(){
		return returnArgs();
	}
}
