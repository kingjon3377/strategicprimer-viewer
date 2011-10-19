package model.map.events;

import java.util.HashMap;
import java.util.Map;

/**
 * The kinds of stone we know about (for purposes of this event).
 */
public enum StoneKind {
	/**
	 * Limestone.
	 */
	Limestone("limestone"),
	/**
	 * Marble.
	 */
	Marble("marble");
	/**
	 * A string representing the StoneKind.
	 */
	private final String str;
	/**
	 * A mapping from string representation to StoneKind.
	 */
	private static final Map<String, StoneKind> SK_MAP = new HashMap<String, StoneKind>();
	/**
	 * @param string a string representing a StoneKind
	 * @return the StoneKind it represents
	 */
	public static StoneKind parseStoneKind(final String string) {
		return SK_MAP.get(string);
	}
	static {
		for (StoneKind sk : values()) {
			SK_MAP.put(sk.str, sk);
		}
	}
	/**
	 * Constructor.
	 * @param string A string representing the StoneKind.
	 */
	private StoneKind(final String string) {
		str = string;
	}
	/**
	 * @return a string representation of the kind of stone
	 */
	@Override
	public String toString() {
		return str;
	}
}
