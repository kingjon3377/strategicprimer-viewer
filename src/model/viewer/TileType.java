package model.viewer;

import java.util.HashMap;

/**
 * Possible tile types.
 * 
 * @author Jonathan Lovelace
 * 
 */
public enum TileType {
	/**
	 * Tundra.
	 */
	Tundra,
	/**
	 * Desert.
	 */
	Desert,
	/**
	 * Mountain.
	 */
	Mountain,
	/**
	 * Boreal forest.
	 */
	BorealForest,
	/**
	 * Temperate forest.
	 */
	TemperateForest,
	/**
	 * Ocean.
	 */
	Ocean,
	/**
	 * Plains.
	 */
	Plains,
	/**
	 * Jungle.
	 */
	Jungle,
	/**
	 * Not visible.
	 */
	NotVisible;
	/**
	 * The mapping from descriptive strings to tile types. Used to make
	 * multiple-return-points warnings go away.
	 */
	private static final HashMap<String, TileType> TILE_TYPE_MAP = new HashMap<String, TileType>();// NOPMD

	static {
		TILE_TYPE_MAP.put("tundra", TileType.Tundra);
		TILE_TYPE_MAP.put("temperate_forest", TileType.TemperateForest);
		TILE_TYPE_MAP.put("boreal_forest", TileType.BorealForest);
		TILE_TYPE_MAP.put("ocean", TileType.Ocean);
		TILE_TYPE_MAP.put("desert", TileType.Desert);
		TILE_TYPE_MAP.put("plains", TileType.Plains);
		TILE_TYPE_MAP.put("jungle", TileType.Jungle);
		TILE_TYPE_MAP.put("mountain", TileType.Mountain);
	}

	/**
	 * Parse a tile terrain type.
	 * 
	 * @param string
	 *            A string describing the terrain
	 * @return the terrain type
	 */
	public static TileType getTileType(final String string) {
		if (TILE_TYPE_MAP.containsKey(string)) {
			return TILE_TYPE_MAP.get(string);
		} // else
		throw new IllegalArgumentException("Unrecognized terrain type string");
	}
}