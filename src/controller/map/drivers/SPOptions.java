package controller.map.drivers;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import util.Pair;

/**
 * The command-line options passed by the user. At this point we assume that if any
 * option is passed to an app more than once, the subsequent option overrides the
 * previous, and any option passed without argument has an implied argument of "true".
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
public class SPOptions implements Iterable<Pair<String, String>> {
	/**
	 * Options taking arguments.
	 */
	private final Map<String, String> options = new LinkedHashMap<>();
	/**
	 * @param opt an option without argument
	 */
	public void addOption(final String opt) {
		options.put(opt, "true");
	}
	/**
	 * @param opt an option taking an argument
	 * @param arg its argument
	 */
	public void setOption(final String opt, final String arg) {
		options.put(opt, arg);
	}
	/**
	 * @param opt an option
	 * @return whether that option was given, either with or without an argument
	 */
	public boolean hasOption(final String opt) {
		return options.containsKey(opt);
	}
	/**
	 * @param opt an option
	 * @return the value that was given for that option, or "true" if it didn't have
	 * one, or "false" if it wasn't given at all
	 */
	public String getArgument(final String opt) {
		if (options.containsKey(opt)) {
			return options.get(opt);
		} else {
			return "false";
		}
	}
	/**
	 * @return an iterator over the options
	 */
	@Override
	public Iterator<Pair<String, String>> iterator() {
		final Iterator<Map.Entry<String, String>> retval = options.entrySet().iterator();
		return new Iterator<Pair<String, String>>() {
			@Override
			public boolean hasNext() {
				return retval.hasNext();
			}
			@Override
			public Pair<String, String> next() {
				final Map.Entry<String, String> nextVal = retval.next();
				return Pair.of(nextVal.getKey(), nextVal.getValue());
			}
			@Override
			public void remove() {
				retval.remove();
			}
		};
	}
	/**
	 * @return a copy of this object.
	 */
	public SPOptions copy() {
		final SPOptions retval = new SPOptions();
		retval.options.putAll(options);
		return retval;
	}
}
