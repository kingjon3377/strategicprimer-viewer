package model.exploration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;

import model.map.Tile;
import model.map.TileType;

import org.junit.Before;
import org.junit.Test;

/**
 * A test case for TestExplorationRunner.
 * 
 * @author Jonathan Lovelace
 * 
 */
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
	private ExplorationRunner runner;

	/**
	 * To fix a warning.
	 */
	public TestExplorationRunner() {
		setUp();
	}

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
	 * TODO: Use a mock object rather than a real object for the Tile, and even
	 * for the Table.
	 * @throws MissingTableException if the table is missing
	 */
	@Test
	public void testGetPrimaryRock() throws MissingTableException {
		runner.loadTable("major_rock", new ConstantTable("primary_rock_test"));
		assertEquals("primary rock test",
				runner.getPrimaryRock(new Tile(0, 0, TileType.Tundra, "")),
				"primary_rock_test");
	}

	/**
	 * Test the getPrimaryTree method.
	 * 
	 * TODO Use a mock object rather than a real object for the Tile, and even
	 * for the Tables.
	 * @throws MissingTableException if a table is missing
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetPrimaryTree() throws MissingTableException {
		runner.loadTable("boreal_major_tree", new ConstantTable(
				"boreal_major_test"));
		runner.loadTable("temperate_major_tree", new ConstantTable(
				"temperate_major_test"));
		assertEquals("primary tree test for boreal forest",
				runner.getPrimaryTree(new Tile(0, 0, TileType.BorealForest, "")),
				"boreal_major_test");
		assertEquals(
				"primary tree test for temperate forest",
				runner.getPrimaryTree(new Tile(0, 0, TileType.TemperateForest, "")),
				"temperate_major_test");
	}

	/**
	 * Test that getPrimaryTree objects to non-forest tiles.
	 * @throws MissingTableException if a table is missing
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalGetPrimaryTree() throws MissingTableException {
		runner.getPrimaryTree(new Tile(0, 0, TileType.Tundra, ""));
		fail("gave a primary tree for non-forest");
	}

	/**
	 * Test the consultTable method.
	 * 
	 * TODO: Use a mock object rather than a real object for the Tile, and even
	 * for the Tables.
	 * @throws MissingTableException if a table is missing
	 */
	@Test
	public void testConsultTable() throws MissingTableException {
		runner.loadTable(TEST_TABLE_ONE, new ConstantTable("test_one"));
		runner.loadTable(TEST_TABLE_TWO, new ConstantTable("test_two"));
		runner.loadTable(TEST_TABLE_THREE, new ConstantTable(TEST_THREE));
		assertEquals("first table", runner.consultTable(TEST_TABLE_ONE,
				new Tile(0, 0, TileType.Tundra, "")), "test_one");
		assertEquals("second table", runner.consultTable(TEST_TABLE_TWO,
				new Tile(0, 0, TileType.Tundra, "")), "test_two");
		assertEquals("third table", runner.consultTable(TEST_TABLE_THREE,
				new Tile(0, 0, TileType.Tundra, "")), TEST_THREE);
	}

	/**
	 * Test the recursiveConsultTable method: the one method under test whose
	 * correctness is nonobvious.
	 * 
	 * TODO: Use a mock object rather than a real object for the Tile, and even
	 * for the Tables.
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
		assertEquals("two levels of recursion", runner.recursiveConsultTable(
				TEST_TABLE_ONE, new Tile(0, 0, TileType.Tundra, "")),
				"( ( test_three ) )");
		assertEquals("one level of recursion", runner.recursiveConsultTable(
				TEST_TABLE_TWO, new Tile(0, 0, TileType.Tundra, "")),
				"( test_three )");
		assertEquals("no recursion", runner.recursiveConsultTable(
				TEST_TABLE_THREE, new Tile(0, 0, TileType.Tundra, "")), TEST_THREE);
		assertEquals("one-sided split", runner.recursiveConsultTable(
				"test_table_four", new Tile(0, 0, TileType.Plains, "")),
				"_ ( ( test_three ) )");
	}

	/**
	 * Test the defaultResults() method.
	 * @throws MissingTableException if a table is missing
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testDefaultResults() throws MissingTableException {
		runner.loadTable("major_rock", new ConstantTable("test_rock"));
		runner.loadTable("boreal_major_tree", new ConstantTable("boreal_tree"));
		runner.loadTable("temperate_major_tree", new ConstantTable(
				"temperate_tree"));
		assertEquals("defaultResults in non-forest",
				"The primary rock type here is test_rock.\n",
				runner.defaultResults(new Tile(0, 0, TileType.Tundra, "")));
		assertEquals(
				"defaultResults in boreal forest",
				"The primary rock type here is test_rock.\nThe main kind of tree is boreal_tree.\n",
				runner.defaultResults(new Tile(0, 0, TileType.BorealForest, "")));
		assertEquals(
				"defaultResults in temperate forest",
				"The primary rock type here is test_rock.\nThe main kind of tree is temperate_tree.\n",
				runner.defaultResults(new Tile(0, 0, TileType.TemperateForest, "")));
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
				new QuadrantTable(1, new ArrayList<String>(Arrays
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
