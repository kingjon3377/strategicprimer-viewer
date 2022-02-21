package common.map.fixtures.towns;

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

	@Override public String toString() {
		return string;
	}

	public static TownSize parseTownSize(final String size) {
		switch (size) {
		case "small":
			return Small;
		case "medium":
			return Medium;
		case "large":
			return Large;
		default:
			throw new IllegalArgumentException(String.format(
				"Failed to parse TownSize from '%s'", size));
		}
	}
}
