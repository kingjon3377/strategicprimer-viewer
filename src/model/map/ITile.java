package model.map;

import java.util.Iterator;
/**
 * An interface for the contents of a tile.
 * @author Jonathan Lovelace
 *
 */
public interface ITile extends FixtureIterable<TileFixture>, Subsettable<ITile> {

	/**
	 * @return the contents of the tile
	 */
	@Override
	Iterator<TileFixture> iterator();

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
	 * @return the rivers that we contain
	 */
	Iterable<River> getRivers();

	/**
	 * @return the kind of tile this is
	 */
	TileType getTerrain();
}
