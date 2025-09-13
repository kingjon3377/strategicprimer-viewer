package common.map.fixtures.towns;

import lovelace.util.EnumParser;
import lovelace.util.ThrowingFunction;

/**
 * Possible statuses of towns, fortifications, and cities.
 */
public enum TownStatus {
	Active("active"),
	Abandoned("abandoned"),
	Ruined("ruined"),
	Burned("burned");

	TownStatus(final String str) {
		string = str;
	}

	private final String string;

	@Override
	public String toString() {
		return string;
	}

	private static final ThrowingFunction<String, TownStatus, IllegalArgumentException> PARSER =
			new EnumParser<>(TownStatus.class, values().length);

	public static TownStatus parse(final String status) {
		return PARSER.apply(status);
	}
}
