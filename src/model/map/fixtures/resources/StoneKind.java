package model.map.fixtures.resources;

import java.util.HashMap;
import java.util.Map;
import util.NullCleaner;

/**
 * The kinds of stone we know about (for purposes of this event).
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
	 * Laterite. Should only be found under jungle.
	 */
	Laterite("laterite"),
	/**
	 * Shale.
	 */
	Shale("shale");
	/**
	 * A mapping from string representation to StoneKind.
	 */
	private static final Map<String, StoneKind> SK_MAP = new HashMap<>();
	/**
	 * A string representing the StoneKind.
	 */
	private final String str;

	static {
		for (final StoneKind kind : values()) {
			SK_MAP.put(kind.str, kind);
		}
	}

	/**
	 * Constructor.
	 *
	 * @param desc A string representing the StoneKind.
	 */
	StoneKind(final String desc) {
		str = desc;
	}

	/**
	 * @param kind a string representing a StoneKind
	 * @return the StoneKind it represents
	 */
	public static StoneKind parseStoneKind(final String kind) {
		if (SK_MAP.containsKey(kind)) {
			return NullCleaner.assertNotNull(SK_MAP.get(kind));
		} else {
			throw new IllegalArgumentException("Unrecognized kind of stone");
		}
	}

	/**
	 * @return a string representation of the kind of stone
	 */
	@Override
	public String toString() {
		return str;
	}
}
