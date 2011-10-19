package model.map.events;

import java.util.HashMap;
import java.util.Map;

/**
 * Possible status of towns, fortifications, and cities.
 */
public enum TownStatus {
	/**
	 * The town is inhabited.
	 */
	Active("active"),
	/**
	 * The town is abandoned.
	 */
	Abandoned("abandoned"),
	/**
	 * The town is burned-out.
	 */
	Burned("burned"),
	/**
	 * The town is in ruins.
	 */
	Ruined("ruined");
	/**
	 * A mapping from strings to TownStatus.
	 */
	private static final Map<String, TownStatus> TST_MAP = new HashMap<String, TownStatus>();
	/**
	 * A string representing the TownStatus.
	 */
	private final String str;

	/**
	 * Constructor.
	 * 
	 * @param string
	 *            a string representing the status.
	 */
	private TownStatus(final String string) {
		str = string;
	}

	static {
		for (final TownStatus ts : values()) {
			TST_MAP.put(ts.str, ts);
		}
	}

	/**
	 * @param string
	 *            a string representing a TownStatus
	 * @return the TownStatus it represents
	 */
	public static TownStatus parseTownStatus(final String string) {
		return TST_MAP.get(string);
	}

	/**
	 * @return a string representation of the status
	 */
	@Override
	public String toString() {
		return str;
	}
}
