package model.map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import model.map.fixtures.Ground;
import model.map.fixtures.terrain.Forest;

/**
 * A possible replacement for IMap, aiming to completely hide the implementation
 * details. For example, if Tile objects are still used, they shouldn't be
 * exposed; instead, callers ask for the tile type, rivers, forest, mountain,
 * fixtures, etc., mapped to by a given Point. Mutator methods (including those
 * used in constructing the map object) are out of the scope of this interface.
 *
 * We also include several of the features that MapView added to the original
 * SPMap.
 *
 * We extend Comparable so we can put one of these in a Pair.
 *
 * TODO: Write tests.
 *
 * TODO: Write a proper implementation (not using MapView), and serialization
 * for it.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public interface IMapNG extends Subsettable<IMapNG>, Comparable<IMapNG> {
	/**
	 * @return the map version and dimensions
	 */
	MapDimensions dimensions();

	/**
	 * @return a view of the players in the map.
	 */
	Iterable<@NonNull Player> players();

	/**
	 * @return a view of the locations on the map
	 */
	Iterable<@NonNull Point> locations();

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
	Iterable<@NonNull River> getRivers(Point location);

	/**
	 * Implementations should aim to have only the "main" forest here, and any
	 * "extra" forest Fixtures in the "et cetera" collection.
	 *
	 * @param location a location
	 * @return the forest (if any) at that location; null if there is none
	 */
	@Nullable
	Forest getForest(Point location);

	/**
	 * Implementations should aim to have only the "main" Ground here, and any
	 * exposed or otherwise "extra" Fixtures in the "et cetera" collection.
	 *
	 * @param location a location
	 * @return the Ground at that location; null if there is none
	 */
	@Nullable
	Ground getGround(Point location);

	/**
	 * @param location a location
	 * @return a view of any fixtures on the map that aren't covered in the
	 *         other querying methods.
	 */
	Iterable<@NonNull TileFixture> getOtherFixtures(Point location);

	/**
	 * @return the current turn
	 */
	int getCurrentTurn();

	/**
	 * @return the current player
	 */
	Player getCurrentPlayer();
}
