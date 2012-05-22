package org.openpixi.pixi.ui.interpreter.commandline;

import org.openpixi.pixi.ui.interpreter.commandline.generic.CommandLineParser;
import org.openpixi.pixi.ui.interpreter.commandline.generic.CommandSwitch;


public class CommandLineInterpreter {

//	public static byte MAX_ARGS = 127;
	private static String[] SWITCH_START = {"-","/","--"};
	
	private CommandSwitch[] activationList;
	private CommandSwitch[] knownSwitches = {
			// Add supported switches here:
			new SwitchBatch(),
			new SwitchUnnamed()
			// ...
			};
	
	public void interpret(String[] args){
		CommandLineParser parser = new CommandLineParser(knownSwitches);
		parser.loadSyntax(SWITCH_START);
		activationList = parser.parseArguments(args);
		
		// TODO: Check compatibility between parsed switches

		// Activate switches
		for(CommandSwitch sw : activationList){
			sw.activate();
		}
	}
}
