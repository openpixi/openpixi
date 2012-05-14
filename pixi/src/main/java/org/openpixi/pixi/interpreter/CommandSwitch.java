package org.openpixi.pixi.interpreter;

import java.util.ArrayList;
//import org.openpixi.pixi.interpreter.CommandSwitchIndex;

abstract class CommandSwitch {
	
	protected static String EMPTY_NAME_STR = "";

	//private final CommandSwitchIndex enumIndex;
	private final String m_strLongName;
	private final String m_strShortName;
	private final int m_nMinArgs;
	private final int m_nMaxArgs;
	
	private ArrayList<String> m_arrAdditionalArgs;
	private int m_nAdditionalArgs;
	
	/*
	 * Constructors
	 */
	
	// Disallow creation of unspecified switches
	@SuppressWarnings("unused")
	private CommandSwitch(){
		//this.enumIndex = null;
		this.m_strLongName = null;
		this.m_strShortName = null;
		this.m_nMinArgs = 0;
		this.m_nMaxArgs = 0;
		this.m_nAdditionalArgs = 0;
		this.m_arrAdditionalArgs = null;
	};
	
	// To be called by subclasses
	protected CommandSwitch(
			//CommandSwitchIndex index,
			String LName,
			String SName,
			int min,
			int max
			) {
		// TODO: Value check!
		//this.enumIndex		= index;
		this.m_strLongName	= LName;
		this.m_strShortName	= SName;
		this.m_nMinArgs 		= min;
		this.m_nMaxArgs 		= max;
		this.m_nAdditionalArgs = 0;
		this.m_arrAdditionalArgs = new ArrayList<String>(max);
		
		//System.out.println(toString());
	}

	// To be implemented by subclass
	protected abstract void activate();
	
	/*
	 * Change state
	 */

	final void addAdditionalArg(String arg){
		m_arrAdditionalArgs.add(arg);
		m_nAdditionalArgs++;
	}
	
	/*
	 * Getters
	 */
	
	@Override
	public String toString(){
		String str = new String();

		if( this.isEmptySwitch() ){
			str =	"Long name: " 	+ "EMPTY_NAME_STR" 	+ "\n" +
					"Short name: " 	+ "EMPTY_NAME_STR" 	+ "\n";
		}else{
			str =	"Long name: " 	+ m_strLongName 	+ "\n" +
					"Short name: " 	+ m_strShortName 	+ "\n";
		}
		str = str.concat(
				//"Index: " 	+ enumIndex 	+ "\n" +
				"Min args: " 	+ m_nMinArgs 		+ "\n" +
				"Max args: " 	+ m_nMaxArgs		+ "\n" +
				"Args: "
				);
		
		for(String arg : m_arrAdditionalArgs){
			str = str.concat(arg + " ");
		}
		return str;
	}
	
	/*
	final CommandSwitchIndex getIndex(){
		return enumIndex;
	}
	*/
	
	final String getLongName() {
		return m_strLongName;
	}

	final String getShortName() {
		return m_strShortName;
	}
	
	final int getMinArgs(){
		return m_nMinArgs;
	}
	
	final int getMaxArgs(){
		return m_nMaxArgs;
	}
	
	final String[] getArgs(){
		return m_arrAdditionalArgs.toArray(new String[m_arrAdditionalArgs.size()]);
	}
	
	final int getNumArgs(){
		return m_nAdditionalArgs;
	}

	final boolean isCalled(String str) {
		return m_strLongName.equals(str) || m_strShortName.equals(str);
	}
	
	final boolean isEmptySwitch(){
		if( (m_strLongName.equals(EMPTY_NAME_STR)) || (m_strShortName.equals(EMPTY_NAME_STR)) ){
			return true;
		}
		return false;
	}
}