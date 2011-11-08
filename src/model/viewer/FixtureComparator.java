package model.viewer;

import java.io.Serializable;
import java.util.Comparator;

import model.map.TileFixture;

/**
 * A Comparator for TileFixtures. In the new map version, only the upper-most of
 * a tile's fixtures is visible.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class FixtureComparator implements Comparator<TileFixture>, Serializable {
	/**
	 * Version UID for serialization. We implement Serializable here because
	 * it's simple to do so (no state), and doing so makes any Map<TileFixture>
	 * using this comparator potentially serializable, even though I avoid
	 * adding Serializable implementation anywhere else.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Compare two fixtures.
	 * @param one The first fixture
	 * @param two The second fixture
	 * @return the result of the comparison.
	 */
	@Override
	public int compare(final TileFixture one, final TileFixture two) {
		return one.getZValue() - two.getZValue();
	}
}
