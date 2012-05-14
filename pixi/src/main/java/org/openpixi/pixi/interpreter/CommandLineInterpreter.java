package org.openpixi.pixi.interpreter;

import java.util.ArrayList;

public class CommandLineInterpreter {

//	public static byte MAX_ARGS = 127;
	private static String[] SUPPORTED_SWITCH_START = {"-","/","--"};
	
	private CommandSwitch[] m_arrActivationList;
	private CommandSwitch[] m_arrCommandSwitches = {
			// TODO: Add supported switches here!
			new PixiSwitch_LoadInitialConditions(),
			new PixiSwitch_Example(),
			new PixiSwitch_EmptySwitch()
			// ...
			};
	
	public void interpret(String[] args){
		CommandLineParser parser = new CommandLineParser(m_arrCommandSwitches);
		parser.loadSyntax(SUPPORTED_SWITCH_START);
		m_arrActivationList = parser.parseArguments(args);

		for(CommandSwitch sw : m_arrActivationList){
			//System.out.println(sw.toString());
			sw.activate();
		}
	}
	
	public static void main(String[] args){
		CommandLineInterpreter interpreter = new CommandLineInterpreter();
		interpreter.interpret(args);
		
		return;
	}
}
