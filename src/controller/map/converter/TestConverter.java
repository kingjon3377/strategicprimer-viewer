package controller.map.converter;

import static org.junit.Assert.assertTrue;
import model.map.MapDimensions;
import model.map.MapView;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.SPMap;
import model.map.Tile;
import model.map.TileType;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.towns.Fortress;

import org.junit.Test;

/**
 * The test case for map-conversion code paths.
 * @author Jonathan Lovelace
 *
 */
public class TestConverter {

	/**
	 * Test conversion.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testConversion() {
		final SPMap start = new SPMap(new MapDimensions(2, 2, 2));
		final Point pointOne = PointFactory.point(0, 0);
		final Tile tileOne = new Tile(TileType.Steppe);
		final Animal fixture = new Animal("animal", false, true,
				"domesticated", 1);
		tileOne.addFixture(fixture);
		start.addTile(pointOne, tileOne);
		final Point pointTwo = PointFactory.point(0, 1);
		final Tile tileTwo = new Tile(TileType.Ocean);
		final CacheFixture fixtureTwo = new CacheFixture("gemstones", "small",
				2);
		tileTwo.addFixture(fixtureTwo);
		start.addTile(pointTwo, tileTwo);
		final Point pointThree = PointFactory.point(1, 0);
		final Tile tileThree = new Tile(TileType.Plains);
		final Unit fixtureThree = new Unit(new Player(0, "A. Player"), "legion", "eagles", 3);
		tileThree.addFixture(fixtureThree);
		start.addTile(pointThree, tileThree);
		final Point pointFour = PointFactory.point(1, 1);
		final Tile tileFour = new Tile(TileType.Jungle);
		final Fortress fixtureFour = new Fortress(new Player(1, "B. Player"), "HQ", 4);
		tileFour.addFixture(fixtureFour);
		start.addTile(pointFour, tileFour);
		final MapView converted = ResolutionDecreaseConverter.convert(start);
		final Point zeroPoint = PointFactory.point(0, 0);
		assertTrue("Combined tile should contain fixtures from tile one",
				doesIterableContain(converted.getTile(zeroPoint), fixture));
		assertTrue("Combined tile should contain fixtures from tile two",
				doesIterableContain(converted.getTile(zeroPoint), fixtureTwo));
		assertTrue(
				"Combined tile should contain fixtures from tile three",
				doesIterableContain(converted.getTile(zeroPoint), fixtureThree));
		assertTrue("Combined tile should contain fixtures from tile four",
				doesIterableContain(converted.getTile(zeroPoint), fixtureFour));
	}

	/**
	 * Test whether an item is in an iterable. Note that the iterable's iterator
	 * will have advanced either to the item searched for or to the end.
	 *
	 * @param iter an iterable
	 * @param item an item
	 * @param <T> the type of the items in the iterator
	 * @param <U> the type of the item
	 * @return whether the iterable contains the item
	 */
	private static <T, U extends T> boolean doesIterableContain(final Iterable<T> iter, final U item) {
		for (final T each : iter) {
			if (each.equals(item)) {
				return true; // NOPMD
			}
		}
		return false;
	}
}
