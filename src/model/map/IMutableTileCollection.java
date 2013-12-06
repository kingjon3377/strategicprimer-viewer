package model.map;

/**
 * An interface for mutable collections of tiles. To simplify calling code, a
 * mutable collection of tiles can only contain mutable tiles.
 *
 * @author Jonathan Lovelace
 */
public interface IMutableTileCollection extends ITileCollection {

	/**
	 * Add a Tile to the map.
	 *
	 * @param tile the tile to add.
	 * @param point the point at which to add it
	 */
	void addTile(Point point, IMutableTile tile);
	/**
	 * Get the specified point. If it isn't in the collection, add a new "empty"
	 * one there and return that. This should never return null.
	 *
	 * Since this is a mutable collection, only return mutable tiles.
	 *
	 * @param point a point
	 *
	 * @return the tile at that point, or a new "empty" tile at that point.
	 */
	@Override
	IMutableTile getTile(Point point);
}
