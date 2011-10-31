package model.viewer;

import java.io.Serializable;
import java.util.Comparator;
import java.util.logging.Logger;

import model.map.TileFixture;
import model.map.events.AbstractTownEvent;
import model.map.events.Forest;
import model.map.events.IEvent;
import model.map.fixtures.Fortress;
import model.map.fixtures.Mountain;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.Unit;

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
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(FixtureComparator.class.getName());
	/**
	 * Compare two fixtures.
	 * @param one The first fixture
	 * @param two The second fixture
	 * @return the result of the comparison.
	 */
	@Override
	public int compare(final TileFixture one, final TileFixture two) {
		return getValue(one) - getValue(two);
	}
	/**
	 * Return an integer representing, effectively, the fixture's z-value on the tile.
	 * @param fix a fixture
	 * @return a value for it
	 */
	private static int getValue(final TileFixture fix) {
		if (fix instanceof Mountain) {
			return 10; // NOPMD
		} else if (fix instanceof Forest) {
			return 20; // NOPMD
		} else if (fix instanceof RiverFixture) {
			return 30; // NOPMD
		} else if (fix instanceof AbstractTownEvent) {
			return 50; // NOPMD
			// We have to do this out of order because TownEvents *are* IEvents
			// but get a higher priority.
		} else if (fix instanceof IEvent) {
			return 40; // NOPMD
		} else if (fix instanceof Fortress) {
			return 60; // NOPMD
		} else if (fix instanceof Unit) {
			return 70; // NOPMD
		} else {
			LOGGER.warning("Unhandled type of Fixture.");
			return 0;
		}
	}
}
