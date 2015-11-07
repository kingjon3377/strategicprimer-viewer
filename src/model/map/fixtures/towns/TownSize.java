package model.map.fixtures.towns;

import java.util.HashMap;
import java.util.Map;

import util.NullCleaner;

/**
 * Sizes of towns, fortifications, and cities.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2014 Jonathan Lovelace
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
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

	/**
	 * Constructor.
	 *
	 * @param str a string representing the size.
	 */
	private TownSize(final String str) {
		sizeStr = str;
	}

	static {
		for (final TownSize tsz : values()) {
			TSZ_MAP.put(tsz.sizeStr, tsz);
		}
	}

	/**
	 * @param string a string representing a TownSize
	 *
	 * @return the TownSize it represents
	 */
	public static TownSize parseTownSize(final String string) {
		if (TSZ_MAP.containsKey(string)) {
			return NullCleaner.assertNotNull(TSZ_MAP.get(string));
		} else {
			throw new IllegalArgumentException("Unknown town size");
		}
	}

	/**
	 *
	 * @return a string representation of the size
	 */
	@Override
	public String toString() {
		return sizeStr;
	}
}
