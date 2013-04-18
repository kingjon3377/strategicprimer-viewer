package model.map.fixtures;

import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import model.map.IFixture;
import model.map.River;
import model.map.Subsettable;
import model.map.TileFixture;

/**
 * A Fixture to encapsulate the rivers on a tile, so we can show a chit for
 * rivers.
 *
 * @author Jonathan Lovelace
 *
 */
public class RiverFixture implements TileFixture,
		Iterable<River>, Subsettable<RiverFixture> {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructor.
	 *
	 * @param initial the initial state of the fixture
	 */
	public RiverFixture(final River... initial) {
		super();
		for (final River river : initial) {
			rivers.add(river);
		}
	}

	/**
	 * The Set we're using to hold the Rivers.
	 */
	private final Set<River> rivers = EnumSet.noneOf(River.class);

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
		return EnumSet.copyOf(rivers);
	}

	/**
	 * @return an iterator over the rivers
	 */
	@Override
	public Iterator<River> iterator() {
		return rivers.iterator();
	}

	/**
	 * @param obj an object
	 * @return whether it's an identical RiverFixture
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof RiverFixture && ((RiverFixture) obj).rivers
						.equals(rivers));
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
		final StringBuilder sbuild = new StringBuilder(
				"RiverFixture with rivers: ");
		for (final River river : rivers) {
			sbuild.append(river.toString());
			sbuild.append(' ');
		}
		return sbuild.toString();
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
	 * @param out ignored (no output)
	 */
	@Override
	public boolean isSubset(final RiverFixture obj, final PrintWriter out) {
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
}
