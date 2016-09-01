package util;

import java.util.Random;

/**
 * A wrapper class for a single Random for the whole application.
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
public final class SingletonRandom {
	/**
	 * The singleton Random.
	 */
	public static final Random RANDOM = new Random(System.currentTimeMillis());

	/**
	 * Private constructor so we can't instantiate this class.
	 */
	private SingletonRandom() {
		// Do nothing
	}
}
