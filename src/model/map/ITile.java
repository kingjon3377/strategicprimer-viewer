package model.map;

import java.util.Iterator;

import model.map.fixtures.RiverFixture;
/**
 * An interface for the contents of a tile.
 * @author Jonathan Lovelace
 *
 */
public interface ITile extends FixtureIterable<TileFixture>, Subsettable<ITile> {

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
	 * @return the contents of the tile
	 */
	@Override
	Iterator<TileFixture> iterator();

	/**
	 * @param river a river to add
	 */
	void addRiver(River river);

	/**
	 * @param river a river to remove
	 */
	void removeRiver(River river);

	/**
	 * A tile is "empty" if its tile type is NotVisible and it has no contents.
	 *
	 * @return whether this tile is "empty".
	 */
	boolean isEmpty();

	/**
	 * @return whether there are any rivers on the tile
	 */
	boolean hasRiver();

	/**
	 * Call hasRiver() before this, because this may throw
	 * IllegalStateException if we don't actually contain a river.
	 *
	 * TODO: Declare as interface if possible
	 *
	 * @return the RiverFixture that we contain
	 */
	RiverFixture getRivers();

	/**
	 * @return the kind of tile this is
	 */
	TileType getTerrain();

	/**
	 * @param ttype the tile's new terrain type
	 */
	void setTerrain(TileType ttype);

}
