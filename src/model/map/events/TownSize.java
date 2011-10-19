package model.map.events;

import java.util.HashMap;
import java.util.Map;

/**
 * Sizes of towns, fortifications, and cities.
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
	private static final Map<String, TownSize> TSZ_MAP = new HashMap<String, TownSize>();
	/**
	 * A string representing the size.
	 */
	private final String sizeStr;

	/**
	 * Constructor.
	 * 
	 * @param str
	 *            a string representing the size.
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
	 * @param string
	 *            a string representing a TownSize
	 * @return the TownSize it represents
	 */
	public static TownSize parseTownSize(final String string) {
		return TSZ_MAP.get(string);
	}

	/**
	 * @return a string representation of the size
	 */
	@Override
	public String toString() {
		return sizeStr;
	}
}
