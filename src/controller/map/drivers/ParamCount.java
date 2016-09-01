package controller.map.drivers;

/**
 * Possible numbers of (non-option?) parameters a driver might want.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public enum ParamCount {
	/**
	 * None at all.
	 */
	None,
	/**
	 * One.
	 */
	One,
	/**
	 * Exactly two.
	 */
	Two,
	/**
	 * At least one.
	 */
	AtLeastOne,
	/**
	 * At least two.
	 */
	AtLeastTwo,
	/**
	 * Any number, zero oro more.
	 */
	AnyNumber
}
