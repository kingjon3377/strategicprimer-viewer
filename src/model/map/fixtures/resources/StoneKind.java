package model.map.fixtures.resources;

import java.util.HashMap;
import java.util.Map;

import util.NullCleaner;

/**
 * The kinds of stone we know about (for purposes of this event).
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
	Marble("marble");
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
	private StoneKind(final String string) {
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
