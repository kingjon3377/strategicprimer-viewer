package view.util;

/**
 * A class for a wrapper around System.exit().
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2013 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 */
public final class DriverQuit {
	/**
	 * Do not instantiate.
	 */
	private DriverQuit() {
		// Do nothing.
	}

	/**
	 * Quit. Note that this should not be called from a non-static context
	 * except by some CLI drivers.
	 *
	 * @param code The exit code.
	 */
	public static void quit(final int code) {
		System.exit(code);
	}
}
