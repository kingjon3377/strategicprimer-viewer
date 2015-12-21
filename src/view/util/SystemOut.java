package view.util;

import util.NullCleaner;

import java.io.PrintStream;

/**
 * A class to get around Eclipse's insistence that System.out might be null. (FIXME:
 * Remove this if it's no longer necessary.)
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class SystemOut {
	/**
	 * The singleton object.
	 */
	public static final PrintStream SYS_OUT = NullCleaner
			                                          .assertNotNull(System.out);

	/**
	 * Constructor.
	 */
	private SystemOut() {
		// Do not instantiate.
	}

	/**
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "SystemOut";
	}
}
