package model.map.fixtures;

import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import model.map.IFixture;
import model.map.River;
import model.map.Subsettable;
import model.map.TileFixture;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;

/**
 * A Fixture to encapsulate the rivers on a tile, so we can show a chit for
 * rivers.
 *
 * @author Jonathan Lovelace
 *
 */
public final class RiverFixture implements TileFixture, Iterable<River>,
		Subsettable<RiverFixture> {
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
	public RiverFixture(final River... initial) {
		super();
		rivers = NullCleaner.assertNotNull(EnumSet.noneOf(River.class));
		for (final River river : initial) {
			rivers.add(river);
		}
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
	 * @param fix A TileFixture to compare to
	 *
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}

	/**
	 * @param obj another RiverFixture
	 * @return whether it's a strict subset of this one, containing no rivers
	 *         that this doesn't
	 * @param ostream ignored (no output)
	 */
	@Override
	public boolean isSubset(final RiverFixture obj, final PrintWriter ostream) {
		final Set<River> temp = EnumSet.copyOf(obj.rivers);
		temp.removeAll(rivers);
		return temp.isEmpty();
	}

	/**
	 * Perhaps rivers should have IDs (and names ..), though.
	 *
	 * TODO: investigate how FreeCol does it.
	 *
	 * @return an ID for the fixture. This is constant because it's really a
	 *         container for a ollection of rivers.
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
