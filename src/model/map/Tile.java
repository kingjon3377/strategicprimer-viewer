// $codepro.audit.disable booleanMethodNamingConvention
package model.map;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.towns.Fortress;
import util.ArraySet;

/**
 * A tile in a map.
 *
 * @author Jonathan Lovelace
 *
 */
public final class Tile implements XMLWritable,
		Iterable<TileFixture>, Subsettable<Tile>, Serializable {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructor.
	 *
	 * @param tileType The tile type
	 */
	public Tile(final TileType tileType) {
		super();
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
	 * This is immediately needed for the MapNGAdapter to be even remotely efficient.
	 * @return a read-only view of the contents of the tile.
	 */
	public Set<TileFixture> getContents() {
		return Collections.unmodifiableSet(contents);
	}
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
						&& getTerrain().equals(((Tile) obj).getTerrain()) && contents
							.equals(((Tile) obj).contents));
	}

	/**
	 *
	 * @return a hash-code for the object
	 */
	@Override
	public int hashCode() {
		return getTerrain().ordinal();
	}

	/**
	 *
	 * @return a String representation of the tile
	 */
	@Override
	public String toString() {
		final StringBuilder sbuilder = new StringBuilder(getTerrain().toString());
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
	public boolean isSubset(final Tile obj, final PrintWriter out) {
		if (getTerrain().equals(obj.getTerrain())) {
			final Set<TileFixture> temp = new HashSet<TileFixture>(obj.contents);
			temp.removeAll(contents);
			final Map<Integer, Subsettable<?>> mySubsettables = new HashMap<Integer, Subsettable<?>>();
			final List<TileFixture> tempList = new ArrayList<TileFixture>(temp);
			for (final TileFixture fix : contents) {
				if (fix instanceof Subsettable) {
					mySubsettables.put(Integer.valueOf(fix.getID()),
							(Subsettable<?>) fix);
				}
			}
			boolean retval = true;
			for (final TileFixture fix : tempList) {
				if (shouldSkip(fix)) {
					temp.remove(fix);
				} else if (fix instanceof Subsettable
						&& mySubsettables.containsKey(Integer.valueOf(fix
								.getID()))) {
					temp.remove(fix);
					final Subsettable<?> mine = mySubsettables.get(Integer
							.valueOf(fix.getID()));
					if (mine instanceof Fortress && fix instanceof Fortress) {
						retval &= ((Fortress) mine).isSubset((Fortress) fix,
								out);
					} else {
						throw new IllegalStateException(
								"Unhandled Subsettable class");
					}
				}
			}
			for (TileFixture fix : temp) {
				retval = false;
				out.println("Extra fixture:\t" + fix.toString());
			}
			return retval; // NOPMD
		} else {
			out.println("Tile type wrong");
			return false;
		}
	}

	/**
	 * @param fix a fixture
	 * @return whether strict-subset calculations should skip it.
	 */
	public static boolean shouldSkip(final TileFixture fix) {
		return fix instanceof CacheFixture || fix instanceof TextFixture
				|| (fix instanceof Animal && ((Animal) fix).isTraces());
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
