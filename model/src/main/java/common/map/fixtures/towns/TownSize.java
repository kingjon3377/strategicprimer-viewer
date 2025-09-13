package common.map.fixtures.towns;

import lovelace.util.EnumParser;
import lovelace.util.ThrowingFunction;

/**
 * Sizes of towns, fortifications, and cities.
 */
public enum TownSize {
	Small("small"),
	Medium("medium"),
	Large("large");

	TownSize(final String str) {
		string = str;
	}

	private final String string;

	@Override
	public String toString() {
		return string;
	}

	private static final ThrowingFunction<String, TownSize, IllegalArgumentException> PARSER =
			new EnumParser<>(TownSize.class, 3);

	public static TownSize parseTownSize(final String size) {
		return PARSER.apply(size);
	}
}
