package controller.map.drivers;

import util.Pair;

/**
 * An interface for the command-line options passed by the user. At this point we
 * assume that if any option is passed to an app more than once, the subsequent option
 * overrides the previous, and any option passed without argument has an implied
 * argument of "true".
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface SPOptions extends Iterable<Pair<String, String>> {
	/**
	 * Whether the given option was given.
	 * @param opt an option
	 * @return whether that option was given, either with or without an argument
	 */
	boolean hasOption(String opt);

	/**
	 * Get the argument provided for the given argument (or "true" if given without one,
	 * "false" if not given).
	 * @param opt an option
	 * @return the value that was given for that option, or "true" if it didn't have one,
	 * or "false" if it wasn't given at all
	 */
	String getArgument(String opt);

	/**
	 * Clone the object.
	 * @return a copy of this object.
	 */
	SPOptions copy();
}
