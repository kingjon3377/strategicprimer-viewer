package common.map;

import java.text.ParseException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * If a tile has a river, it could be in any one of several directions. This
 * class enumerates those directions. Tiles should have a *set* of these.
 *
 * At present we just cover the four cardinal directions.
 *
 * TODO: Extend to cover other directions?
 */
public enum River {
	North("north"),
	East("east"),
	South("south"),
	West("west"),
	/**
	 * A lake (to be depicted as being in the center of the tile).
	 */
	Lake("lake");

	River(final String description) {
		this.description = description;
		string = description.substring(0, 1).toUpperCase() + description.substring(1);

	}

	/**
	 * A descriptive string representing the direction, suitable for use in XML as well.
	 */
	private final String description;

	/**
	 * A descriptive string representing the direction, suitable for use in XML as well.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * The description with the first letter capitalized.
	 */
	private final String string;

	@Override
	public String toString() {
		return string;
	}

	/**
	 * Get the river matching the given description.
	 */
	public static River parse(final String description) throws ParseException {
		Optional<River> retval = Stream.of(values())
			.filter((r) -> description.equals(r.description)).findAny();
		if (retval.isPresent()) {
			return retval.get();
		} else {
			throw new ParseException(
				String.format("Failed to parse River from '%s'", description), -1);
		}
	}
}
