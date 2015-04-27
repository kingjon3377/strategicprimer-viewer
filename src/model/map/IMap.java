package model.map;


/**
 * An interface for the map and any wrappers around it.
 *
 * @author Jonathan Lovelace
 * @deprecated The old map API is deprecated in the mapng branch
 */
// ESCA-JAVA0237:
@Deprecated
public interface IMap extends Subsettable<IMap>, Comparable<IMap> {
	/**
	 * @return The map's dimensions and version.
	 */
	MapDimensions getDimensions();

	/**
	 * @param point a point
	 * @return the tile at those coordinates
	 */
	ITile getTile(Point point);

	/**
	 *
	 * @return the players in the map
	 */
	IPlayerCollection getPlayers();

	/**
	 * We need this for subset calculations if nothing else.
	 *
	 * @return the collection of tiles.
	 */
	ITileCollection getTiles();
}
