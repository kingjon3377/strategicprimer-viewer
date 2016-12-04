package util;

/**
 * A utility class to detect, and cache, whether the system is a Mac or not.
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
public final class OnMac {
	/**
	 * Whether this system is a mac.
	 */
	@SuppressWarnings("AccessOfSystemProperties")
	public static final boolean SYSTEM_IS_MAC;

	static {
		final String temp = System.getProperty("os.name");
		if (temp == null) {
			SYSTEM_IS_MAC = false;
		} else {
			SYSTEM_IS_MAC = temp.toLowerCase().startsWith("mac os x");
		}
	}

	/**
	 * Do not instantiate.
	 */
	private OnMac() {
		// Do not instantiate.
	}
}
