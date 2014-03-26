package model.map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.NullStream.DEV_NULL;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.terrain.Mountain;
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
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testPlayerCollectionSubset() {
		final PlayerCollection zero = new PlayerCollection();
		final PlayerCollection one = new PlayerCollection();
		final PlayerCollection two = new PlayerCollection();
		one.add(new Player(1, ONE_STR));
		two.add(new Player(1, ONE_STR));
		two.add(new Player(2, "two"));
		assertTrue("Empty is subset of self", zero.isSubset(zero, DEV_NULL));
		assertTrue("Empty is subset of one", one.isSubset(zero, DEV_NULL));
		assertTrue("Empty is subset of two", two.isSubset(zero, DEV_NULL));
		assertFalse("One is not subset of empty",
				zero.isSubset(one, DEV_NULL));
		assertTrue("One is subset of self", one.isSubset(one, DEV_NULL));
		assertTrue("One is subset of two", two.isSubset(one, DEV_NULL));
		assertFalse("Two is not subset of empty",
				zero.isSubset(two, DEV_NULL));
		assertFalse("Two is not subset of one", one.isSubset(two, DEV_NULL));
		assertTrue("Two is subset of self", two.isSubset(two, DEV_NULL));
	}

	/**
	 * A test of RiverFixture's subset feature.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testRiverSubset() {
		final RiverFixture zero = new RiverFixture();
		final RiverFixture one = new RiverFixture(River.Lake, River.South,
				River.East);
		final RiverFixture two = new RiverFixture(River.West, River.North);
		final RiverFixture three = new RiverFixture(River.Lake, River.South,
				River.East, River.North, River.West);
		assertTrue("None is a subset of all", three.isSubset(zero, DEV_NULL));
		assertTrue("Three are a subset of all", three.isSubset(one, DEV_NULL));
		assertTrue("Two are a subset of all", three.isSubset(two, DEV_NULL));
		assertTrue("All is a subset of all", three.isSubset(three, DEV_NULL));
		assertTrue("None is a subset of two", two.isSubset(zero, DEV_NULL));
		assertFalse("All is not a subset of two",
				two.isSubset(three, DEV_NULL));
		assertFalse("Three are not a subset of two",
				two.isSubset(one, DEV_NULL));
		assertTrue("Two are a subset of themselves",
				two.isSubset(two, DEV_NULL));
		assertTrue("None is a subset of three", one.isSubset(zero, DEV_NULL));
		assertFalse("All is not a subset of three",
				one.isSubset(three, DEV_NULL));
		assertFalse("A different two are not a subset of three",
				one.isSubset(two, DEV_NULL));
		assertTrue("Three are a subset of themselves",
				one.isSubset(one, DEV_NULL));
		assertTrue("None is a subset of itself",
				zero.isSubset(zero, DEV_NULL));
		assertFalse("All are not a subset of none",
				zero.isSubset(three, DEV_NULL));
		assertFalse("Three are not a subset of none",
				zero.isSubset(one, DEV_NULL));
		assertFalse("Two are not a subset of none",
				zero.isSubset(two, DEV_NULL));
	}

	/**
	 * A test of Fortress's subset feature.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testFortressSubset() {
		final Fortress one = new Fortress(new Player(1, ONE_STR), "fOne", 1);
		final Fortress two = new Fortress(new Player(2, "two"), "fOne", 1);
		assertFalse("Subset requires same owner, first test",
				one.isSubset(two, DEV_NULL));
		assertFalse("Subset requires same owner, second test",
				two.isSubset(one, DEV_NULL));
		final Fortress three = new Fortress(new Player(1, ONE_STR), "fTwo", 2);
		assertFalse("Subset requires same name, first test",
				one.isSubset(three, DEV_NULL));
		assertFalse("Subset requires same name, second test",
				three.isSubset(one, DEV_NULL));
		final Fortress four = new Fortress(new Player(1, ONE_STR), "fOne", 3);
		assertTrue("Subset doesn't require identiy or ID equality",
				one.isSubset(four, DEV_NULL));
		assertTrue("Subset doesn't require identiy or ID equality",
				four.isSubset(one, DEV_NULL));
		four.addUnit(new Unit(new Player(2, "two"), "unit_type", "unit_name", 4));
		assertTrue("Fortress without is a subset of fortress with unit",
				four.isSubset(one, DEV_NULL));
		assertFalse("Fortress with is not a subset of fortress without unit",
				one.isSubset(four, DEV_NULL));
	}

	/**
	 * A test of Tile's subset feature.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testTileSubset() {
		final Tile one = new Tile(TileType.Steppe);
		final Tile three = new Tile(TileType.Desert);
		assertFalse("Subset tile must match in type",
				one.isSubset(three, DEV_NULL));
		assertFalse("Subset tile must match in type",
				three.isSubset(one, DEV_NULL));
		final Tile four = new Tile(TileType.Steppe);
		assertTrue("Tile is subset of self", one.isSubset(one, DEV_NULL));
		assertTrue("Tile is subset of equal tile",
				one.isSubset(four, DEV_NULL));
		assertTrue("Tile is subset of equal tile",
				four.isSubset(one, DEV_NULL));
		four.addFixture(new Mountain());
		assertTrue("Tile with fixture is subset of tile without",
				four.isSubset(one, DEV_NULL));
		assertFalse("Tile without fixture is not subset of tile with",
				one.isSubset(four, DEV_NULL));
		one.addFixture(new Mountain());
		assertTrue("Adding equal fixture makes it again a subset",
				one.isSubset(four, DEV_NULL));
		four.addFixture(new CacheFixture("category", "contents", 1));
		assertTrue("Subset calculation skips Caches",
				one.isSubset(four, DEV_NULL));
		four.addFixture(new TextFixture("text", -1));
		assertTrue("Subset calculation skips arbitrary-text",
				one.isSubset(four, DEV_NULL));
		four.addFixture(new Animal("animal", true, false, "wild", 2));
		assertTrue("Subset calculation skips animal tracks ...",
				one.isSubset(four, DEV_NULL));
		four.addFixture(new Animal("animal", false, false, "wild", 3));
		assertFalse("But not the animals themselves",
				one.isSubset(four, DEV_NULL));
	}

	/**
	 * Test the TileCollection subset feature.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testTileCollectionSubset() {
		final TileCollection zero = new TileCollection();
		final TileCollection one = new TileCollection();
		final Point pointOne = PointFactory.point(0, 0);
		one.addTile(pointOne, new Tile(TileType.Jungle));
		final TileCollection two = new TileCollection();
		two.addTile(pointOne, new Tile(TileType.Jungle));
		final Point pointTwo = PointFactory.point(1, 1);
		two.addTile(pointTwo, new Tile(TileType.Ocean));
		assertTrue("None is a subset of itself",
				zero.isSubset(zero, DEV_NULL));
		assertTrue("None is a subset of one", one.isSubset(zero, DEV_NULL));
		assertTrue("None is a subset of two", two.isSubset(zero, DEV_NULL));
		assertFalse("One is not a subset of none",
				zero.isSubset(one, DEV_NULL));
		assertTrue("One is a subset of itself", one.isSubset(one, DEV_NULL));
		assertTrue("One is a subset of two", two.isSubset(one, DEV_NULL));
		assertFalse("TWo is not a subset of none",
				zero.isSubset(two, DEV_NULL));
		assertFalse("Two is not a subset of one", one.isSubset(two, DEV_NULL));
		assertTrue("Two is a subset of itself", two.isSubset(two, DEV_NULL));
		one.addTile(pointTwo, new Tile(TileType.Plains));
		assertFalse("Corresponding but non-matching tile breaks subset",
				two.isSubset(one, DEV_NULL));
		assertFalse("Corresponding but non-matching tile breaks subset",
				one.isSubset(two, DEV_NULL));
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TestSubsets";
	}
}
