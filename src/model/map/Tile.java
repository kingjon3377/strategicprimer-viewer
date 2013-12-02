// $codepro.audit.disable booleanMethodNamingConvention
package model.map;

import java.io.PrintWriter;
import java.util.ArrayList;
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
import model.viewer.TileTypeFixture;

import org.eclipse.jdt.annotation.Nullable;

import util.ArraySet;

/**
 * A tile in a map.
 *
 * @author Jonathan Lovelace
 *
 */
public final class Tile implements IMutableTile {
	/**
	 * Constructor.
	 *
	 * TODO: Take fixtures as constructor parameters, so that callers don't have
	 * to use the IMutableTile interface rather than ITile.
	 *
	 * @param tileType The tile type
	 */
	public Tile(final TileType tileType) {
		super();
		type = tileType;
		// Can't be an otherwise-preferable TreeSet because of Java bug
		// #7030899: TreeSet ignores equals() entirely.
		contents = new ArraySet<>();
	}

	/**
	 * The units, fortresses, and events on the tile.
	 */
	private final Set<TileFixture> contents;

	/**
	 * @param fix something new on the tile
	 * @return true iff it was not already in the set.
	 */
	@Override
	public boolean addFixture(final TileFixture fix) {
		if ((fix instanceof TextFixture)
				&& ((TextFixture) fix).getText().isEmpty()) {
			return false; // NOPMD
		} else if (fix instanceof TileTypeFixture) {
			final TileType old = getTerrain();
			setTerrain(((TileTypeFixture) fix).getTileType());
			return !getTerrain().equals(old); // NOPMD
		} else if (fix instanceof RiverFixture) {
			if (hasRiver()) {
				final RiverFixture rivers = (RiverFixture) getRivers();
				for (final River river : (RiverFixture) fix) {
					if (river != null) {
						rivers.addRiver(river);
					}
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

	/**
	 * @param fix something to remove from the tile
	 * @return the result of the operation
	 */
	@Override
	public boolean removeFixture(final TileFixture fix) {
		return contents.remove(fix);
	}

	/**
	 * @return the contents of the tile
	 */
	@Override
	public Iterator<TileFixture> iterator() {
		final Iterator<TileFixture> iter = contents.iterator();
		assert iter != null;
		return iter;
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it is an identical tile
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		// TODO: Figure out some way of testing contents-equality without using getContents
		if (this == obj) {
			return true;
		} else if ((obj instanceof ITile)
				&& getTerrain().equals(((ITile) obj).getTerrain())) {
			final Set<TileFixture> ours = new HashSet<>(contents);
			final Iterator<TileFixture> iter = ((ITile) obj).iterator();
			while (iter.hasNext()) {
				ours.remove(iter.next());
			}
			return ours.isEmpty() && !iter.hasNext();
		} else {
			return false;
		}
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
		// This can get big; fortunately it's rarely called. Assume each item on
		// the tile is half a K.
		final String terrain = getTerrain().toString();
		final int len = terrain.length() + 12 + (contents.size() * 512);
		final StringBuilder sbuilder = new StringBuilder(len).append(terrain);
		sbuilder.append(". Contents:");
		for (final TileFixture fix : contents) {
			sbuilder.append("\n\t\t");
			sbuilder.append(fix);
		}
		final String retval = sbuilder.toString();
		assert retval != null;
		return retval;
	}

	/**
	 * @param river a river to add
	 */
	@Override
	public void addRiver(final River river) {
		if (hasRiver()) {
			((RiverFixture) getRivers()).addRiver(river);
		} else {
			addFixture(new RiverFixture(river));
		}
	}

	/**
	 * @param river a river to remove
	 */
	@Override
	public void removeRiver(final River river) {
		if (hasRiver()) {
			final RiverFixture rivers = (RiverFixture) getRivers();
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
	@Override
	public boolean isEmpty() {
		return TileType.NotVisible.equals(getTerrain())
				&& !iterator().hasNext();
	}

	/**
	 * @return whether we contain a RiverFixture
	 */
	@Override
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
	@Override
	public Iterable<River> getRivers() {
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
	public boolean isSubset(final ITile obj, final PrintWriter out) {
		if (getTerrain().equals(obj.getTerrain())) {
			return isSubsetImpl(obj, out); // NOPMD
		} else {
			out.println("Tile type wrong");
			return false;
		}
	}

	/**
	 * Implementation of isSubset() assuming that the terrain types match.
	 *
	 * @param obj another Tile
	 * @return whether it's a strict subset of this one, having no members this
	 *         one doesn't
	 * @param out the stream to write details of the differences to
	 */
	protected boolean isSubsetImpl(final ITile obj, final PrintWriter out) {
		final List<TileFixture> temp = new ArrayList<>();
		final Map<Integer, Subsettable<?>> mySubsettables = getSubsettableContents();
		for (final TileFixture fix : obj) {
			if (fix != null && !contents.contains(fix) && !temp.contains(fix)
					&& !shouldSkip(fix)) {
				temp.add(fix);
			}
		}
		boolean retval = true;
		for (final TileFixture fix : temp) {
			assert fix != null;
			if (fix instanceof Subsettable
					&& mySubsettables.containsKey(Integer.valueOf(fix.getID()))) {
				final Subsettable<?> mine = mySubsettables.get(Integer
						.valueOf(fix.getID()));
				if (mine instanceof Fortress && fix instanceof Fortress) {
					if (!((Fortress) mine).isSubset((Fortress) fix, out)) {
						retval = false;
					}
				} else {
					throw new IllegalStateException(
							"Unhandled Subsettable class");
				}
			} else {
				retval = false;
				out.print("Extra fixture:\t");
				out.println(fix.toString());
			}
		}
		return retval; // NOPMD
	}

	/**
	 * @return the contents of the tile that are Subsettable implementations,
	 *         each mapped from its ID # to itself.
	 */
	private Map<Integer, Subsettable<?>> getSubsettableContents() {
		final Map<Integer, Subsettable<?>> mySubsettables = new HashMap<>();
		for (final TileFixture fix : contents) {
			if (fix instanceof Subsettable) {
				mySubsettables.put(Integer.valueOf(fix.getID()),
						(Subsettable<?>) fix);
			}
		}
		return mySubsettables;
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
	 * @return the kind of tile this is
	 */
	@Override
	public TileType getTerrain() {
		return type;
	}

	/**
	 * @param ttype the tile's new terrain type
	 */
	@Override
	public void setTerrain(final TileType ttype) {
		type = ttype;
	}
}
