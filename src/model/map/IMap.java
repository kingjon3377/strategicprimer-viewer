package model.map;

import org.eclipse.jdt.annotation.NonNull;

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
	@NonNull
	MapDimensions getDimensions();

	/**
	 * @param point a point
	 * @return the tile at those coordinates
	 */
	@NonNull
	ITile getTile(@NonNull final Point point);

	/**
	 *
	 * @return the players in the map
	 */
	@NonNull
	IPlayerCollection getPlayers();

	/**
	 * We need this for subset calculations if nothing else.
	 *
	 * @return the collection of tiles.
	 */
	@NonNull
	ITileCollection getTiles();
}
