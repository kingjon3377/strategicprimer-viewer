package model.map.fixtures.resources;

import java.util.HashMap;
import java.util.Map;

/**
 * Kinds of minerals whose events the program knows about.
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
	private MineralKind(final String string) {
		str = string;
	}

	static {
		for (final MineralKind mk : values()) {
			M_MAP.put(mk.str, mk);
		}
	}

	/**
	 * @param string a string representing a MineralKind
	 *
	 * @return the MineralKind it represents
	 */
	public static MineralKind parseMineralKind(final String string) {
		return M_MAP.get(string);
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
