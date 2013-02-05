package model.map;

import java.io.Serializable;

/**
 * An interface for the map and any wrappers around it.
 *
 * @author Jonathan Lovelace
 *
 */
// ESCA-JAVA0237:
public interface IMap extends XMLWritable, Subsettable<IMap>, Comparable<IMap>, Serializable {
	/**
	 * @return The map's dimensions and version.
	 */
	MapDimensions getDimensions();

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
