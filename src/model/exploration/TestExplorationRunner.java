package model.exploration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import model.viewer.Tile;
import model.viewer.TileType;

import org.junit.Before;
import org.junit.Test;
/**
 * A test case for TestExplorationRunner
 * @author Jonathan Lovelace
 *
 */
public class TestExplorationRunner {
	/**
	 * The object we're testing.
	 */
	private ExplorationRunner runner;
	/**
	 * Set up for the tests.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		runner = new ExplorationRunner();
	}
	/**
	 * Test the getPrimaryRock method.
	 * @todo Use a mock object rather than a real object for the Tile, and even for the Table.
	 */
	@Test
	public void testGetPrimaryRock() {
		runner.loadTable("major_rock", new ConstantTable("primary_rock_test"));
		assertEquals(runner.getPrimaryRock(new Tile(0, 0, TileType.Tundra)), "primary_rock_test");
	}
	/**
	 * Test the getPrimaryTree method.
	 * @todo Use a mock object rather than a real object for the Tile, and even for the Tables.
	 */
	@Test
	public void testGetPrimaryTree() {
		runner.loadTable("boreal_major_tree", new ConstantTable("boreal_major_test"));
		runner.loadTable("temperate_major_tree", new ConstantTable("temperate_major_test"));
		assertEquals(runner.getPrimaryTree(new Tile(0, 0, TileType.BorealForest)), "boreal_major_test");
		assertEquals(runner.getPrimaryTree(new Tile(0, 0, TileType.TemperateForest)), "temperate_major_test");
	}
	/**
	 * Test the consultTable method.
	 * @todo Use a mock object rather than a real object for the Tile, and even for the Tables.
	 */
	@Test
	public void testConsultTable() {
		runner.loadTable("test_table_one", new ConstantTable("test_one"));
		runner.loadTable("test_table_two", new ConstantTable("test_two"));
		runner.loadTable("test_table_three", new ConstantTable("test_three"));
		assertEquals(runner.consultTable("test_table_one", new Tile(0, 0, TileType.Tundra)), "test_one");
		assertEquals(runner.consultTable("test_table_two", new Tile(0, 0, TileType.Tundra)), "test_two");
		assertEquals(runner.consultTable("test_table_three", new Tile(0, 0, TileType.Tundra)), "test_three");
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
		runner.loadTable("test_table_one", new ConstantTable("( #test_table_two# )"));
		runner.loadTable("test_table_two", new ConstantTable("( #test_table_three# )"));
		runner.loadTable("test_table_three", new ConstantTable("test_three"));
		assertEquals(runner.recursiveConsultTable("test_table_one", new Tile(0, 0, TileType.Tundra)), "( ( test_three ) )");
		assertEquals(runner.recursiveConsultTable("test_table_two", new Tile(0, 0, TileType.Tundra)), "( test_three )");
		assertEquals(runner.recursiveConsultTable("test_table_three", new Tile(0, 0, TileType.Tundra)), "test_three");
	}

}
