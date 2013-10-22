package model.map.fixtures.towns;

import java.util.HashMap;
import java.util.Map;

/**
 * The kinds of events we know about.
 *
 * @author Jonathan Lovelace
 */
public enum TownKind {
	/**
	 * A fortification.
	 */
	Fortification("fortification"),
	/**
	 * A town.
	 */
	Town("town"),
	/**
	 * A city.
	 */
	City("city");
	/**
	 * A mapping from strings to EventKinds.
	 */
	private static final Map<String, TownKind> EK_MAP = new HashMap<>();
	/**
	 * A string representing the event.
	 */
	private final String str;

	/**
	 * Constructor.
	 *
	 * @param string a string representing the event, to be used in non-specific
	 *        event tags.
	 */
	private TownKind(final String string) {
		str = string;
	}

	static {
		for (final TownKind ek : values()) {
			EK_MAP.put(ek.str, ek);
		}
	}

	/**
	 * @param string a string representing an EventKind
	 *
	 * @return the EventKind it represents
	 */
	public static TownKind parseKind(final String string) {
		if (EK_MAP.containsKey(string)) {
			final TownKind retval = EK_MAP.get(string);
			assert retval != null;
			return retval;
		} else {
			throw new IllegalArgumentException("Unknown town kind");
		}
	}

	/**
	 *
	 * @return a string representing the kind of event.
	 */
	@Override
	public String toString() {
		return str;
	}
}
