package model.map;

import model.map.fixtures.Ground;
import model.map.fixtures.terrain.Forest;

/**
 * A possible replacement for IMap, aiming to completely hide the implementation
 * details. For example, if Tile objects are still used, they shouldn't be
 * exposed; instead, callers ask for the tile type, rivers, forest, mountain,
 * fixtures, etc., mapped to by a given Point. Mutator methods (including those
 * used in constructing the map object) are out of the scope of this interface.
 *
 * We also include several of the features that MapView added to the original SPMap.
 *
 * We extend Comparable so we can put one of these in a Pair.
 *
 * TODO: Write tests.
 *
 * TODO: Write a proper implementation (not using MapView), and serialization for it.
 *
 * @author Jonathan Lovelace
 *
 */
public interface IMapNG extends XMLWritable, Subsettable<IMapNG>, Comparable<IMapNG> {
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
	 * @param location a location
	 * @return the "base terrain" at that location
	 */
	TileType getBaseTerrain(final Point location);
	/**
	 * @param location a location
	 * @return whether that location is mountainous
	 */
	boolean isMountainous(final Point location);
	/**
	 * @param location a location
	 * @return a view of the river directions, if any, at that location
	 */
	Iterable<River> getRivers(final Point location);

	/**
	 * Implementations should aim to have only the "main" forest here, and any
	 * "extra" forest Fixtures in the "et cetera" collection.
	 *
	 * @param location a location
	 * @return the forest (if any) at that location
	 */
	Forest getForest(final Point location);

	/**
	 * Implementations should aim to have only the "main" Ground here, and any
	 * exposed or otherwise "extra" Fixtures in the "et cetera" collection.
	 * @param location a location
	 * @return the Ground at that location
	 */
	Ground getGround(final Point location);
	/**
	 * @param location a location
	 * @return a view of any fixtures on the map that aren't covered in the other querying methods.
	 */
	Iterable<TileFixture> getOtherFixtures(final Point location);
	/**
	 * @return the current turn
	 */
	int getCurrentTurn();
	/**
	 * @return the current player
	 */
	Player getCurrentPlayer();
}
