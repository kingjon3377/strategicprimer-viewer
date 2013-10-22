package model.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	Tundra("tundra", 1, 2),
	/**
	 * Desert.
	 */
	Desert("desert", 1, 2),
	/**
	 * Mountain. Starting in version 2, this is represented as a plain, steppe,
	 * or desert plus a mountain on the tile.
	 *
	 * @deprecated Format version 1 only
	 */
	@Deprecated
	Mountain("mountain", 1),
	/**
	 * Boreal forest. Starting in version 2, this is represented as a steppe
	 * plus a forest.
	 *
	 * @deprecated Format version 1 only
	 */
	@Deprecated
	BorealForest("boreal_forest", 1),
	/**
	 * Temperate forest. Starting in version 2, this is represented as a plain
	 * plus a forest.
	 *
	 * @deprecated Format version 1 only
	 */
	@Deprecated
	TemperateForest("temperate_forest", 1),
	/**
	 * Ocean.
	 */
	Ocean("ocean", 1, 2),
	/**
	 * Plains.
	 */
	Plains("plains", 1, 2),
	/**
	 * Jungle.
	 */
	Jungle("jungle", 1, 2),
	/**
	 * Steppe. This is like plains, but higher-latitude and colder. Beginning in
	 * version 2, a temperate forest is plains plus forest, and a boreal forest
	 * is steppe plus forest, while a mountain is either a desert, a plain, or a
	 * steppe plus a mountain.
	 */
	Steppe("steppe", 2),
	/**
	 * Not visible.
	 */
	NotVisible("not_visible", 1, 2);
	/**
	 * The map versions that support the tile type as such. (For example,
	 * version 2 and later replace forests as a tile type with forests as
	 * something on the tile.)
	 */
	private final List<Integer> versions;
	/**
	 * A cache of the lists of types supported by particular versions.
	 * Initializer in the static block below because here it made the line too
	 * long.
	 */
	private static final Map<Integer, Set<TileType>> VALS_BY_VER = new HashMap<>();

	/**
	 * TODO: Write test code using this.
	 *
	 * @param ver a map version
	 * @return a list of all tile-types that version supports.
	 */
	public static Set<TileType> valuesForVersion(final int ver) {
		final Integer boxedVer = Integer.valueOf(ver);
		synchronized (VALS_BY_VER) {
			if (!VALS_BY_VER.containsKey(boxedVer)) {
				final Set<TileType> set = EnumSet.noneOf(TileType.class);
				for (final TileType type : values()) {
					if (type.isSupportedByVersion(ver)) {
						set.add(type);
					}
				}
				VALS_BY_VER.put(boxedVer, set);
			}
		}
		final Set<TileType> retval = Collections.unmodifiableSet(VALS_BY_VER.get(boxedVer));
		assert retval != null;
		return retval;
	}

	/**
	 * Constructor.
	 *
	 * @param descr a descriptive string to represent the type.
	 * @param vers the map versions that support the tile type.
	 */
	private TileType(final String descr, final int... vers) {
		versions = new ArrayList<>();
		for (final int ver : vers) {
			versions.add(Integer.valueOf(ver));
		}
		desc = descr;
	}

	/**
	 * @param ver a map version
	 * @return whether that version supports this tile type.
	 */
	public boolean isSupportedByVersion(final int ver) {
		return versions.contains(Integer.valueOf(ver));
	}

	/**
	 * A descriptive string to represent the type.
	 */
	private final String desc;
	/**
	 * The mapping from descriptive strings to tile types. Used to make
	 * multiple-return-points warnings go away.
	 */
	private static final Map<String, TileType> TILE_TYPE_MAP = new HashMap<>(); // NOPMD
	static {
		for (final TileType type : values()) {
			TILE_TYPE_MAP.put(type.toXML(), type);
		}
	}

	/**
	 * Parse a tile terrain type.
	 *
	 * @param string A string describing the terrain
	 *
	 * @return the terrain type
	 */
	public static TileType getTileType(final String string) {
		if (TILE_TYPE_MAP.containsKey(string)) {
			final TileType retval = TILE_TYPE_MAP.get(string);
			assert retval != null;
			return retval;
		} // else
		throw new IllegalArgumentException("Unrecognized terrain type string "
				+ string);
	}

	/**
	 * @return the XML representation of the tile type.
	 */
	public String toXML() {
		return desc;
	}
}
