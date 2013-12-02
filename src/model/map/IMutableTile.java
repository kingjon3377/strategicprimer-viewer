package model.map;
/**
 * An interface for modifying, not just querying, the contents of a tile.
 * @author Jonathan Lovelace
 *
 */
public interface IMutableTile extends ITile {
	/**
	 * @param fix something new on the tile
	 * @return true iff it was not already in the set.
	 */
	boolean addFixture(TileFixture fix);

	/**
	 * @param fix something to remove from the tile
	 * @return the result of the operation
	 */
	boolean removeFixture(TileFixture fix);

	/**
	 * @param river a river to add
	 */
	void addRiver(River river);

	/**
	 * @param river a river to remove
	 */
	void removeRiver(River river);

	/**
	 * @param ttype the tile's new terrain type
	 */
	void setTerrain(TileType ttype);
}
