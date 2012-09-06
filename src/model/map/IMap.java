package model.map;

/**
 * An interface for the map and any wrappers around it.
 *
 * @author Jonathan Lovelace
 *
 */
// ESCA-JAVA0237:
public interface IMap extends XMLWritable, Subsettable<IMap>, Comparable<IMap> {

	/**
	 * @return the map version
	 */
	int getVersion();

	/**
	 *
	 * @return how many rows the map has.
	 */
	int rows();

	/**
	 *
	 * @return how many columns the map has
	 */
	int cols();

	/**
	 * Add a player to the game.
	 *
	 * @param player the player to add
	 */
	void addPlayer(final Player player);

	/**
	 * @param point a point
	 * @return the tile at those coordinates
	 */
	Tile getTile(final Point point);

	/**
	 *
	 * @return the players in the map
	 */
	PlayerCollection getPlayers();

	/**
	 * We need this for subset calculations if nothing else.
	 *
	 * @return the collection of tiles.
	 */
	TileCollection getTiles();
}
