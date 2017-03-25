package model.map;

import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import model.map.fixtures.Ground;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.terrain.Forest;

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
	 * The dimensions of the map.
	 * @return the map version and dimensions
	 */
	MapDimensions dimensions();

	/**
	 * The players in the map.
	 * @return a view of the players in the map.
	 */
	Iterable<Player> players();

	/**
	 * The locations in the map.
	 * @return a view of the locations on the map
	 */
	Iterable<Point> locations();

	/**
	 * A stream of the locations in the map.
	 * @return a view of the locations on the map
	 */
	Stream<Point> locationStream();

	/**
	 * The base terrain at the given point.
	 * @param location a location
	 * @return the "base terrain" at that location
	 */
	TileType getBaseTerrain(Point location);

	/**
	 * Whether the given location is mountainous.
	 * @param location a location
	 * @return whether that location is mountainous
	 */
	boolean isMountainous(Point location);

	/**
	 * The rivers, if any, at the given location.
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
	 * Any fixtures, other than the main ground and forest, at the given location.
	 * @param location a location
	 * @return a view of any fixtures on the map that aren't covered in the other
	 * querying methods.
	 */
	Iterable<@NonNull TileFixture> getOtherFixtures(Point location);

	/**
	 * A stream of any fixtures, other than the main ground and forest, at the given
	 * location.
	 * @param location a location
	 * @return a stream of any fixtures on the map that aren't covered in the other
	 * querying methods.
	 */
	Stream<@NonNull TileFixture> streamOtherFixtures(Point location);

	/**
	 * A Stream over *all* fixtures on a tile, including its primary forest and Ground,
	 * if any.
	 * @param location a location
	 * @return a Stream of all fixtures there
	 */
	@SuppressWarnings("null")
	default Stream<@NonNull TileFixture> streamAllFixtures(final Point location) {
		return Stream.concat(Stream.of(getGround(location), getForest(location))
									 .filter(Objects::nonNull),
				streamOtherFixtures(location));
	}
	/**
	 * The current turn.
	 * @return the current turn
	 */
	int getCurrentTurn();

	/**
	 * The current player.
	 * @return the current player
	 */
	Player getCurrentPlayer();

	/**
	 * Clone the map.
	 * @param zero whether to "zero" sensitive data (probably just DCs)
	 * @param player the player for whom the map is being prepared, or null if none
	 * @return a copy of this map
	 */
	IMapNG copy(boolean zero, final @Nullable Player player);

	/**
	 * A location is empty if it has no terrain, no Ground, no Forest, no rivers, and no
	 * fixtures.
	 *
	 * @param location a location
	 * @return whether the map is empty at that location
	 */
	default boolean isLocationEmpty(final Point location) {
		return TileType.NotVisible == getBaseTerrain(location) &&
					   !isMountainous(location) &&
					   !getRivers(location).iterator().hasNext() &&
					   streamAllFixtures(location).noneMatch(x -> true);
	}

	/**
	 * A stream of the players in the map.
	 * @return a stream of the players in the map
	 */
	default Stream<Player> streamPlayers() {
		return StreamSupport.stream(players().spliterator(), false);
	}
	/**
	 * Strict-subset calculations should skip caches, text fixtures, and animal tracks.
	 * @param fix a fixture
	 * @return whether strict-subset calculations should skip it.
	 */
	default boolean shouldSkip(final TileFixture fix) {
		return (fix instanceof CacheFixture) || (fix instanceof TextFixture) ||
					   ((fix instanceof Animal) && ((Animal) fix).isTraces());
	}
}
