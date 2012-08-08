package model.map.events;

import java.util.HashMap;
import java.util.Map;

/**
 * The kinds of events we know about.
 *
 * @author Jonathan Lovelace
 */
public enum EventKind {
	/**
	 * "Nothing of interest here ...". Useful primarily as an alternative to
	 * null.
	 * @deprecated Should never actually be used.
	 */
	@Deprecated
	Nothing("nothing"),
	/**
	 * A mineral vein.
	 * @deprecated We don't treat MineralEvents as Events anymore.
	 */
	@Deprecated
	Mineral("mineral"),
	/**
	 * A fortification.
	 */
	Fortification("fortification"),
	/**
	 * A town.
	 */
	Town("town"),
	/**
	 * Signs of a long-ago battle.
	 * @deprecated We don't treat BattlefieldEvents as Events anymore.
	 */
	@Deprecated
	Battlefield("battlefield"),
	/**
	 * A city.
	 */
	City("city"),
	/**
	 * A stone deposit.
	 * @deprecated We don't treat StoneEvents as events anymore.
	 */
	@Deprecated
	Stone("stone"),
	/**
	 * Caves beneath the tile.
	 * @deprecated We don't treat CaveEvents as Events anymore.
	 */
	@Deprecated
	Caves("cave");
	/**
	 * A mapping from strings to EventKinds.
	 */
	private static final Map<String, EventKind> EK_MAP = new HashMap<String, EventKind>();
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
	private EventKind(final String string) {
		str = string;
	}

	static {
		for (final EventKind ek : values()) {
			EK_MAP.put(ek.str, ek);
		}
	}

	/**
	 * @param string a string representing an EventKind
	 *
	 * @return the EventKind it represents
	 */
	public static EventKind parseEventKind(final String string) {
		return EK_MAP.get(string);
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
