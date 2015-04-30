package model.map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.NullStream.DEV_NULL;

import java.io.IOException;

import model.map.fixtures.RiverFixture;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Fortress;

import org.junit.Test;

/**
 * Tests for Subsettable functionality.
 *
 * @author Jonathan Lovelace
 *
 */
public class TestSubsets {
	/**
	 * The string "static-method", used in an annotation on each method.
	 */
	private static final String ST_MET = "static-method";
	/**
	 * A commonly-used string.
	 */
	private static final String ONE_STR = "one";

	/**
	 * A test of PlayerCollection's subset feature.
	 * @throws IOException on I/O error writing to the null stream
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testPlayerCollectionSubset() throws IOException {
		final PlayerCollection zero = new PlayerCollection();
		final PlayerCollection one = new PlayerCollection();
		final PlayerCollection two = new PlayerCollection();
		one.add(new Player(1, ONE_STR));
		two.add(new Player(1, ONE_STR));
		two.add(new Player(2, "two"));
		assertTrue("Empty is subset of self", zero.isSubset(zero, DEV_NULL, ""));
		assertTrue("Empty is subset of one", one.isSubset(zero, DEV_NULL, ""));
		assertTrue("Empty is subset of two", two.isSubset(zero, DEV_NULL, ""));
		assertFalse("One is not subset of empty",
				zero.isSubset(one, DEV_NULL, ""));
		assertTrue("One is subset of self", one.isSubset(one, DEV_NULL, ""));
		assertTrue("One is subset of two", two.isSubset(one, DEV_NULL, ""));
		assertFalse("Two is not subset of empty",
				zero.isSubset(two, DEV_NULL, ""));
		assertFalse("Two is not subset of one", one.isSubset(two, DEV_NULL, ""));
		assertTrue("Two is subset of self", two.isSubset(two, DEV_NULL, ""));
	}

	/**
	 * A test of RiverFixture's subset feature.
	 * @throws IOException on I/O error writing to the null stream
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testRiverSubset() throws IOException {
		final RiverFixture zero = new RiverFixture();
		final RiverFixture one = new RiverFixture(River.Lake, River.South,
				River.East);
		final RiverFixture two = new RiverFixture(River.West, River.North);
		final RiverFixture three = new RiverFixture(River.Lake, River.South,
				River.East, River.North, River.West);
		assertTrue("None is a subset of all", three.isSubset(zero, DEV_NULL, ""));
		assertTrue("Three are a subset of all", three.isSubset(one, DEV_NULL, ""));
		assertTrue("Two are a subset of all", three.isSubset(two, DEV_NULL, ""));
		assertTrue("All is a subset of all", three.isSubset(three, DEV_NULL, ""));
		assertTrue("None is a subset of two", two.isSubset(zero, DEV_NULL, ""));
		assertFalse("All is not a subset of two",
				two.isSubset(three, DEV_NULL, ""));
		assertFalse("Three are not a subset of two",
				two.isSubset(one, DEV_NULL, ""));
		assertTrue("Two are a subset of themselves",
				two.isSubset(two, DEV_NULL, ""));
		assertTrue("None is a subset of three", one.isSubset(zero, DEV_NULL, ""));
		assertFalse("All is not a subset of three",
				one.isSubset(three, DEV_NULL, ""));
		assertFalse("A different two are not a subset of three",
				one.isSubset(two, DEV_NULL, ""));
		assertTrue("Three are a subset of themselves",
				one.isSubset(one, DEV_NULL, ""));
		assertTrue("None is a subset of itself",
				zero.isSubset(zero, DEV_NULL, ""));
		assertFalse("All are not a subset of none",
				zero.isSubset(three, DEV_NULL, ""));
		assertFalse("Three are not a subset of none",
				zero.isSubset(one, DEV_NULL, ""));
		assertFalse("Two are not a subset of none",
				zero.isSubset(two, DEV_NULL, ""));
	}

	/**
	 * A test of Fortress's subset feature.
	 * @throws IOException on I/O error writing to the null stream
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testFortressSubset() throws IOException {
		final Fortress one = new Fortress(new Player(1, ONE_STR), "fOne", 1);
		final Fortress two = new Fortress(new Player(2, "two"), "fOne", 1);
		assertFalse("Subset requires same owner, first test",
				one.isSubset(two, DEV_NULL, ""));
		assertFalse("Subset requires same owner, second test",
				two.isSubset(one, DEV_NULL, ""));
		final Fortress three = new Fortress(new Player(1, ONE_STR), "fTwo", 2);
		assertFalse("Subset requires same name, first test",
				one.isSubset(three, DEV_NULL, ""));
		assertFalse("Subset requires same name, second test",
				three.isSubset(one, DEV_NULL, ""));
		final Fortress four = new Fortress(new Player(1, ONE_STR), "fOne", 3);
		assertTrue("Subset doesn't require identiy or ID equality",
				one.isSubset(four, DEV_NULL, ""));
		assertTrue("Subset doesn't require identiy or ID equality",
				four.isSubset(one, DEV_NULL, ""));
		four.addUnit(new Unit(new Player(2, "two"), "unit_type", "unit_name", 4));
		assertTrue("Fortress without is a subset of fortress with unit",
				four.isSubset(one, DEV_NULL, ""));
		assertFalse("Fortress with is not a subset of fortress without unit",
				one.isSubset(four, DEV_NULL, ""));
	}

	/**
	 * Test the MapNG subset feature.
	 *
	 * FIXME: Cover more than terrain, like the now-deleted Tile subset test did.
	 *
	 * @throws IOException on I/O error writing to the null stream
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testMapSubset() throws IOException {
		final IMutableMapNG zero =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(),
						-1);
		final IMutableMapNG one =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(),
						-1);
		final Point pointOne = PointFactory.point(0, 0);
		one.setBaseTerrain(pointOne, TileType.Jungle);
		final IMutableMapNG two =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(),
						-1);
		two.setBaseTerrain(pointOne, TileType.Jungle);
		final Point pointTwo = PointFactory.point(1, 1);
		two.setBaseTerrain(pointTwo, TileType.Ocean);
		assertTrue("None is a subset of itself",
				zero.isSubset(zero, DEV_NULL, ""));
		assertTrue("None is a subset of one", one.isSubset(zero, DEV_NULL, ""));
		assertTrue("None is a subset of two", two.isSubset(zero, DEV_NULL, ""));
		assertFalse("One is not a subset of none",
				zero.isSubset(one, DEV_NULL, ""));
		assertTrue("One is a subset of itself", one.isSubset(one, DEV_NULL, ""));
		assertTrue("One is a subset of two", two.isSubset(one, DEV_NULL, ""));
		assertFalse("TWo is not a subset of none",
				zero.isSubset(two, DEV_NULL, ""));
		assertFalse("Two is not a subset of one", one.isSubset(two, DEV_NULL, ""));
		assertTrue("Two is a subset of itself", two.isSubset(two, DEV_NULL, ""));
		one.setBaseTerrain(pointTwo, TileType.Plains);
		assertFalse("Corresponding but non-matching tile breaks subset",
				two.isSubset(one, DEV_NULL, ""));
		assertFalse("Corresponding but non-matching tile breaks subset",
				one.isSubset(two, DEV_NULL, ""));
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TestSubsets";
	}
}
