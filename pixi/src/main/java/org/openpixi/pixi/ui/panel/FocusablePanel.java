package org.openpixi.pixi.ui.panel;

import javax.swing.Box;

/**
 * Interface for focusable panel which can add properties to the property panel.
 */
public interface FocusablePanel {

	public void setFocus(boolean focus);

	public boolean isFocused();

	/**
	 * Add components to the property panel when focused.
	 * @param box Property panel.
	 */
	public void addPropertyComponents(Box box);
}
