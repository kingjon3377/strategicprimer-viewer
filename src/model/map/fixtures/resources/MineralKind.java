package model.map.fixtures.resources;

import java.util.HashMap;
import java.util.Map;

/**
 * Kinds of minerals whose events the program knows about.
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
 * @deprecated We now use free-form strings for minerals' kinds. Eventually we'll want to
 * load a list of kinds from file.
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

	static {
		for (final MineralKind kind : values()) {
			M_MAP.put(kind.str, kind);
		}
	}

	/**
	 * Constructor.
	 *
	 * @param desc A string representing the MineralKind.
	 */
	MineralKind(final String desc) {
		str = desc;
	}

	/**
	 * @param desc a string representing a MineralKind
	 * @return the MineralKind it represents
	 */
	public static MineralKind parseMineralKind(final String desc) {
		if (M_MAP.containsKey(desc)) {
			return M_MAP.get(desc);
		} else {
			throw new IllegalArgumentException("Not a kind of mineral we recognize");
		}
	}

	/**
	 * @return a string representation of the mineral
	 */
	@Override
	public String toString() {
		return str;
	}
}
