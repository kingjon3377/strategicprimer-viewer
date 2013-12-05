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

	/**
	 * @return a String describing all members of a kind of fixture.
	 */
	String plural();

	/**
	 * @return a *short*, no more than one line and preferably no more than two
	 *         dozen characters, description of the fixture, suitable for saying
	 *         what it is when an explorer happens on it.
	 */
	String shortDesc();
}
