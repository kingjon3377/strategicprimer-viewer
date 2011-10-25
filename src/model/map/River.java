package model.map;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

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
	private static final Map<String, River> RIVER_MAP = new HashMap<String, River>();
	/**
	 * Mapping from directions to descriptive strings.
	 */
	private static final Map<River, String> STR_MAP = new EnumMap<River, String>(River.class);
	/**
	 * Add a pairing to the maps.
	 * @param str the descriptive string
	 * @param river the direction
	 */
	private static void addToMaps(final String str, final River river) {
		RIVER_MAP.put(str, river);
		STR_MAP.put(river, str);
	}
	static {
		addToMaps("north", River.North);
		addToMaps("south", River.South);
		addToMaps("east", River.East);
		addToMaps("west", River.West);
		addToMaps("lake", River.Lake);
	}

	/**
	 * Parse a river direction.
	 * 
	 * @param string
	 *            a string giving the direction
	 * 
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
