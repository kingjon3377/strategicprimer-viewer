package common.map.fixtures.towns;

/**
 * Possible statuses of towns, fortifications, and cities.
 */
public enum TownStatus {
	Active("active"),
	Abandoned("abandoned"),
	Ruined("ruined"),
	Burned("burned");
	
	private TownStatus(String str) {
		string = str;
	}

	private final String string;

	@Override
	public String toString() {
		return string;
	}

	public static TownStatus parse(String status) {
		switch (status) {
		case "active":
			return TownStatus.Active;
		case "abandoned":
			return TownStatus.Abandoned;
		case "burned":
			return TownStatus.Burned;
		case "ruined":
			return TownStatus.Ruined;
		default:
			throw new IllegalArgumentException(String.format(
				"Failed to parse TownStatus from '%s'", status));
		}
	}
}
