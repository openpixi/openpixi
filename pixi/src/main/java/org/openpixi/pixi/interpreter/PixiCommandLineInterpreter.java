package org.openpixi.pixi.interpreter;

public class PixiCommandLineInterpreter {

//	public static byte MAX_ARGS = 127;
	private static String[] SUPPORTED_SWITCH_START = {"-","/","--"};
	
	private CommandSwitch[] m_arrActivationList;
	private CommandSwitch[] m_arrCommandSwitches = {
			// Add supported switches here:
			new PixiSwitch_Batch(),
			new PixiSwitch_Unnamed()
			// ...
			};
	
	public void interpret(String[] args){
		CommandLineParser parser = new CommandLineParser(m_arrCommandSwitches);
		parser.loadSyntax(SUPPORTED_SWITCH_START);
		m_arrActivationList = parser.parseArguments(args);
		
		// TODO: Check compatibility between parsed switches

		// Activate switches
		for(CommandSwitch sw : m_arrActivationList){
			sw.activate();
		}
	}
}
