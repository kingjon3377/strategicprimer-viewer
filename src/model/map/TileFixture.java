package model.map;

/**
 * Something that can go on a tile.
 *
 * @author Jonathan Lovelace
 */
public interface TileFixture extends IFixture, Comparable<TileFixture> {
	// Marker interface; also, TODO: what members should this have?
	/**
	 * TODO: This should be user-configurable.
	 *
	 * @return a z-value for determining which fixture should be uppermost on a
	 *         tile.
	 */
	int getZValue();
}
