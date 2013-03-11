package model.map;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
		Subsettable<TileCollection> {
	/**
	 * The Map this is a wrapper around.
	 */
	private final Map<Point, Tile> tiles = new HashMap<Point, Tile>();

	/**
	 * Add a Tile to the map.
	 *
	 * @param tile the tile to add.
	 * @param point the point at which to add it
	 */
	public void addTile(final Point point, final Tile tile) {
		tiles.put(point, tile);
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
			tiles.put(point, new Tile(TileType.NotVisible));
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
				|| (obj instanceof TileCollection && withoutEmptyTiles(((TileCollection) obj).tiles)
						.equals(withoutEmptyTiles(tiles)));
	}
	/**
	 * @param mapping a point-tile mapping
	 * @return an equivalent one without any empty tiles.
	 */
	private static Map<Point, Tile> withoutEmptyTiles(final Map<Point, Tile> mapping) {
		final Map<Point, Tile> retval = new HashMap<Point, Tile>();
		for (final Entry<Point, Tile> entry : mapping.entrySet()) {
			final Tile tile = entry.getValue();
			if (!tile.isEmpty()) {
				retval.put(entry.getKey(), tile);
			}
		}
		return retval;
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
	 * {@link Tile#isSubset(Tile, PrintWriter)} has the side effect of printing what
	 * makes it *not* a subset; we want that done for *all* relevant tiles.
	 *
	 * @param obj another TileCollection
	 * @return whether it's a strict subset of this one
	 * @param out the stream to write details of differences to
	 */
	@Override
	public boolean isSubset(final TileCollection obj, final PrintWriter out) {
		boolean retval = true; // NOPMD
		for (final Point point : obj) {
			if (tiles.containsKey(point) || obj.getTile(point).isEmpty()) {
				if (!tiles.get(point).isSubset(obj.getTile(point), out)) {
					retval = false; // NOPMD
				}
			} else {
				out.print("Extra tile at ");
				out.println(point.toString());
				retval = false; // NOPMD
			}
		}
		return retval;
	}
}
