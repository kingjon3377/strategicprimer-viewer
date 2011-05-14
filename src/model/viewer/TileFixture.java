package model.viewer;

/**
 * Something that can go on a tile.
 * 
 * @author Jonathan Lovelace
 */
public interface TileFixture {
	/**
	 * @return what tile the Fixture is on.
	 */
	Tile getLocation();
}
