package model.map;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import util.NullCleaner;

import static java.util.Collections.unmodifiableSet;

/**
 * Possible tile types.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
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
	 * Mountain. Starting in version 2, this is represented as a plain, steppe, or desert
	 * plus a mountain on the tile.
	 *
	 * @deprecated Format version 1 only
	 */
	@Deprecated
	Mountain("mountain", 1),
	/**
	 * Boreal forest. Starting in version 2, this is represented as a steppe plus a
	 * forest.
	 *
	 * @deprecated Format version 1 only
	 */
	@Deprecated
	BorealForest("boreal_forest", 1),
	/**
	 * Temperate forest. Starting in version 2, this is represented as a plain plus a
	 * forest.
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
	 * Steppe. This is like plains, but higher-latitude and colder. Beginning in version
	 * 2, a temperate forest is plains plus forest, and a boreal forest is steppe plus
	 * forest, while a mountain is either a desert, a plain, or a steppe plus a mountain.
	 */
	Steppe("steppe", 2),
	/**
	 * Not visible.
	 */
	NotVisible("not_visible", 1, 2);
	/**
	 * A cache of the lists of types supported by particular versions. Initializer in the
	 * static block below because here it made the line too long.
	 */
	private static final Map<Integer, Set<TileType>> VALS_BY_VER = new HashMap<>();
	/**
	 * The mapping from descriptive strings to tile types. Used to make
	 * multiple-return-points warnings go away.
	 */
	private static final Map<String, TileType> CACHE = new HashMap<>();
	/**
	 * The map versions that support the tile type as such. (For example, version 2 and
	 * later replace forests as a tile type with forests as something on the tile.)
	 */
	private final List<Integer> versions;
	/**
	 * A descriptive string to represent the type.
	 */
	private final String desc;

	static {
		for (final TileType type : values()) {
			CACHE.put(type.toXML(), type);
		}
	}

	/**
	 * Constructor.
	 *
	 * @param description        a descriptive string to represent the type.
	 * @param supportingVersions the map versions that support the tile type.
	 */
	TileType(final String description, final int... supportingVersions) {
		versions = new ArrayList<>();
		for (final int ver : supportingVersions) {
			versions.add(NullCleaner.assertNotNull(Integer.valueOf(ver)));
		}
		desc = description;
	}

	/**
	 * TODO: Write test code using this.
	 *
	 * @param ver a map version
	 * @return a list of all tile-types that version supports.
	 */
	@SuppressWarnings("SynchronizationOnStaticField")
	public static Iterable<TileType> valuesForVersion(final int ver) {
		final Integer boxedVer = NullCleaner.assertNotNull(Integer.valueOf(ver));
		synchronized (VALS_BY_VER) {
			if (!VALS_BY_VER.containsKey(boxedVer)) {
				final Set<@NonNull TileType> set =
						NullCleaner.assertNotNull(
								EnumSet.copyOf(Stream.of(values())
													   .filter(type -> type
																			   .isSupportedByVersion(
																					   ver))
													   .collect(Collectors.toSet())));
				VALS_BY_VER.put(boxedVer, set);
			}
		}
		return NullCleaner.assertNotNull(unmodifiableSet(VALS_BY_VER.get(boxedVer)));
	}

	/**
	 * Parse a tile terrain type.
	 *
	 * @param description A string describing the terrain
	 * @return the terrain type
	 */
	public static TileType getTileType(final String description) {
		if (CACHE.containsKey(description)) {
			return NullCleaner.assertNotNull(CACHE.get(description));
		} // else
		throw new IllegalArgumentException("Unrecognized terrain type string " +
												   description);
	}

	/**
	 * @param ver a map version
	 * @return whether that version supports this tile type.
	 */
	public boolean isSupportedByVersion(final int ver) {
		return versions.contains(Integer.valueOf(ver));
	}

	/**
	 * @return the XML representation of the tile type.
	 */
	public String toXML() {
		return desc;
	}
}
