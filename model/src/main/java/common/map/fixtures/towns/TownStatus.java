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
		switch (status) {
		case "active":
			return Active;
		case "abandoned":
			return Abandoned;
		case "burned":
			return Burned;
		case "ruined":
			return Ruined;
		default:
			throw new IllegalArgumentException(String.format(
				"Failed to parse TownStatus from '%s'", status));
		}
	}
}
