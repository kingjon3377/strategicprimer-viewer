package drivers.exploration.old;
import java.util.Deque;
import java.util.LinkedList;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import common.map.Point;
import common.map.TileType;
import common.map.MapDimensionsImpl;
import common.map.MapDimensions;

import common.map.fixtures.terrain.Forest;
import java.util.Collections;
import java.util.Arrays;

/**
 * Tests of the {@link ExplorationRunner}.
 */
public class TestExplorationRunner {
	/**
	 * Test of the {@link ExplorationRunner#getPrimaryRock} method.
	 */
	@Test
	public void testGetPrimaryRock() throws MissingTableException {
		ExplorationRunner runner = new ExplorationRunner();
		runner.loadTable("major_rock", new MockTable("primary_rock_test"));
		assertEquals("primary_rock_test", runner.getPrimaryRock(new Point(0, 0),
			TileType.Tundra, false, Collections.emptyList(), new MapDimensionsImpl(69, 88, 2)),
			"primary rock test");
	}

	/**
	 * Test of the {@link ExplorationRunner#getPrimaryTree} method.
	 */
	@Test
	public void testGetPrimaryTree() throws MissingTableException {
		ExplorationRunner runner = new ExplorationRunner();
		runner.loadTable("boreal_major_tree", new MockTable("boreal_major_test"));
		runner.loadTable("temperate_major_tree", new MockTable("temperate_major_test"));
		Point point = new Point(0, 0);
		MapDimensions dimensions = new MapDimensionsImpl(69, 88, 2);
		assertEquals("boreal_major_test", runner.getPrimaryTree(point, TileType.Steppe, false,
			Arrays.asList(new Forest("kind", false, 3)), dimensions),
			"primary tree test for forest in steppe");
		assertEquals("temperate_major_test", runner.getPrimaryTree(point, TileType.Plains, false,
			Arrays.asList(new Forest("second", false, 4)), dimensions),
			"primary tree test for forest in plains");
	}

	/**
	 * Test that {@link ExplorationRunner#getPrimaryTree} objects to being called on non-forested tiles.
	 */
	@Test
	public void testIllegalGetPrimaryTree() throws MissingTableException {
		assertThrows(IllegalArgumentException.class,
			() -> new ExplorationRunner().getPrimaryTree(new Point(0, 0), TileType.Tundra,
				false, Collections.emptyList(), new MapDimensionsImpl(69, 88, 2)),
			"getPrimaryTree objects to non-forested tile input");
	}

	/**
	 * Test the {@link ExplorationRunner#consultTable} method.
	 */
	@Test
	public void testConsultTable() throws MissingTableException {
		ExplorationRunner runner = new ExplorationRunner();
		runner.loadTable("test_table_one", new MockTable("test_one"));
		runner.loadTable("test_table_two", new MockTable("test_two"));
		runner.loadTable("test_table_three", new MockTable("test_three"));
		Point point = new Point(0, 0);
		MapDimensions dimensions = new MapDimensionsImpl(69, 88, 2);
		assertEquals("test_one", runner.consultTable("test_table_one", point,
			TileType.Tundra, false, Collections.emptyList(), dimensions), "first table");
		assertEquals("test_two", runner.consultTable("test_table_two", point,
			TileType.Tundra, false, Collections.emptyList(), dimensions), "second table");
		assertEquals("test_three", runner.consultTable("test_table_three", point,
			TileType.Tundra, false, Collections.emptyList(), dimensions), "third table");
	}

	/**
	 * Test the {@link ExplorationRunner#recursiveConsultTable} method: the one
	 * method under test whose correctness is non-obvious. We don't use
	 * mock tables here because setting them up would be more trouble than
	 * they're worth.
	 */
	@Test
	public void testRecursiveConsultTable() throws MissingTableException {
		ExplorationRunner runner = new ExplorationRunner();
		runner.loadTable("test_table_one", new ConstantTable("( #test_table_two# )"));
		runner.loadTable("test_table_two", new ConstantTable("( #test_table_three# )"));
		runner.loadTable("test_table_three", new ConstantTable("test_three"));
		runner.loadTable("test_table_four", new ConstantTable("_ #test_table_one"));
		Point point = new Point(0, 0);
		MapDimensions dimensions = new MapDimensionsImpl(69, 88, 2);
		assertEquals("( ( test_three ) )", runner.recursiveConsultTable("test_table_one", point,
			TileType.Tundra, false, Collections.emptyList(), dimensions),
			"two levels of recursion");
		assertEquals("( test_three )", runner.recursiveConsultTable("test_table_two", point,
			TileType.Tundra, false, Collections.emptyList(), dimensions),
			"one level of recursion");
		assertEquals("test_three", runner.recursiveConsultTable("test_table_three", point,
			TileType.Tundra, false, Collections.emptyList(), dimensions), "no recursion");
		assertEquals("_ ( ( test_three ) )", runner.recursiveConsultTable("test_table_four", point,
			TileType.Plains, false, Collections.emptyList(), dimensions), "one-sided split");
	}

	/**
	 * Test the {@link ExplorationRunner#defaultResults} method.
	 */
	@Test
	public void testDefaultResults() throws MissingTableException {
		ExplorationRunner runner = new ExplorationRunner();
		runner.loadTable("major_rock", new ConstantTable("test_rock"));
		runner.loadTable("boreal_major_tree", new ConstantTable("boreal_tree"));
		runner.loadTable("temperate_major_tree", new ConstantTable("temperate_tree"));
		Point point = new Point(0, 0);
		MapDimensions dimensions = new MapDimensionsImpl(69, 88, 0);
		assertEquals("The primary rock type here is test_rock.",
			runner.defaultResults(point, TileType.Tundra, false, Collections.emptyList(),
				dimensions), "defaultResults in non-forest");
		assertEquals(String.format("The primary rock type here is test_rock.%n" +
				"The main kind of tree here is boreal_tree.%n"),
			runner.defaultResults(point, TileType.Steppe, false, Arrays.asList(
				new Forest("boreal_tree", false, 1)), dimensions),
			"defaultResults in boreal forest");
		assertEquals(String.format("The primary rock type here is test_rock.%n" +
				"The main kind of tree here is temperate_tree.%n"),
			runner.defaultResults(point, TileType.Plains, false, Arrays.asList(
				new Forest("temperate_tree", false, 2)), dimensions),
				"defaultResults in temperate forest");
	}

	/**
	 * Test recursive checking. Note that the method returns true if the
	 * table in question, or one it references, does <em>not</em> exist.
	 */
	@Test
	public void testRecursiveCheck() throws MissingTableException {
		ExplorationRunner runner = new ExplorationRunner();
		runner.loadTable("existent_table", new ConstantTable("exists"));
		assertTrue(runner.recursiveCheck("non-existent-table"),
			"base case of non-existent table");
		assertFalse(runner.recursiveCheck("existent_table"),
			"base case of existing table");
		runner.loadTable("referent_one", new ConstantTable("#existent_table#"));
		runner.loadTable("referent_two", new ConstantTable("( #existent_table# )"));
		runner.loadTable("referent_three", new QuadrantTable(1, "#referent_one#",
			"#referent_two#"));
		assertFalse(runner.recursiveCheck("referent_three"),
			"recursive case to exercise cache-hits");
		runner.loadTable("false_referent", new ConstantTable("#nonexistent#"));
		assertTrue(runner.recursiveCheck("false_referent"),
			"reference to nonexistent table");
	}

	/**
	 * Test global-recursive checking. Note that the method returns
	 * <em>true</em> if any table references a nonexistent table, and
	 * <em>false</em> if the graph is self-complete.
	 */
	@Test
	public void testGlobalRecursiveCheck() throws MissingTableException {
		ExplorationRunner runner = new ExplorationRunner();
		assertFalse(runner.globalRecursiveCheck(), "recursive check with no tables");
		runner.loadTable("existent", new ConstantTable("true_table"));
		assertFalse(runner.globalRecursiveCheck(), "recursive check with only valid tables");
		runner.loadTable("false_ref", new ConstantTable("#false#"));
		assertTrue(runner.globalRecursiveCheck(), "recursive check with an invalid table");
	}

	/**
	 * Test loading {@link QuadrantTable quadrant-based tables}.
	 */
	@Test
	public void testLoadQuadrantTable() throws MissingTableException, IOException {
		ExplorationRunner runner = new ExplorationRunner();
		Deque<String> data = new LinkedList<>(Arrays.asList("quadrant", "2", "one", "two", "three",
			"four", "five", "six"));
		String table = "testLoadQuadrantTable().result";
		runner.loadTableFromDataStream(data.iterator(), table);
		Point point = new Point(0, 0);
		MapDimensions dimensions = new MapDimensionsImpl(69, 88, 2);
		assertEquals("one", runner.consultTable(table, point, TileType.Tundra, false,
			Collections.emptyList(), dimensions), "loading quadrant table");
		Point pointTwo = new Point(36, 30);
		assertEquals("one", runner.consultTable(table, point, TileType.Ocean, false,
			Collections.emptyList(), dimensions), "quadrant table isn't a terrain table");
		assertEquals("five", runner.consultTable(table, pointTwo, TileType.Tundra, false,
			Collections.emptyList(), dimensions), "quadrant table isn't a constant table");
		assertEquals("six", runner.consultTable(table, pointTwo, TileType.Tundra, false,
				Collections.emptyList(), new MapDimensionsImpl(35, 32, 2)),
			"quadrant table can use alternate dimensions");
		assertThrows(IllegalArgumentException.class,
			() -> runner.loadTableFromDataStream(
				new LinkedList(Arrays.asList("quadrant")).iterator(),
				"testLoadQuadrantTable().illegal"));
	}

	/**
	 * A mock object to make sure that tables other than {@link
	 * QuadrantTable} do not use the map dimensions.
	 */
	private static class MockDimensions implements MapDimensions {
		public static final MockDimensions INSTANCE = new MockDimensions();
		private MockDimensions() {}
		@Override
		public int getRows() {
			throw new IllegalStateException("Should not be called");
		}

		@Override
		public int getColumns() {
			throw new IllegalStateException("Should not be called");
		}

		@Override
		public int getVersion() {
			throw new IllegalStateException("Should not be called");
		}
	}

	private final MockDimensions mockDimensions = MockDimensions.INSTANCE;

	/**
	 * Test loading {@link RandomTable random-number-based tables}.
	 */
	@Test
	public void testLoadRandomTable() throws MissingTableException, IOException {
		ExplorationRunner runner = new ExplorationRunner();
		String table = "testLoadRandomTable()";
		runner.loadTableFromDataStream(
			new LinkedList<>(Arrays.asList("random", "0 one", "99 two")).iterator(),
			table);
		assertEquals("one", runner.consultTable(table, Point.INVALID_POINT, TileType.Tundra,
			false, Collections.emptyList(), mockDimensions), "loading random table");
	}

	/**
	 * Test loading {@link TerrainTable terrain-based tables}.
	 */
	@Test
	public void testLoadTerrainTable() throws MissingTableException, IOException {
		ExplorationRunner runner = new ExplorationRunner();
		String table = "testLoadTerrainTable()";
		runner.loadTableFromDataStream(new LinkedList<>(Arrays.asList("terrain", "tundra one",
				"plains two", "ocean three", "mountain four",
				"temperate_forest five")).iterator(),
			table);
		assertEquals("one", runner.consultTable(table, Point.INVALID_POINT, TileType.Tundra,
			false, Collections.emptyList(), mockDimensions), "loading terrain table: tundra");
		assertEquals("two", runner.consultTable(table, Point.INVALID_POINT, TileType.Plains,
			false, Collections.emptyList(), mockDimensions), "loading terrain table: plains");
		assertEquals("three", runner.consultTable(table, Point.INVALID_POINT, TileType.Ocean, false,
			Collections.emptyList(), mockDimensions), "loading terrain table: ocean");
		assertEquals("five", runner.consultTable(table, Point.INVALID_POINT, TileType.Plains,
			false, Arrays.asList(new Forest("forestKind", false, 1)), mockDimensions),
			"loading terrain table: version 2 equivalent of temperate forest");
		assertEquals("four", runner.consultTable(table, Point.INVALID_POINT, TileType.Plains, true,
			Collections.emptyList(), mockDimensions),
			"loading terrain table: version 2 equivalent of mountain");
	}

	/**
	 * Test loading {@link ConstantTable constant "tables"}
	 */
	@Test
	public void testLoadConstantTable() throws MissingTableException, IOException {
		ExplorationRunner runner = new ExplorationRunner();
		String table = "testLoadConstantTable()";
		runner.loadTableFromDataStream(
			new LinkedList<>(Arrays.asList("constant", "one")).iterator(), table);
		assertEquals("one", runner.consultTable(table, Point.INVALID_POINT, TileType.Plains,
			false, Collections.emptyList(), mockDimensions), "one");
	}

	/**
	 * Test that the table-loading code correctly rejects invalid input.
	 */
	@Test
	public void testTableLoadingInvalidInput() throws MissingTableException {
		ExplorationRunner runner = new ExplorationRunner();
		// no data
		assertThrows(IllegalArgumentException.class,
			() -> runner.loadTableFromDataStream(
				new LinkedList<>(Arrays.asList("")).iterator(),
				"testTableLoadingInvalidInput().noData"));
		// invalid header
		assertThrows(IllegalArgumentException.class,
			() -> runner.loadTableFromDataStream(new LinkedList<>(Arrays.asList("2",
					"invalidData", "invalidData")).iterator(),
				"testTableLoadingInvalidInput().invalidHeader"));
	}
}
