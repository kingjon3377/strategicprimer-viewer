package controller.map.drivers;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import util.Pair;
import util.SetPairConverter;

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
public class SPOptionsImpl implements SPOptions {
	/**
	 * Options taking arguments.
	 */
	private final Map<String, String> options;
	/**
	 * Adapter to it.
	 */
	private final Iterable<Pair<String, String>> adapter;

	/**
	 * Add an option without any argument.
	 * @param opt an option without argument
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	public void addOption(final String opt) {
		options.put(opt, "true");
	}

	/**
	 * Add an option taking an argument.
	 * @param opt an option taking an argument
	 * @param arg its argument
	 */
	public void setOption(final String opt, final String arg) {
		options.put(opt, arg);
	}

	/**
	 * Whether the given option was provided.
	 * @param opt an option
	 * @return whether that option was given, either with or without an argument
	 */
	@Override
	public boolean hasOption(final String opt) {
		return options.containsKey(opt);
	}

	/**
	 * Get the argument that was provided for the given argument, or "false" if it
	 * wasn't specified.
	 * @param opt an option
	 * @return the value that was given for that option, or "true" if it didn't have one,
	 * or "false" if it wasn't given at all
	 */
	@Override
	public String getArgument(final String opt) {
		if (options.containsKey(opt)) {
			return options.get(opt);
		} else {
			return "false";
		}
	}

	/**
	 * An iterator over the specified options.
	 * @return an iterator over the options
	 */
	@Override
	public Iterator<Pair<String, String>> iterator() {
		return adapter.iterator();
	}
	/**
	 * Copy constructor.
	 * @param existing another instance to copy from
	 */
	public SPOptionsImpl(final Iterable<Pair<String, String>> existing) {
		options = new LinkedHashMap<>(StreamSupport.stream(existing.spliterator(), false)
				.collect(Collectors.toMap(Pair::first, Pair::second)));
		adapter = new SetPairConverter<>(options);
	}
	/**
	 * No-arg constructor.
	 */
	public SPOptionsImpl() {
		options = new LinkedHashMap<>();
		adapter = new SetPairConverter<>(options);
	}
	/**
	 * Clone the object.
	 * @return a copy of this object.
	 */
	@Override
	public SPOptionsImpl copy() {
		return new SPOptionsImpl(this);
	}
	/**
	 * Shows the options that the user has selected.
	 * @return a String representation of the options
	 */
	@Override
	public String toString() {
		final StringBuilder build = new StringBuilder(options.size() * 20);
		for (final Map.Entry<String, String> entry : options.entrySet()) {
			build.append(entry.getKey());
			if (!"true".equals(entry.getValue())) {
				build.append('=');
				build.append(entry.getValue());
			}
			build.append('\n');
		}
		return build.toString();
	}
}
