package model.exploration.old;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import model.map.ITile;
import model.map.Point;
import model.map.PointFactory;
import model.map.River;
import model.map.TileFixture;
import model.map.TileType;

import org.junit.Before;
import org.junit.Test;

/**
 * A test case for TestExplorationRunner.
 *
 * @author Jonathan Lovelace
 *
 */
// ESCA-JAVA0137:
public final class TestExplorationRunner {
	/**
	 * Extracted constant, to fix a warning because it occurred three or more
	 * times.
	 */
	private static final String TEST_THREE = "test_three";
	/**
	 * Extracted constant, to fix a warning because it occurred three or more
	 * times.
	 */
	private static final String TEST_TABLE_THREE = "test_table_three";
	/**
	 * Extracted constant, to fix a warning because it occurred three or more
	 * times.
	 */
	private static final String TEST_TABLE_TWO = "test_table_two";
	/**
	 * Extracted constant, to fix a warning because it occurred three or more
	 * times.
	 */
	private static final String TEST_TABLE_ONE = "test_table_one";
	/**
	 * The object we're testing.
	 */
	private ExplorationRunner runner = new ExplorationRunner();

	/**
	 * Set up for the tests.
	 */
	// ESCA-JAVA0160:
	@Before
	public void setUp() {
		runner = new ExplorationRunner();
	}

	/**
	 * Test the getPrimaryRock method.
	 *
	 * TODO: Use a mock object rather than a real object for the Table.
	 *
	 * @throws MissingTableException if the table is missing
	 */
	@Test
	public void testGetPrimaryRock() throws MissingTableException {
		runner.loadTable("major_rock", new ConstantTable("primary_rock_test"));
		final Point point = PointFactory.point(0, 0);
		final MockTile mock = new MockTile(TileType.Tundra);
		assertEquals("primary rock test", runner.getPrimaryRock(point, mock),
				"primary_rock_test");
	}
	/**
	 * A mock-object for a Tile that only allows its terrain type to be queried.
	 */
	private static class MockTile implements ITile {
		/**
		 * Whether the getTerrain method has been called.
		 */
		private boolean called = false;
		/**
		 * The terrain type.
		 */
		private final TileType terrain;
		/**
		 * An exception to throw if an unexpected method is called.
		 */
		private final IllegalStateException ise = new IllegalStateException(
				"Unexpected method called on mock object");
		/**
		 * Constructor.
		 * @param type the terrain type to report
		 */
		protected MockTile(final TileType type) {
			terrain = type;
		}

		/**
		 * @param obj
		 *            ignored
		 * @param ostream
		 *            ignored
		 * @param context
		 *            ignored
		 * @return nothing; always throws
		 * @throws IOException
		 *             never
		 */
		@Override
		public boolean isSubset(final ITile obj, final Appendable ostream,
				final String context) throws IOException {
			throw ise;
		}
		/**
		 * @return nothing, always throws
		 */
		@Override
		public Iterator<TileFixture> iterator() {
			throw ise;
		}
		/**
		 * @return nothing, always throws
		 */
		@Override
		public boolean isEmpty() {
			throw ise;
		}
		/**
		 * @return nothing, always throws
		 */
		@Override
		public boolean hasRiver() {
			throw ise;
		}
		/**
		 * @return nothing, always throws
		 */
		@Override
		public Iterable<River> getRivers() {
			throw ise;
		}
		/**
		 * @return the terrain type we were told
		 */
		@Override
		public TileType getTerrain() {
			called = true;
			return terrain;
		}
		/**
		 * @return whether the getTerrain method has been called
		 */
		protected boolean wasCalled() {
			return called;
		}
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			throw ise;
		}
	}
	/**
	 * Test the getPrimaryTree method.
	 *
	 * TODO Use mock objects rather than real Tables.
	 *
	 * @throws MissingTableException if a table is missing
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetPrimaryTree() throws MissingTableException {
		runner.loadTable("boreal_major_tree", new ConstantTable(
				"boreal_major_test"));
		runner.loadTable("temperate_major_tree", new ConstantTable(
				"temperate_major_test"));
		final Point point = PointFactory.point(0, 0);
		final MockTile mockOne = new MockTile(TileType.BorealForest);
		assertEquals("primary tree test for boreal forest",
				runner.getPrimaryTree(point, mockOne), "boreal_major_test");
		assertTrue("Primary tree test queried the tile type", mockOne.wasCalled());
		final MockTile mockTwo = new MockTile(TileType.TemperateForest);
		assertEquals("primary tree test for temperate forest",
				runner.getPrimaryTree(point, mockTwo), "temperate_major_test");
		assertTrue("Primary tree test queried tile type", mockTwo.wasCalled());
	}

	/**
	 * Test that getPrimaryTree objects to non-forest tiles.
	 *
	 * @throws MissingTableException if a table is missing
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalGetPrimaryTree() throws MissingTableException {
		final Point point = PointFactory.point(0, 0);
		runner.getPrimaryTree(point, new MockTile(TileType.Tundra));
		fail("gave a primary tree for non-forest");
	}

	/**
	 * Test the consultTable method.
	 *
	 * TODO: Use a mock object rather than a real object for the Tables.
	 *
	 * @throws MissingTableException if a table is missing
	 */
	@Test
	public void testConsultTable() throws MissingTableException {
		runner.loadTable(TEST_TABLE_ONE, new ConstantTable("test_one"));
		runner.loadTable(TEST_TABLE_TWO, new ConstantTable("test_two"));
		runner.loadTable(TEST_TABLE_THREE, new ConstantTable(TEST_THREE));
		final Point point = PointFactory.point(0, 0);
		final MockTile mock = new MockTile(TileType.Tundra);
		assertEquals("first table", runner.consultTable(TEST_TABLE_ONE, point,
				mock), "test_one");
		assertEquals("second table", runner.consultTable(TEST_TABLE_TWO, point,
				mock), "test_two");
		assertEquals("third table", runner.consultTable(TEST_TABLE_THREE,
				point, mock), TEST_THREE);
	}

	/**
	 * Test the recursiveConsultTable method: the one method under test whose
	 * correctness is nonobvious.
	 *
	 * TODO: Use a mock object rather than a real object for the Tables.
	 *
	 * @throws MissingTableException if a table is missing
	 */
	@Test
	public void testRecursiveConsultTable() throws MissingTableException {
		runner.loadTable(TEST_TABLE_ONE, new ConstantTable(
				"( #test_table_two# )"));
		runner.loadTable(TEST_TABLE_TWO, new ConstantTable(
				"( #test_table_three# )"));
		runner.loadTable(TEST_TABLE_THREE, new ConstantTable(TEST_THREE));
		runner.loadTable("test_table_four", new ConstantTable(
				"_ #test_table_one"));
		final Point point = PointFactory.point(0, 0);
		final MockTile mockOne = new MockTile(TileType.Tundra);
		assertEquals("two levels of recursion",
				runner.recursiveConsultTable(TEST_TABLE_ONE, point, mockOne),
				"( ( test_three ) )");
		assertEquals("one level of recursion",
				runner.recursiveConsultTable(TEST_TABLE_TWO, point, mockOne),
				"( test_three )");
		assertEquals("no recursion",
				runner.recursiveConsultTable(TEST_TABLE_THREE, point, mockOne),
				TEST_THREE);
		final MockTile mockTwo = new MockTile(TileType.Plains);
		assertEquals(
				"one-sided split",
				runner.recursiveConsultTable("test_table_four", point, mockTwo),
				"_ ( ( test_three ) )");
	}

	/**
	 * Test the defaultResults() method.
	 *
	 * @throws MissingTableException if a table is missing
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testDefaultResults() throws MissingTableException {
		runner.loadTable("major_rock", new ConstantTable("test_rock"));
		runner.loadTable("boreal_major_tree", new ConstantTable("boreal_tree"));
		runner.loadTable("temperate_major_tree", new ConstantTable(
				"temperate_tree"));
		final Point point = PointFactory.point(0, 0);
		final MockTile mockOne = new MockTile(TileType.Tundra);
		assertEquals("defaultResults in non-forest",
				"The primary rock type here is test_rock.\n",
				runner.defaultResults(point, mockOne));
		assertTrue("Default results checks tile type", mockOne.wasCalled());
		final MockTile mockTwo = new MockTile(TileType.BorealForest);
		assertEquals("defaultResults in boreal forest",
				"The primary rock type here is test_rock.\n"
						+ "The main kind of tree is boreal_tree.\n",
				runner.defaultResults(point, mockTwo));
		assertTrue("Default results checks tile type", mockTwo.wasCalled());
		final MockTile mockThree = new MockTile(TileType.TemperateForest);
		assertEquals("defaultResults in temperate forest",
				"The primary rock type here is test_rock.\n"
						+ "The main kind of tree is temperate_tree.\n",
				runner.defaultResults(point, mockThree));
		assertTrue("Default results checks tile type", mockThree.wasCalled());
	}

	/**
	 *
	 * Test recursive checking. Note that the method returns true if the table
	 * in question, or one it references, does *not* exist.
	 */
	@Test
	public void testRecursiveCheck() {
		runner.loadTable("existent_table", new ConstantTable("exists"));
		assertTrue("base case of non-existent table",
				runner.recursiveCheck("nonexistenttable"));
		assertFalse("base case of existent table",
				runner.recursiveCheck("existent_table"));
		runner.loadTable("referent_one", new ConstantTable("#existent_table#"));
		runner.loadTable("referent_two", new ConstantTable(
				"( #existent_table# )"));
		runner.loadTable(
				"referent_three",
				new QuadrantTable(1, new ArrayList<>(Arrays
						.asList(new String[] { "#referent_one#",
								"#referent_two#" }))));
		assertFalse("recursive case to exercise cache-hits",
				runner.recursiveCheck("referent_three"));
		runner.loadTable("false_referent", new ConstantTable("#nonexistent#"));
		assertTrue("reference to nonexistent table",
				runner.recursiveCheck("false_referent"));
	}

	/**
	 * Test the recursiveCheck() method.
	 */
	@Test
	public void testGlobalRecursiveCheck() {
		assertFalse("recursive check with no tables", runner.recursiveCheck());
		runner.loadTable("existent", new ConstantTable("true_table"));
		assertFalse("recursive check with only valid tables",
				runner.recursiveCheck());
		runner.loadTable("false_ref", new ConstantTable("#false#"));
		assertTrue("recursive check with an invalid table",
				runner.recursiveCheck());
	}

	/**
	 *
	 * @return a String representation of the class
	 */
	@Override
	public String toString() {
		return "TestExplorationRunner";
	}
}
