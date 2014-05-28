package model.map;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;
import util.PrefixingPrintWriter;

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
public final class TileCollection implements IMutableTileCollection {
	/**
	 * The Map this is a wrapper around.
	 */
	private final Map<Point, IMutableTile> tiles = new HashMap<>();

	/**
	 * Add a Tile to the map.
	 *
	 * @param tile the tile to add.
	 * @param point the point at which to add it
	 */
	@Override
	public void addTile(final Point point, final IMutableTile tile) {
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
	@Override
	public IMutableTile getTile(final Point point) {
		if (!tiles.containsKey(point)) {
			tiles.put(point, new Tile(TileType.NotVisible));
		}
		return NullCleaner.assertNotNull(tiles.get(point));
	}

	/**
	 * @return an iterator over the Points in the map.
	 */
	@Override
	public Iterator<Point> iterator() {
		return NullCleaner.assertNotNull(tiles.keySet().iterator());
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it is an identical TileCollection.
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return obj == this
				|| obj instanceof TileCollection
				&& withoutEmptyTiles(((TileCollection) obj).tiles).equals(
						withoutEmptyTiles(tiles));
	}

	/**
	 * @param mapping a point-tile mapping
	 * @return an equivalent one without any empty tiles.
	 */
	private static Map<Point, ? extends ITile> withoutEmptyTiles(
			final Map<Point, ? extends ITile> mapping) {
		final Map<Point, ITile> retval = new HashMap<>();
		for (final Entry<Point, ? extends ITile> entry : mapping.entrySet()) {
			final ITile tile = entry.getValue();
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
	 * {@link Tile#isSubset(ITile, Appendable)} has the side effect of printing
	 * what makes it *not* a subset; we want that done for *all* relevant tiles.
	 *
	 * @param obj another TileCollection
	 * @return whether it's a strict subset of this one
	 * @param ostream the stream to write details of differences to
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean
			isSubset(final ITileCollection obj, final Appendable ostream)
					throws IOException {
		boolean retval = true; // NOPMD
		for (final Point point : obj) {
			if (point == null) {
				continue;
			} else if (tiles.containsKey(point) || obj.getTile(point).isEmpty()) {
				final StringWriter str = new StringWriter(); // NOPMD
				try (final PrefixingPrintWriter writer = new PrefixingPrintWriter(
						str, point.toString() + ":\t")) {
					if (!tiles.get(point).isSubset(obj.getTile(point), writer)) {
						ostream.append(str.toString());
						retval = false; // NOPMD
					}
				}
			} else {
				ostream.append("Extra tile at ");
				ostream.append(point.toString());
				ostream.append('\n');
				retval = false; // NOPMD
			}
		}
		return retval;
	}

	/**
	 * @param point a point
	 * @return whether there's a non-empty tile at that point
	 */
	@Override
	public boolean hasTile(final Point point) {
		return tiles.containsKey(point) && !tiles.get(point).isEmpty();
	}
}
