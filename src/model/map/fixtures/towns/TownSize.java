package model.map.fixtures.towns;

import java.util.HashMap;
import java.util.Map;
import util.NullCleaner;

/**
 * Sizes of towns, fortifications, and cities.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public enum TownSize {
	/**
	 * Small.
	 */
	Small("small"),
	/**
	 * Medium.
	 */
	Medium("medium"),
	/**
	 * Large.
	 */
	Large("large");
	/**
	 * A mapping from string to TownSize.
	 */
	private static final Map<String, TownSize> TSZ_MAP = new HashMap<>();
	/**
	 * A string representing the size.
	 */
	private final String sizeStr;

	static {
		for (final TownSize tsz : values()) {
			TSZ_MAP.put(tsz.sizeStr, tsz);
		}
	}

	/**
	 * Constructor.
	 *
	 * @param str a string representing the size.
	 */
	TownSize(final String str) {
		sizeStr = str;
	}

	/**
	 * @param sizeDesc a string representing a TownSize
	 * @return the TownSize it represents
	 */
	public static TownSize parseTownSize(final String sizeDesc) {
		if (TSZ_MAP.containsKey(sizeDesc)) {
			return NullCleaner.assertNotNull(TSZ_MAP.get(sizeDesc));
		} else {
			throw new IllegalArgumentException("Unknown town size");
		}
	}

	/**
	 * @return a string representation of the size
	 */
	@Override
	public String toString() {
		return sizeStr;
	}
}
