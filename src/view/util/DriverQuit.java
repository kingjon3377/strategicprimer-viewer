package view.util;

/**
 * A class for a wrapper around System.exit().
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("UtilityClassCanBeEnum")
public final class DriverQuit {
	/**
	 * Do not instantiate.
	 */
	private DriverQuit() {
		// Do nothing.
	}

	/**
	 * Quit. Note that this should not be called from a non-static context except by some
	 * CLI drivers.
	 *
	 * @param code The exit code.
	 */
	@SuppressWarnings("CallToSystemExit")
	public static void quit(final int code) {
		System.exit(code);
	}
}
