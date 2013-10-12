package model.map;

import java.io.Serializable;

import com.sun.istack.internal.NotNull;

/**
 * An interface for the map and any wrappers around it.
 *
 * @author Jonathan Lovelace
 *
 */
// ESCA-JAVA0237:
public interface IMap extends Subsettable<IMap>, Comparable<IMap>, Serializable {
	/**
	 * @return The map's dimensions and version.
	 */
	@NotNull MapDimensions getDimensions();

	/**
	 * Add a player to the game.
	 *
	 * @param player the player to add
	 */
	void addPlayer(@NotNull final Player player);

	/**
	 * @param point a point
	 * @return the tile at those coordinates
	 */
	@NotNull Tile getTile(@NotNull final Point point);

	/**
	 *
	 * @return the players in the map
	 */
	@NotNull PlayerCollection getPlayers();

	/**
	 * We need this for subset calculations if nothing else.
	 *
	 * @return the collection of tiles.
	 */
	@NotNull TileCollection getTiles();
}
