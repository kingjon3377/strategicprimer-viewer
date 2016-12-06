package model.map;

import java.util.stream.Stream;
import model.map.fixtures.Ground;
import model.map.fixtures.terrain.Forest;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A possible replacement for IMap, aiming to completely hide the implementation details.
 * For example, if Tile objects are still used, they shouldn't be exposed; instead,
 * callers ask for the tile type, rivers, forest, mountain, fixtures, etc., mapped to by a
 * given Point. Mutator methods (including those used in constructing the map object) are
 * out of the scope of this interface.
 *
 * We also include several of the features that MapView added to the original SPMap.
 *
 * We extend Comparable so we can put one of these in a Pair.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface IMapNG
		extends Subsettable<@NonNull IMapNG>, Comparable<@NonNull IMapNG> {
	/**
	 * @return the map version and dimensions
	 */
	MapDimensions dimensions();

	/**
	 * @return a view of the players in the map.
	 */
	Iterable<Player> players();

	/**
	 * @return a view of the locations on the map
	 */
	Iterable<Point> locations();

	/**
	 * @return a view of the locations on the map
	 */
	Stream<Point> locationStream();

	/**
	 * @param location a location
	 * @return the "base terrain" at that location
	 */
	TileType getBaseTerrain(Point location);

	/**
	 * @param location a location
	 * @return whether that location is mountainous
	 */
	boolean isMountainous(Point location);

	/**
	 * @param location a location
	 * @return a view of the river directions, if any, at that location
	 */
	Iterable<River> getRivers(Point location);

	/**
	 * Implementations should aim to have only the "main" forest here, and any "extra"
	 * forest Fixtures in the "other fixtures" collection.
	 *
	 * @param location a location
	 * @return the forest (if any) at that location; null if there is none
	 */
	@Nullable Forest getForest(Point location);

	/**
	 * Implementations should aim to have only the "main" Ground here, and any exposed or
	 * otherwise "extra" Fixtures in the "other fixtures" collection.
	 *
	 * @param location a location
	 * @return the Ground at that location; null if there is none
	 */
	@Nullable Ground getGround(Point location);

	/**
	 * @param location a location
	 * @return a view of any fixtures on the map that aren't covered in the other
	 * querying
	 * methods.
	 */
	Iterable<@NonNull TileFixture> getOtherFixtures(Point location);

	/**
	 * @param location a location
	 * @return a stream of any fixtures on the map that aren't covered in the other
	 * querying methods.
	 */
	Stream<@NonNull TileFixture> streamOtherFixtures(Point location);

	/**
	 * @return the current turn
	 */
	int getCurrentTurn();

	/**
	 * @return the current player
	 */
	Player getCurrentPlayer();

	/**
	 * @param zero whether to "zero" sensitive data (probably just DCs)
	 * @return a copy of this map
	 */
	IMapNG copy(boolean zero);

	/**
	 * A location is empty if it has no terrain, no Ground, no Forest, no rivers, and no
	 * fixtures.
	 *
	 * @param location a location
	 * @return whether the map is empty at that location
	 */
	default boolean isLocationEmpty(final Point location) {
		return TileType.NotVisible == getBaseTerrain(location) &&
					   !isMountainous(location) && getGround(location) == null &&
					   getForest(location) == null &&
					   !getRivers(location).iterator().hasNext() &&
					   streamOtherFixtures(location).noneMatch(x -> true);
	}

	/**
	 * A map is empty if *every* location is empty. Note that calculating this can be
	 * quite expensive!
	 *
	 * @return whether the map is entirely empty
	 */
	default boolean isEmpty() {
		return locationStream().allMatch(this::isLocationEmpty);
	}
}
