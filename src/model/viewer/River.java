package model.viewer;

import java.util.HashMap;

/**
 * If a tile has a river, it could be in any one of several directions. This
 * class enumerates those. Tiles should have a <em>set</em> of these.
 * 
 * At present we'll just cover the four cardinal directions.
 * 
 * @author Jonathan Lovelace
 * 
 */
public enum River {
	/**
	 * North.
	 */
	North,
	/**
	 * East.
	 */
	East,
	/**
	 * South.
	 */
	South,
	/**
	 * West.
	 */
	West,
	/**
	 * A lake (to be depicted as being in the center of the tile).
	 */
	Lake;
	/**
	 * Mapping from descriptive strings to directions.
	 */
	private static final HashMap<String, River> RIVER_MAP = new HashMap<String, River>();

	static {
		RIVER_MAP.put("north", River.North);
		RIVER_MAP.put("south", River.South);
		RIVER_MAP.put("east", River.East);
		RIVER_MAP.put("west", River.West);
		RIVER_MAP.put("lake", River.Lake);
	}

	/**
	 * Parse a river direction
	 * 
	 * @param string
	 *            a string giving the direction
	 * @return the river direction
	 */
	public static River getRiver(final String string) {
		if (RIVER_MAP.containsKey(string)) {
			return RIVER_MAP.get(string);
		} else {
			throw new IllegalArgumentException(
					"Unrecognized river direction string");
		}
	}
}
