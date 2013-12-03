package model.map;
/**
 * An interface for mutable collections of tiles.
 * @author Jonathan Lovelace
 */
public interface IMutableTileCollection extends ITileCollection {

	/**
	 * Add a Tile to the map.
	 *
	 * @param tile the tile to add.
	 * @param point the point at which to add it
	 */
	void addTile(Point point, ITile tile);

}
