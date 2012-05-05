package model.map;

/**
 * Something that can go on a tile.
 * 
 * @author Jonathan Lovelace
 */
public interface TileFixture extends XMLWritable, Comparable<TileFixture> {
	// Marker interface; also, TODO: what members should this have?
	/**
	 * TODO: This should be user-configurable.
	 * @return a z-value for determining which fixture should be uppermost on a tile.
	 */
	int getZValue();
	/**
	 * @return an ID (UID for most fixtures, though perhaps not for things like
	 *         mountains and hills) for the fixture.
	 */
	long getID();
	/**
	 * @param fix a fixture
	 * @return whether it's equal, ignoring ID (and DC for events), to this one 
	 */
	boolean equalsIgnoringID(TileFixture fix);
}
