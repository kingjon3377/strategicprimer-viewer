package model.viewer.events;

import java.util.HashMap;
import java.util.Map;

import model.viewer.events.MineralEvent.MineralKind;

/**
 * Kinds of minerals whose events the program knows about.
 */
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
	private static final Map<String, MineralKind> M_MAP = new HashMap<String, MineralKind>();
	/**
	 * A string representing the MineralKind.
	 */
	private final String str;
	/**
	 * Constructor.
	 * @param string A string representing the MineralKind.
	 */
	private MineralKind(final String string) {
		str = string;
	}
	static {
		for (MineralKind mk : values()) {
			M_MAP.put(mk.str, mk);
		}
	}
	/**
	 * @param string a string representing a MineralKind
	 * @return the MineralKind it represents
	 */
	public static MineralKind parseMineralKind(final String string) {
		return M_MAP.get(string);
	}
	/**
	 * @return a string representation of the mineral
	 */
	@Override
	public String toString() {
		return str;
	}
}