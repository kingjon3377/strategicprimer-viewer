package model.map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static view.util.NullStream.BIT_BUCKET;
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
		assertTrue("Empty is subset of self", zero.isSubset(zero, BIT_BUCKET));
		assertTrue("Empty is subset of one", one.isSubset(zero, BIT_BUCKET));
		assertTrue("Empty is subset of two", two.isSubset(zero, BIT_BUCKET));
		assertFalse("One is not subset of empty",
				zero.isSubset(one, BIT_BUCKET));
		assertTrue("One is subset of self", one.isSubset(one, BIT_BUCKET));
		assertTrue("One is subset of two", two.isSubset(one, BIT_BUCKET));
		assertFalse("Two is not subset of empty",
				zero.isSubset(two, BIT_BUCKET));
		assertFalse("Two is not subset of one", one.isSubset(two, BIT_BUCKET));
		assertTrue("Two is subset of self", two.isSubset(two, BIT_BUCKET));
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
		assertTrue("None is a subset of all", three.isSubset(zero, BIT_BUCKET));
		assertTrue("Three are a subset of all", three.isSubset(one, BIT_BUCKET));
		assertTrue("Two are a subset of all", three.isSubset(two, BIT_BUCKET));
		assertTrue("All is a subset of all", three.isSubset(three, BIT_BUCKET));
		assertTrue("None is a subset of two", two.isSubset(zero, BIT_BUCKET));
		assertFalse("All is not a subset of two",
				two.isSubset(three, BIT_BUCKET));
		assertFalse("Three are not a subset of two",
				two.isSubset(one, BIT_BUCKET));
		assertTrue("Two are a subset of themselves",
				two.isSubset(two, BIT_BUCKET));
		assertTrue("None is a subset of three", one.isSubset(zero, BIT_BUCKET));
		assertFalse("All is not a subset of three",
				one.isSubset(three, BIT_BUCKET));
		assertFalse("A different two are not a subset of three",
				one.isSubset(two, BIT_BUCKET));
		assertTrue("Three are a subset of themselves",
				one.isSubset(one, BIT_BUCKET));
		assertTrue("None is a subset of itself",
				zero.isSubset(zero, BIT_BUCKET));
		assertFalse("All are not a subset of none",
				zero.isSubset(three, BIT_BUCKET));
		assertFalse("Three are not a subset of none",
				zero.isSubset(one, BIT_BUCKET));
		assertFalse("Two are not a subset of none",
				zero.isSubset(two, BIT_BUCKET));
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
				one.isSubset(two, BIT_BUCKET));
		assertFalse("Subset requires same owner, second test",
				two.isSubset(one, BIT_BUCKET));
		final Fortress three = new Fortress(new Player(1, ONE_STR), "fTwo", 2);
		assertFalse("Subset requires same name, first test",
				one.isSubset(three, BIT_BUCKET));
		assertFalse("Subset requires same name, second test",
				three.isSubset(one, BIT_BUCKET));
		final Fortress four = new Fortress(new Player(1, ONE_STR), "fOne", 3);
		assertTrue("Subset doesn't require identiy or ID equality",
				one.isSubset(four, BIT_BUCKET));
		assertTrue("Subset doesn't require identiy or ID equality",
				four.isSubset(one, BIT_BUCKET));
		four.addUnit(new Unit(new Player(2, "two"), "unit_type", "unit_name", 4));
		assertTrue("Fortress without is a subset of fortress with unit",
				four.isSubset(one, BIT_BUCKET));
		assertFalse("Fortress with is not a subset of fortress without unit",
				one.isSubset(four, BIT_BUCKET));
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
				one.isSubset(three, BIT_BUCKET));
		assertFalse("Subset tile must match in type",
				three.isSubset(one, BIT_BUCKET));
		final Tile four = new Tile(TileType.Steppe);
		assertTrue("Tile is subset of self", one.isSubset(one, BIT_BUCKET));
		assertTrue("Tile is subset of equal tile",
				one.isSubset(four, BIT_BUCKET));
		assertTrue("Tile is subset of equal tile",
				four.isSubset(one, BIT_BUCKET));
		four.addFixture(new Mountain());
		assertTrue("Tile with fixture is subset of tile without",
				four.isSubset(one, BIT_BUCKET));
		assertFalse("Tile without fixture is not subset of tile with",
				one.isSubset(four, BIT_BUCKET));
		one.addFixture(new Mountain());
		assertTrue("Adding equal fixture makes it again a subset",
				one.isSubset(four, BIT_BUCKET));
		four.addFixture(new CacheFixture("category", "contents", 1));
		assertTrue("Subset calculation skips Caches",
				one.isSubset(four, BIT_BUCKET));
		four.addFixture(new TextFixture("text", -1));
		assertTrue("Subset calculation skips arbitrary-text",
				one.isSubset(four, BIT_BUCKET));
		four.addFixture(new Animal("animal", true, false, "wild", 2));
		assertTrue("Subset calculation skips animal tracks ...",
				one.isSubset(four, BIT_BUCKET));
		four.addFixture(new Animal("animal", false, false, "wild", 3));
		assertFalse("But not the animals themselves",
				one.isSubset(four, BIT_BUCKET));
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
				zero.isSubset(zero, BIT_BUCKET));
		assertTrue("None is a subset of one", one.isSubset(zero, BIT_BUCKET));
		assertTrue("None is a subset of two", two.isSubset(zero, BIT_BUCKET));
		assertFalse("One is not a subset of none",
				zero.isSubset(one, BIT_BUCKET));
		assertTrue("One is a subset of itself", one.isSubset(one, BIT_BUCKET));
		assertTrue("One is a subset of two", two.isSubset(one, BIT_BUCKET));
		assertFalse("TWo is not a subset of none",
				zero.isSubset(two, BIT_BUCKET));
		assertFalse("Two is not a subset of one", one.isSubset(two, BIT_BUCKET));
		assertTrue("Two is a subset of itself", two.isSubset(two, BIT_BUCKET));
		one.addTile(pointTwo, new Tile(TileType.Plains));
		assertFalse("Corresponding but non-matching tile breaks subset",
				two.isSubset(one, BIT_BUCKET));
		assertFalse("Corresponding but non-matching tile breaks subset",
				one.isSubset(two, BIT_BUCKET));
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TestSubsets";
	}
}
