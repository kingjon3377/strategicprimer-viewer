package model.map;


/**
 * An interface to specify all the mutator methods that were in IMap, and to
 * explicitly return mutable collections.
 *
 * @author Jonathan Lovelace
 */
public interface IMutableMap extends IMap {
	/**
	 * Add a player to the game.
	 *
	 * @param player the player to add
	 */
	void addPlayer(Player player);
	/**
	 * @return a mutable view of the players in the map
	 */
	@Override
	IMutablePlayerCollection getPlayers();
	/**
	 * @return a mutable view of the tiles in the map
	 */
	@Override
	IMutableTileCollection getTiles();
	/**
	 * @param point a point
	 * @return the tile at those coordinates
	 */
	@Override
	IMutableTile getTile(Point point);
}
