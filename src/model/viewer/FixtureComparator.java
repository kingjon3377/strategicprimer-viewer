package model.viewer;

import java.util.Comparator;

import model.map.TileFixture;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A Comparator for TileFixtures. In the new map version, only the upper-most of
 * a tile's fixtures is visible.
 *
 * TODO: tests
 *
 * @author Jonathan Lovelace
 *
 */
public class FixtureComparator implements Comparator<TileFixture> {
	/**
	 * Compare two fixtures.
	 *
	 * @param one The first fixture
	 * @param two The second fixture
	 * @return the result of the comparison.
	 */
	@Override
	public int compare(@Nullable final TileFixture one, @Nullable final TileFixture two) {
		if (one == null || two == null) {
			throw new IllegalArgumentException("Asked to compare null fixture");
		}
		return two.getZValue() - one.getZValue();
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "FixtureComparator";
	}
}
