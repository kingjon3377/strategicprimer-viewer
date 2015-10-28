// $codepro.audit.disable booleanMethodNamingConvention
package model.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.resources.CacheFixture;
import model.viewer.TileTypeFixture;
import util.ArraySet;
import util.NullCleaner;

/**
 * A tile in a map.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 * @deprecated the old map API is deprecated in this branch
 */
@Deprecated
public final class Tile implements IMutableTile {
	/**
	 * The tile type.
	 */
	private TileType type;

	/**
	 * The units, fortresses, and events on the tile.
	 */
	private final Set<TileFixture> contents;

	/**
	 * Constructor.
	 *
	 * TODO: Take fixtures as constructor parameters, so that callers don't have
	 * to use the IMutableTile interface rather than ITile.
	 *
	 * @param tileType The tile type
	 */
	public Tile(final TileType tileType) {
		type = tileType;
		// Can't be an otherwise-preferable TreeSet because of Java bug
		// #7030899: TreeSet ignores equals() entirely.
		contents = new ArraySet<>();
	}

	/**
	 * @param fix something new on the tile
	 * @return true iff it was not already in the set.
	 */
	@Override
	public boolean addFixture(final TileFixture fix) {
		if (fix instanceof TextFixture
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
		return NullCleaner.assertNotNull(contents.iterator());
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it is an identical tile
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		if (this == obj) {
			return true; // NOPMD
		} else if (obj instanceof ITile
				&& getTerrain().equals(((ITile) obj).getTerrain())) {
			final Set<TileFixture> ours = new HashSet<>(contents);
			final Iterator<TileFixture> iter = ((ITile) obj).iterator();
			while (iter.hasNext()) {
				ours.remove(iter.next());
			}
			return ours.isEmpty() && !iter.hasNext(); // NOPMD
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
		final int len = terrain.length() + 12 + contents.size() * 512;
		final StringBuilder sbuilder = new StringBuilder(len).append(terrain);
		sbuilder.append(". Contents:");
		for (final TileFixture fix : contents) {
			sbuilder.append("\n\t\t");
			sbuilder.append(fix);
		}
		return NullCleaner.assertNotNull(sbuilder.toString());
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
	 * @param ostream the stream to write details of the differences to
	 * @param context
	 *            a string to print before every line of output, describing the
	 *            context
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final ITile obj, final Appendable ostream,
			final String context) throws IOException {
		if (getTerrain().equals(obj.getTerrain())) {
			return isSubsetImpl(obj, ostream, context); // NOPMD
		} else {
			ostream.append(context);
			ostream.append("\tTile type wrong\n");
			return false;
		}
	}

	/**
	 * Implementation of isSubset() assuming that the terrain types match.
	 *
	 * @param obj
	 *            another Tile
	 * @return whether it's a strict subset of this one, having no members this
	 *         one doesn't
	 * @param ostream
	 *            the stream to write details of the differences to
	 * @param context
	 *            a string to print before every line of output, describing the
	 *            context
	 * @throws IOException
	 *             on I/O error writing output to the stream
	 */
	protected boolean isSubsetImpl(final ITile obj, final Appendable ostream,
			final String context) throws IOException {
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
				if (mine instanceof IUnit && fix instanceof IUnit) {
					if (!((IUnit) mine).isSubset(fix, ostream, context)) {
						retval = false;
					}
				} else if (mine instanceof SubsettableFixture) {
					if (!((SubsettableFixture) mine).isSubset(fix, ostream,
							context)) {
						retval = false;
					}
				} else {
					throw new IllegalStateException(
							"Unhandled Subsettable class");
				}
			} else {
				retval = false;
				ostream.append(context);
				ostream.append(" Extra fixture:\t");
				ostream.append(fix.toString());
				ostream.append(", ID #");
				ostream.append(Integer.toString(fix.getID()));
				ostream.append('\n');
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
				|| fix instanceof Animal && ((Animal) fix).isTraces();
	}

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
