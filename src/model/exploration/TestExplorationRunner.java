package model.exploration;

import static org.junit.Assert.assertEquals;
import model.viewer.Tile;
import model.viewer.TileType;

import org.junit.Before;
import org.junit.Test;

/**
 * A test case for TestExplorationRunner
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
	 * @todo Use a mock object rather than a real object for the Tile, and even
	 *       for the Table.
	 */
	@Test
	public void testGetPrimaryRock() {
		runner.loadTable("major_rock", new ConstantTable("primary_rock_test"));
		assertEquals("primary rock test",
				runner.getPrimaryRock(new Tile(0, 0, TileType.Tundra)),
				"primary_rock_test");
	}

	/**
	 * Test the getPrimaryTree method.
	 * 
	 * @todo Use a mock object rather than a real object for the Tile, and even
	 *       for the Tables.
	 */
	@Test
	public void testGetPrimaryTree() {
		runner.loadTable("boreal_major_tree", new ConstantTable(
				"boreal_major_test"));
		runner.loadTable("temperate_major_tree", new ConstantTable(
				"temperate_major_test"));
		assertEquals(
				runner.getPrimaryTree(new Tile(0, 0, TileType.BorealForest)),
				"boreal_major_test");
		assertEquals(
				runner.getPrimaryTree(new Tile(0, 0, TileType.TemperateForest)),
				"temperate_major_test");
	}

	/**
	 * Test the consultTable method.
	 * 
	 * @todo Use a mock object rather than a real object for the Tile, and even
	 *       for the Tables.
	 */
	@Test
	public void testConsultTable() {
		runner.loadTable(TEST_TABLE_ONE, new ConstantTable("test_one"));
		runner.loadTable(TEST_TABLE_TWO, new ConstantTable("test_two"));
		runner.loadTable(TEST_TABLE_THREE, new ConstantTable(TEST_THREE));
		assertEquals("first table", runner.consultTable(TEST_TABLE_ONE,
				new Tile(0, 0, TileType.Tundra)), "test_one");
		assertEquals("second table", runner.consultTable(TEST_TABLE_TWO,
				new Tile(0, 0, TileType.Tundra)), "test_two");
		assertEquals("third table", runner.consultTable(TEST_TABLE_THREE,
				new Tile(0, 0, TileType.Tundra)), TEST_THREE);
	}

	/**
	 * Test the recursiveConsultTable method: the one method under test whose
	 * correctness is nonobvious.
	 * 
	 * @todo Use a mock object rather than a real object for the Tile, and even
	 *       for the Tables.
	 */
	@Test
	public void testRecursiveConsultTable() {
		runner.loadTable("test_table_one", new ConstantTable(
				"( #test_table_two# )"));
		runner.loadTable("test_table_two", new ConstantTable(
				"( #test_table_three# )"));
		runner.loadTable("test_table_three", new ConstantTable("test_three"));
		assertEquals(runner.recursiveConsultTable("test_table_one", new Tile(0,
				0, TileType.Tundra)), "( ( test_three ) )");
		assertEquals(runner.recursiveConsultTable("test_table_two", new Tile(0,
				0, TileType.Tundra)), "( test_three )");
		assertEquals(runner.recursiveConsultTable("test_table_three", new Tile(
				0, 0, TileType.Tundra)), "test_three");
	}

}
