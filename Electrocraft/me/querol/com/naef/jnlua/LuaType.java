/*
 * $Id$
 * See LICENSE.txt for license terms.
 */

package me.querol.com.naef.jnlua;

/**
 * Represents a Lua type.
 */
public enum LuaType {
	// -- Values
	/**
	 * Nil.
	 */
	NIL,

	/**
	 * Boolean.
	 */
	BOOLEAN,

	/**
	 * Light user data (pointer).
	 */
	LIGHTUSERDATA,

	/**
	 * Number.
	 */
	NUMBER,

	/**
	 * String.
	 */
	STRING,

	/**
	 * Table.
	 */
	TABLE,

	/**
	 * Function.
	 */
	FUNCTION,

	/**
	 * User data.
	 */
	USERDATA,

	/**
	 * Thread.
	 */
	THREAD;

	// -- Properties
	/**
	 * Returns the display text of this Lua type. The display text is the type
	 * name in lower case.
	 * 
	 * @return the display text
	 */
	public String displayText() {
		return toString().toLowerCase();
	}
}
