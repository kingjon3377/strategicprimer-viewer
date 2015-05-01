package model.map;
/**
 * An interface for a collection of tiles.
 * @author Jonathan Lovelace
 * @deprecated the old map API is deprecated in this branch
 */
@Deprecated
public interface ITileCollection extends Iterable<Point>,
		Subsettable<ITileCollection> {

	/**
	 * Get the specified point. If it isn't in the collection, add a new "empty"
	 * one there and return that. This should never return null.
	 *
	 * @param point a point
	 *
	 * @return the tile at that point, or a new "empty" tile at that point.
	 */
	ITile getTile(Point point);

	/**
	 * @param point a point
	 * @return whether there's a non-empty tile at that point
	 */
	boolean hasTile(Point point);

}
