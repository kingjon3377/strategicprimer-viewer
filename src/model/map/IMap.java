package model.map;

import com.sun.istack.internal.NotNull;

/**
 * An interface for the map and any wrappers around it.
 *
 * @author Jonathan Lovelace
 *
 */
// ESCA-JAVA0237:
public interface IMap extends Subsettable<IMap>, Comparable<IMap> {
	/**
	 * @return The map's dimensions and version.
	 */
	@NotNull
	MapDimensions getDimensions();

	/**
	 * @param point a point
	 * @return the tile at those coordinates
	 */
	@NotNull
	ITile getTile(@NotNull  Point point);

	/**
	 *
	 * @return the players in the map
	 */
	@NotNull
	IPlayerCollection getPlayers();

	/**
	 * We need this for subset calculations if nothing else.
	 *
	 * @return the collection of tiles.
	 */
	@NotNull
	ITileCollection getTiles();
}
