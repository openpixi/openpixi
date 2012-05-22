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
	private CommandSwitch m_UnnamedSwitch = null;
	
	private boolean m_bSyntaxLoaded		= false;
	private boolean m_bSwitchesLoaded	= false;

	private boolean m_bFailOnError		= false;
	private boolean m_bExitParser		= false;

	
	public CommandLineParser(CommandSwitch[] switches) {
		m_arrSwitches = switches;
		// We cannot parse more switches than we know of:
		m_arrParsedSwitches = new ArrayList<CommandSwitch>(switches.length);
		m_bSwitchesLoaded = true;
		findUnnamedSwitch();
	}
	
	private void findUnnamedSwitch(){
		for(CommandSwitch sw : m_arrSwitches){
			if(sw.isUnnamedSwitch()){
				m_UnnamedSwitch = sw;
				return;
			}
		}
	}
	
	public void loadSyntax(
			String[] switch_start
			// Other required syntax
			// ...
			){
		if(!m_bSyntaxLoaded){
			m_arrStrSwitchStart = switch_start;
			// ...
			m_bSyntaxLoaded = true;
		}
	}
	
	public boolean isReady(){
		return(
				m_bSyntaxLoaded &&
				m_bSwitchesLoaded
				// ...
				);
	}
	
	public void displayStatus(){
		System.out.println("Syntax loaded: " + m_bSyntaxLoaded);
		System.out.println("Switches loaded: " + m_bSwitchesLoaded);
		// ...
	}
	
	public void clean(){
		clearError();
		m_arrParsedSwitches.clear();
	}
	
	public CommandSwitch[] parseArguments(String[] args){
		if(!isReady()){
			System.out.println("CMDLineParser not ready");
			displayStatus();
			return new CommandSwitch[0];
		}
				
		clean();
		
		m_arrPassedStrings = args;
		int nLen = args.length;
		CommandSwitch sw = null;
		int nSwitchPos = 0;
		
		// Parse leading arguments
		nSwitchPos = nextSwitchPosition(0, m_arrPassedStrings, false);
		if( (nSwitchPos>0) && (m_UnnamedSwitch!=null) ){
			parseAdditionalArgs(0, m_arrPassedStrings, m_UnnamedSwitch);
			if(m_ErrorFlag==m_enumErrFlag.NO_ERROR){
				m_arrParsedSwitches.add(m_UnnamedSwitch);
			}else{
				handleError();
			}
		}
		// Parse the rest of the passed arguments
		while( (nSwitchPos < nLen) && (!m_bExitParser) ){
			
			sw = getSwitch(m_arrPassedStrings[nSwitchPos]);
			
			if(sw==null){
				m_ErrorFlag = m_enumErrFlag.UNKNOWN_SWITCH;
				m_ErrorPosition = nSwitchPos;
				handleError();
			}else{
				parseAdditionalArgs(nSwitchPos+1, m_arrPassedStrings, sw);
				if(m_ErrorFlag==m_enumErrFlag.NO_ERROR){
					m_arrParsedSwitches.add(sw);
				}else{
					handleError();
				}
			}	
			nSwitchPos = nextSwitchPosition(nSwitchPos+1, m_arrPassedStrings, false);
		}
		return m_arrParsedSwitches.toArray(new CommandSwitch[m_arrParsedSwitches.size()]);
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
			m_ErrorSwitch	= sw;
			m_ErrorFlag 	= m_enumErrFlag.MISSING_ARG;
			m_ErrorPosition = nLen;
			return;
		}
		
		for(int found = 0; found < nMax; found++){
			nIndex = start + found;

			if(nIndex > nMaxIndex){
				if(found < nMin){
					m_ErrorSwitch	= sw;
					m_ErrorFlag 	= m_enumErrFlag.MISSING_ARG;
					m_ErrorPosition = nIndex;
				}
				return;
			}
			
			testStr = args[nIndex];

			if(!isSwitch(testStr, false)){
				sw.addAdditionalArg(testStr);
				continue;
			}else{
				if(found < nMin){
					m_ErrorSwitch	= sw;
					m_ErrorFlag 	= m_enumErrFlag.BAD_ARG;
					m_ErrorPosition = nIndex;
				}
				return;
			}
		}
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
		}else{
			clearError();
		}
	}

	private void clearError() {
		m_ErrorFlag		= m_enumErrFlag.NO_ERROR;
		m_ErrorSwitch	= null;
		m_ErrorPosition	= -1;
		m_bExitParser	= false;
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
			for(CommandSwitch sw : m_arrSwitches){
				if(sw.isCalled(arg)){
					return true;
				}
			}
		}else{
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
		return length;
	}
	
	/*
	 * Getters
	 */
	
	public CommandSwitch[] getParsedSwitches(){
		return m_arrParsedSwitches.toArray(new CommandSwitch[m_arrParsedSwitches.size()]);
	}
}
