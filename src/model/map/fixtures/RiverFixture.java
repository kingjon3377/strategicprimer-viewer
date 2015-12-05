package model.map.fixtures;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import model.map.IFixture;
import model.map.River;
import model.map.SubsettableFixture;
import model.map.TileFixture;
import util.NullCleaner;

/**
 * A Fixture to encapsulate the rivers on a tile, so we can show a chit for
 * rivers.
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class RiverFixture implements TileFixture, Iterable<@NonNull River>,
		SubsettableFixture {
	/**
	 * The maximum size of a river's equivalent string, plus a space.
	 */
	private static final int MAX_RIVER_SIZE = 6;
	/**
	 * The base string we use in toString before listing the rivers.
	 */
	private static final String BASE_STRING = "RiverFixture with rivers: ";
	/**
	 * The Set we're using to hold the Rivers.
	 */
	private final Set<River> rivers;

	/**
	 * Constructor.
	 *
	 * @param initial the initial state of the fixture
	 */
	public RiverFixture(final @NonNull River @NonNull ... initial) {
		rivers = NullCleaner.assertNotNull(EnumSet.noneOf(River.class));
		for (final River river : initial) {
			rivers.add(river);
		}
	}

	/**
	 * @return a copy of this fixture
	 * @param zero ignored, as this fixture has no sensitive information
	 */
	@Override
	public RiverFixture copy(final boolean zero) {
		RiverFixture retval = new RiverFixture();
		for (River river : this) {
			retval.addRiver(river);
		}
		return retval;
	}
	/**
	 * Add a river.
	 *
	 * @param river the river to add
	 */
	public void addRiver(final River river) {
		rivers.add(river);
	}

	/**
	 * Remove a river.
	 *
	 * @param river the river to remove
	 */
	public void removeRiver(final River river) {
		rivers.remove(river);
	}

	/**
	 * @return the river directions
	 */
	public Set<River> getRivers() {
		return NullCleaner.assertNotNull(EnumSet.copyOf(rivers));
	}

	/**
	 * @return an iterator over the rivers
	 */
	@Override
	public Iterator<River> iterator() {
		return NullCleaner.assertNotNull(rivers.iterator());
	}

	/**
	 * @param obj an object
	 * @return whether it's an identical RiverFixture
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof RiverFixture
				&& ((RiverFixture) obj).rivers.equals(rivers);
	}

	/**
	 * @return a hash value for the object Because of Java bug #6579200, this
	 *         has to return a constant.
	 */
	@Override
	public int hashCode() {
		return 0;
	}

	/**
	 * @param fix A TileFixture to compare to
	 *
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(@Nullable final TileFixture fix) {
		if (fix == null) {
			throw new IllegalArgumentException("Compared to null fixture");
		}
		return fix.hashCode() - hashCode();
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 30;
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final StringBuilder sbuild = new StringBuilder(BASE_STRING.length()
				+ MAX_RIVER_SIZE * rivers.size()).append(BASE_STRING);
		for (final River river : rivers) {
			sbuild.append(river.toString());
			sbuild.append(' ');
		}
		return NullCleaner.assertNotNull(sbuild.toString());
	}

	/**
	 * @param obj
	 *            another RiverFixture
	 * @return whether it's a strict subset of this one, containing no rivers
	 *         that this doesn't
	 * @param context
	 *            a string to print before every line of output, describing the
	 *            context
	 * @param ostream
	 *            a stream to print any error messages on, or which rivers are
	 *            extra
	 * @throws IOException
	 *             on I/O error writing error messages
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Appendable ostream,
			final String context) throws IOException {
		if (obj instanceof RiverFixture) {
			final Set<River> temp = EnumSet.copyOf(((RiverFixture) obj).rivers);
			temp.removeAll(rivers);
			if (temp.isEmpty()) {
				return true; // NOPMD
			} else {
				ostream.append(context);
				ostream.append(" Extra rivers:\t");
				for (River river : temp) {
					ostream.append(river.toString().toLowerCase());
				}
				ostream.append('\n');
				return false; // NOPMD
			}
		} else {
			ostream.append(context);
			ostream.append("Incompatible types\n");
			return false;
		}
	}

	/**
	 * Perhaps rivers should have IDs (and names ..), though.
	 *
	 * TODO: investigate how FreeCol does it.
	 *
	 * @return an ID for the fixture. This is constant because it's really a
	 *         container for a collection of rivers.
	 *
	 */
	@Override
	public int getID() {
		return -1;
	}

	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return equals(fix);
	}

	/**
	 * @return a string describing all river fixtures as a class
	 */
	@Override
	public String plural() {
		return "Rivers";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "a river";
	}
}
