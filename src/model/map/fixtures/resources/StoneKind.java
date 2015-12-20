package model.map.fixtures.resources;

import java.util.HashMap;
import java.util.Map;

import util.NullCleaner;

/**
 * The kinds of stone we know about (for purposes of this event).
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
public enum StoneKind {
	/**
	 * Limestone.
	 */
	Limestone("limestone"),
	/**
	 * Marble.
	 */
	Marble("marble"),
	/**
	 * Slate.
	 */
	Slate("slate"),
	/**
	 * Pumice.
	 */
	Pumice("pumice"),
	/**
	 * Conglomerate.
	 */
	Conglomerate("conglomerate"),
	/**
	 * Sandstone.
	 */
	Sandstone("sandstone"),
	/**
	 * Laterite. TODO: Is this only under jungle?
	 */
	Laterite("laterite"),
	/**
	 * Shale.
	 */
	Shale("shale");
	/**
	 * A string representing the StoneKind.
	 */
	private final String str;
	/**
	 * A mapping from string representation to StoneKind.
	 */
	private static final Map<String, StoneKind> SK_MAP = new HashMap<>();

	/**
	 * @param string a string representing a StoneKind
	 *
	 * @return the StoneKind it represents
	 */
	public static StoneKind parseStoneKind(final String string) {
		if (SK_MAP.containsKey(string)) {
			return NullCleaner.assertNotNull(SK_MAP.get(string));
		} else {
			throw new IllegalArgumentException("Unrecognized kind of stone");
		}
	}

	static {
		for (final StoneKind kind : values()) {
			SK_MAP.put(kind.str, kind);
		}
	}

	/**
	 * Constructor.
	 *
	 * @param string A string representing the StoneKind.
	 */
	StoneKind(final String string) {
		str = string;
	}

	/**
	 *
	 * @return a string representation of the kind of stone
	 */
	@Override
	public String toString() {
		return str;
	}
}
