package controller.map.drivers;

/**
 * Formerly a driver to start other drivers, this now only persists to allow the build
 * system to continue to work while we're in the partly-Java partly-Ceylon transition.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class AppStarter {
	/**
	 * Entry point: start the driver.
	 *
	 * @param args command-line arguments
	 */
	@SuppressWarnings("AccessOfSystemProperties")
	public static void main(final String... args) {
		System.err.println("Call ceylon run instead");
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "AppStarter";
	}
}
