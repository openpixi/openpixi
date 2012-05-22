package org.openpixi.pixi.interpreter;

import java.util.ArrayList;

public abstract class CommandSwitch {
	private final String m_strLongName;
	private final String m_strShortName;
	private final int m_nMinArgs;
	private final int m_nMaxArgs;
	
	private ArrayList<String> m_arrAdditionalArgs;
	
	/*
	 * Constructors
	 */
	
	// Disallow creation of unspecified switches
	@SuppressWarnings("unused")
	private CommandSwitch(){
		// Initialize final fields
		this.m_strLongName = null;
		this.m_strShortName = null;
		this.m_nMinArgs = 0;
		this.m_nMaxArgs = 0;
	};
	
	// To be called by subclasses in the package
	CommandSwitch(
			String LName,
			String SName,
			int minArg,
			int maxArg
			) {
		// TODO: Value check!
		this.m_strLongName	= LName;
		this.m_strShortName	= SName;
		this.m_nMinArgs = minArg;
		this.m_nMaxArgs = maxArg;
		this.m_arrAdditionalArgs = new ArrayList<String>(maxArg);
	}

	// To be implemented by subclasses in the package
	protected abstract void activate();
	
	/*
	 * Change state
	 */

	public final void addAdditionalArg(String arg){
		m_arrAdditionalArgs.add(arg);
	}
	
	/*
	 * Getters
	 */
	// To be implemented by subclasses in the package
	abstract boolean isUnnamedSwitch();
	
	@Override
	public String toString(){
		String str = new String();

		str = "\nSwitch: ";
		if( this.isUnnamedSwitch() ){
			str +=	"Unnamed";
		}else{
			str +=	m_strLongName 	+ "/" + m_strShortName;
		}
		str +=	" (" + m_nMinArgs + "/" + m_nMaxArgs	+ ")\n" +
				"Args: ";
		
		for(String arg : m_arrAdditionalArgs){
			str += arg + " ";
		}
		return str;
	}
	
	public final String getLongName() {
		return m_strLongName;
	}

	public final String getShortName() {
		return m_strShortName;
	}
	
	public final int getMinArgs(){
		return m_nMinArgs;
	}
	
	public final int getMaxArgs(){
		return m_nMaxArgs;
	}
	
	public final String[] getArgs(){
		return m_arrAdditionalArgs.toArray(new String[m_arrAdditionalArgs.size()]);
	}
	
	public final int getNumArgs(){
		return m_arrAdditionalArgs.size();
	}

	public final boolean isCalled(String str) {
		return m_strLongName.equals(str) || m_strShortName.equals(str);
	}
}