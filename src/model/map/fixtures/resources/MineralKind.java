package model.map.fixtures.resources;

import java.util.HashMap;
import java.util.Map;

import util.NullCleaner;

/**
 * Kinds of minerals whose events the program knows about.
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
 * @deprecated We now use free-form strings for minerals' kinds. Eventually
 *             we'll want to load a list of kinds from file.
 */
@Deprecated
public enum MineralKind {
	/**
	 * Iron.
	 */
	Iron("iron"),
	/**
	 * Copper.
	 */
	Copper("copper"),
	/**
	 * Gold.
	 */
	Gold("gold"),
	/**
	 * Silver.
	 */
	Silver("silver"),
	/**
	 * Coal.
	 */
	Coal("coal");
	/**
	 * A mapping from string to MineralKind.
	 */
	private static final Map<String, MineralKind> M_MAP = new HashMap<>();
	/**
	 * A string representing the MineralKind.
	 */
	private final String str;

	/**
	 * Constructor.
	 *
	 * @param string A string representing the MineralKind.
	 */
	MineralKind(final String string) {
		str = string;
	}

	static {
		for (final MineralKind kind : values()) {
			M_MAP.put(kind.str, kind);
		}
	}

	/**
	 * @param string a string representing a MineralKind
	 *
	 * @return the MineralKind it represents
	 */
	public static MineralKind parseMineralKind(final String string) {
		if (M_MAP.containsKey(string)) {
			return NullCleaner.assertNotNull(M_MAP.get(string));
		} else {
			throw new IllegalArgumentException("Not a kind of mineral we recognize");
		}
	}

	/**
	 *
	 * @return a string representation of the mineral
	 */
	@Override
	public String toString() {
		return str;
	}
}
