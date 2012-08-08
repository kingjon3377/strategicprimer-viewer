package model.map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A collection of tiles. This is a wrapper around the Map that had been used by
 * SPMap, to avoid ever returning null. The main difference between this and an
 * SPMap, aside from the latter's extra features, is that this doesn't know
 * anything about the map's size and so doesn't check whether a key makes any
 * sense.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class TileCollection implements Iterable<Point>,
		Subsettable<TileCollection>, DeepCloneable<TileCollection>, HasChildren {
	/**
	 * The default filename for tiles we create to avoid returning null.
	 */
	private final String file;

	/**
	 * Constructor.
	 * 
	 * @param filename the default filename for tiles we create to avoid
	 *        returning null
	 */
	public TileCollection(final String filename) {
		file = filename;
	}

	/**
	 * The Map this is a wrapper around.
	 */
	private final Map<Point, Tile> tiles = new HashMap<Point, Tile>();

	/**
	 * Add a Tile to the map.
	 * 
	 * @param tile the tile to add.
	 */
	public void addTile(final Tile tile) {
		tiles.put(tile.getLocation(), tile);
	}

	/**
	 * Get the specified point. If it isn't in the collection, add a new "empty"
	 * one there and return that. This should never return null.
	 * 
	 * @param point a point
	 * 
	 * @return the tile at that point, or a new "empty" tile at that point.
	 */
	public Tile getTile(final Point point) {
		if (!tiles.containsKey(point)) {
			tiles.put(point, new Tile(point.row(), point.col(),
					TileType.NotVisible, file));
		}
		return tiles.get(point);
	}

	/**
	 * @return an iterator over the Points in the map.
	 */
	@Override
	public Iterator<Point> iterator() {
		return tiles.keySet().iterator();
	}

	/**
	 * @param obj an object
	 * 
	 * @return whether it is an identical TileCollection.
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj == this
				|| (obj instanceof TileCollection && ((TileCollection) obj).tiles
						.equals(tiles));
	}

	/**
	 * 
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return tiles.hashCode();
	}

	/**
	 * 
	 * @return a String representation of the class
	 */
	@Override
	public String toString() {
		return "TileCollection";
	}

	/**
	 * We don't replace the "retval = false" with "return false" because
	 * {@link Tile#isSubset(SimpleTile)} has the side effect of printing what
	 * makes it *not* a subset; we want that done for *all* relevant tiles.
	 * 
	 * @param obj another TileCollection
	 * @return whether it's a strict subset of this one
	 */
	@Override
	public boolean isSubset(final TileCollection obj) {
		boolean retval = true; // NOPMD
		for (final Point point : obj) {
			if (!tiles.containsKey(point)
					|| !tiles.get(point).isSubset(obj.getTile(point))) {
				retval = false; // NOPMD
			}
		}
		return retval;
	}

	/**
	 * @return a clone of this collection
	 */
	@Override
	public TileCollection deepCopy() {
		final TileCollection retval = new TileCollection(file);
		for (final Point point : this) {
			retval.tiles.put(point, (Tile) tiles.get(point).deepCopy());
		}
		return retval;
	}

	/**
	 * Set all children's file property to the specified value, recursively.
	 * 
	 * @param value the value to set
	 */
	@Override
	public void setFileOnChildren(final String value) {
		for (final Tile tile : tiles.values()) {
			tile.setFile(value);
		}
	}
}
