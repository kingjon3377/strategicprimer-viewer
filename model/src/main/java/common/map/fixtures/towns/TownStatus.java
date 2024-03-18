package common.map.fixtures.towns;

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

	public static TownStatus parse(final String status) {
		return switch (status) {
			case "active" -> Active;
			case "abandoned" -> Abandoned;
			case "burned" -> Burned;
			case "ruined" -> Ruined;
			default -> throw new IllegalArgumentException("Failed to parse TownStatus from '%s'".formatted(status));
		};
	}
}
