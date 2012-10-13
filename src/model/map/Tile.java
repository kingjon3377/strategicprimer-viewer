// $codepro.audit.disable booleanMethodNamingConvention
package model.map;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.resources.CacheFixture;
import util.ArraySet;

/**
 * A tile in a map.
 *
 * @author Jonathan Lovelace
 *
 */
public final class Tile implements XMLWritable,
		Iterable<TileFixture>, Subsettable<Tile> {
	/**
	 * Constructor.
	 *
	 * @param tileRow The row number
	 * @param tileCol The column number
	 * @param tileType The tile type
	 */
	public Tile(final int tileRow, final int tileCol, final TileType tileType) {
		super();
		location = PointFactory.point(tileRow, tileCol);
		type = tileType;
		// Can't be an otherwise-preferable TreeSet because of Java bug
		// #7030899: TreeSet ignores equals() entirely.
		contents = new ArraySet<TileFixture>();
	}

	/**
	 * The units, fortresses, and events on the tile.
	 */
	private final Set<TileFixture> contents;

	/**
	 * @param fix something new on the tile
	 * @return true iff it was not already in the set.
	 */
	public boolean addFixture(final TileFixture fix) {
		if ((fix instanceof TextFixture) && ((TextFixture) fix).getText()
				.isEmpty()) {
			return false; // NOPMD
		} else {
			if (fix instanceof RiverFixture) {
				if (hasRiver()) {
					final RiverFixture rivers = getRivers();
					for (River river : (RiverFixture) fix) {
						rivers.addRiver(river);
					}
					return true; // NOPMD
				} else if (((RiverFixture) fix).getRivers().isEmpty()) {
					return false; // NOPMD
				} else {
					return contents.add(fix); // NOPMD
				}
			} else {
				return contents.add(fix); // NOPMD
			}
		}
	}

	/**
	 * @param fix something to remove from the tile
	 * @return the result of the operation
	 */
	public boolean removeFixture(final TileFixture fix) {
		return contents.remove(fix);
	}

	/**
	 * FIXME: Should return a copy, not the real collection.
	 *
	 *
	 * @return the contents of the tile
	 */
	@Override
	public Iterator<TileFixture> iterator() {
		return contents.iterator();
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it is an identical tile
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| ((obj instanceof Tile)
						&& getLocation().equals(
								((Tile) obj).getLocation())
						&& getTerrain().equals(((Tile) obj).getTerrain()) && contents
							.equals(((Tile) obj).contents));
	}

	/**
	 *
	 * @return a hash-code for the object
	 */
	@Override
	public int hashCode() {
		return getLocation().hashCode() + getTerrain().ordinal() << 6 + contents.hashCode() << 8;
	}

	/**
	 *
	 * @return a String representation of the tile
	 */
	@Override
	public String toString() {
		final StringBuilder sbuilder = new StringBuilder(getLocation().toString());
		sbuilder.append(": ");
		sbuilder.append(getTerrain());
		sbuilder.append('.');
		sbuilder.append(" Contents:");
		for (final TileFixture fix : contents) {
			sbuilder.append("\n\t\t");
			sbuilder.append(fix);
		}
		return sbuilder.toString();
	}

	/**
	 * @param river a river to add
	 */
	public void addRiver(final River river) {
		if (hasRiver()) {
			getRivers().addRiver(river);
		} else {
			addFixture(new RiverFixture(river));
		}
	}

	/**
	 * @param river a river to remove
	 */
	public void removeRiver(final River river) {
		if (hasRiver()) {
			final RiverFixture rivers = getRivers();
			rivers.removeRiver(river);
			if (rivers.getRivers().isEmpty()) {
				removeFixture(rivers);
			}
		}
	}

	/**
	 * Update with data from a tile in another map.
	 *
	 * @param tile the same tile in another map.
	 */
	public void update(final Tile tile) {
		setTerrain(tile.getTerrain());
			final Set<TileFixture> unmatchedContents = new HashSet<TileFixture>(
					contents);
			unmatchedContents.removeAll(tile.contents);
			for (final TileFixture local : unmatchedContents) {
				for (final TileFixture remote : tile) {
					if (local.equalsIgnoringID(remote)) {
						removeFixture(local);
						addFixture(remote);
						break;
					}
				}
			}
	}

	/**
	 * A tile is "empty" if its tile type is NotVisible and it has no contents.
	 *
	 * @return whether this tile is "empty".
	 */
	public boolean isEmpty() {
		return TileType.NotVisible.equals(getTerrain()) && !iterator().hasNext();
	}

	/**
	 * @return whether we contain a RiverFixture
	 */
	public boolean hasRiver() {
		for (final TileFixture fix : contents) {
			if (fix instanceof RiverFixture) {
				return true; // NOPMD
			}
		}
		return false;
	}

	/**
	 * Call hasRiver() before this, because this will throw
	 * IllegalStateException if we don't actually contain a river.
	 *
	 * @return the RiverFixture that we contain
	 */
	public RiverFixture getRivers() {
		for (final TileFixture fix : contents) {
			if (fix instanceof RiverFixture) {
				return (RiverFixture) fix;
			}
		}
		throw new IllegalStateException("Didn't find a RiverFixture");
	}

	/**
	 * @param obj another Tile
	 * @return whether it's a strict subset of this one, having no members this
	 *         one doesn't
	 * @param out the stream to write details of the differences to
	 */
	@Override
	public boolean isSubset(final Tile obj, final PrintStream out) {
		if (getLocation().equals(obj.getLocation()) && getTerrain().equals(obj.getTerrain())) {
				final Set<TileFixture> temp = new HashSet<TileFixture>(
						obj.contents);
				temp.removeAll(contents);
				final List<TileFixture> tempList = new ArrayList<TileFixture>(
						temp);
				for (final TileFixture fix : tempList) {
					if (shouldSkip(fix)) {
						temp.remove(fix);
					}
				}
				if (!temp.isEmpty()) {
					out.print("\nExtra fixture in "
							+ getLocation().toString() + ":\t");
					for (final TileFixture fix : temp) {
						out.print(fix.toString());
					}
				}
				return temp.isEmpty(); // NOPMD
		} else {
			out.print("Type of " + getLocation().toString()
					+ " wrong\t");
			return false;
		}
	}

	/**
	 * @param fix a fixture
	 * @return whether strict-subset calculations should skip it.
	 */
	private static boolean shouldSkip(final TileFixture fix) {
		return fix instanceof CacheFixture || fix instanceof TextFixture
				|| (fix instanceof Animal && ((Animal) fix).isTraces());
	}

	/**
	 * The tile's location.
	 */
	private final Point location;

	/**
	 * @return the tile's location
	 */
	public Point getLocation() {
		return location;
	}

	/**
	 * The tile type.
	 */
	private TileType type;

	/**
	 *
	 * @return the kind of tile
	 */
	public TileType getTerrain() {
		return type;
	}

	/**
	 * @param ttype the tile's new terrain type
	 */
	public void setTerrain(final TileType ttype) {
		type = ttype;
	}
}
