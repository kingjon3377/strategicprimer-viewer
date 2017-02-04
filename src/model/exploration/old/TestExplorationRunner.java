package model.exploration.old;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import model.map.MapDimensions;
import model.map.MapDimensionsImpl;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import model.map.TileType;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * A test case for TestExplorationRunner.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TestExplorationRunner {
	/**
	 * The empty list.
	 */
	private static final Stream<TileFixture> EMPTY = Stream.empty();
	/**
	 * Extracted constant, to fix a warning because it occurred three or more times.
	 */
	private static final String TEST_THREE = "test_three";
	/**
	 * Extracted constant, to fix a warning because it occurred three or more times.
	 */
	private static final String TEST_TABLE_THREE = "test_table_three";
	/**
	 * Extracted constant, to fix a warning because it occurred three or more times.
	 */
	private static final String TEST_TABLE_TWO = "test_table_two";
	/**
	 * Extracted constant, to fix a warning because it occurred three or more times.
	 */
	private static final String TEST_TABLE_ONE = "test_table_one";
	/**
	 * The object we're testing.
	 */
	private ExplorationRunner runner = new ExplorationRunner();
	/**
	 * Mock table class.
	 */
	private static class MockTable implements EncounterTable {
		/**
		 * The (remaining) values that should be returned, in order.
		 */
		private final Deque<String> values = new LinkedList<>();
		/**
		 * Constructor.
		 * @param vals the values that should be returned, in order
		 */
		protected MockTable(final List<String> vals) {
			values.addAll(vals);
		}

		/**
		 * Generates an "encounter." For QuadrantTables this is always the same for each
		 * tile for random event tables the result will be randomly selected from that
		 * table.
		 *
		 * @param point         the location of the tile
		 * @param terrain       the terrain at the location
		 * @param fixtures      the fixtures on the tile, if any
		 * @param mapDimensions the dimensions of the map
		 * @return an appropriate event for that tile
		 */
		@Override
		public String generateEvent(final Point point, final TileType terrain,
									final Stream<TileFixture> fixtures,
									final MapDimensions mapDimensions) {
			return values.pop();
		}

		/**
		 * For table-debugging purposes.
		 *
		 * @return all events the table can return.
		 */
		@Override
		public Set<String> allEvents() {
			throw new UnsupportedOperationException("mock object");
		}
	}
	/**
	 * Set up for the tests.
	 */
	@Before
	public void setUp() {
		runner = new ExplorationRunner();
	}

	/**
	 * Test the getPrimaryRock method.
	 *
	 * @throws MissingTableException if the table is missing
	 */
	@Test
	public void testGetPrimaryRock() throws MissingTableException {
		runner.loadTable("major_rock", new MockTable(Collections.singletonList("primary_rock_test")));
		assertThat("primary rock test",
				runner.getPrimaryRock(PointFactory.point(0, 0), TileType.Tundra, EMPTY,
						new MapDimensionsImpl(69, 88, 2)), equalTo("primary_rock_test"));
	}

	/**
	 * Test the getPrimaryTree method.
	 *
	 * @throws MissingTableException if a table is missing
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetPrimaryTree() throws MissingTableException {
		runner.loadTable("boreal_major_tree",
				new MockTable(Collections.singletonList("boreal_major_test")));
		runner.loadTable("temperate_major_tree",
				new MockTable(Collections.singletonList("temperate_major_test")));
		final Point point = PointFactory.point(0, 0);
		final MapDimensions dimensions = new MapDimensionsImpl(69, 88, 2);
		assertThat("primary tree test for boreal forest",
				runner.getPrimaryTree(point, TileType.BorealForest, EMPTY, dimensions),
				equalTo("boreal_major_test"));
		assertThat("primary tree test for temperate forest",
				runner.getPrimaryTree(point, TileType.TemperateForest, EMPTY,
						dimensions),
				equalTo("temperate_major_test"));
	}

	/**
	 * Test that getPrimaryTree objects to non-forest tiles.
	 *
	 * @throws MissingTableException if a table is missing
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalGetPrimaryTree() throws MissingTableException {
		final Point point = PointFactory.point(0, 0);
		runner.getPrimaryTree(point, TileType.Tundra, EMPTY,
				new MapDimensionsImpl(69, 88, 2));
		fail("gave a primary tree for non-forest");
	}

	/**
	 * Test the consultTable method.
	 *
	 * @throws MissingTableException if a table is missing
	 */
	@Test
	public void testConsultTable() throws MissingTableException {
		runner.loadTable(TEST_TABLE_ONE,
				new MockTable(Collections.singletonList("test_one")));
		runner.loadTable(TEST_TABLE_TWO,
				new MockTable(Collections.singletonList("test_two")));
		runner.loadTable(TEST_TABLE_THREE,
				new MockTable(Collections.singletonList(TEST_THREE)));
		final Point point = PointFactory.point(0, 0);
		final MapDimensions dimensions = new MapDimensionsImpl(69, 88, 2);
		assertThat("first table", runner.consultTable(TEST_TABLE_ONE,
				point, TileType.Tundra, EMPTY, dimensions), equalTo("test_one"));
		assertThat("second table", runner.consultTable(TEST_TABLE_TWO, point,
				TileType.Tundra, EMPTY, dimensions), equalTo("test_two"));
		assertThat("third table", runner.consultTable(TEST_TABLE_THREE,
				point, TileType.Tundra, EMPTY, dimensions), equalTo(TEST_THREE));
	}

	/**
	 * Test the recursiveConsultTable method: the one method under test whose correctness
	 * is non-obvious. We don't use mock tables here because setting them up would be
	 * more trouble than they're worth.
	 *
	 * @throws MissingTableException if a table is missing
	 */
	@Test
	public void testRecursiveConsultTable() throws MissingTableException {
		runner.loadTable(TEST_TABLE_ONE, new ConstantTable("( #test_table_two# )"));
		runner.loadTable(TEST_TABLE_TWO, new ConstantTable("( #test_table_three# )"));
		runner.loadTable(TEST_TABLE_THREE, new ConstantTable(TEST_THREE));
		runner.loadTable("test_table_four", new ConstantTable("_ #test_table_one"));
		final Point point = PointFactory.point(0, 0);
		final MapDimensions dimensions = new MapDimensionsImpl(69, 88, 2);
		assertThat(
				"two levels of recursion",
				runner.recursiveConsultTable(TEST_TABLE_ONE, point,
						TileType.Tundra, EMPTY, dimensions),
				equalTo("( ( test_three ) )"));
		assertThat(
				"one level of recursion",
				runner.recursiveConsultTable(TEST_TABLE_TWO, point,
						TileType.Tundra, EMPTY, dimensions), equalTo("( test_three )"));
		assertThat(
				"no recursion",
				runner.recursiveConsultTable(TEST_TABLE_THREE, point,
						TileType.Tundra, EMPTY, dimensions), equalTo(TEST_THREE));
		assertThat(
				"one-sided split",
				runner.recursiveConsultTable("test_table_four", point,
						TileType.Plains, EMPTY, dimensions),
				equalTo("_ ( ( test_three ) )"));
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
		runner.loadTable("temperate_major_tree", new ConstantTable("temperate_tree"));
		final Point point = PointFactory.point(0, 0);
		final MapDimensions dimensions = new MapDimensionsImpl(69, 88, 2);
		assertThat("defaultResults in non-forest",
				runner.defaultResults(point, TileType.Tundra, EMPTY, dimensions).trim(),
				equalTo("The primary rock type here is test_rock."));
		assertThat("defaultResults in boreal forest",
				runner.defaultResults(point, TileType.BorealForest, EMPTY, dimensions),
				equalTo(String.format("The primary rock type here is test_rock.%n"
											  +
											  "The main kind of tree is boreal_tree" +
											  ".%n")));
		assertThat("defaultResults in temperate forest",
				runner.defaultResults(point, TileType.TemperateForest, EMPTY,
						dimensions),
				equalTo(String.format("The primary rock type here is test_rock.%n"
											  +
											  "The main kind of tree is temperate_tree" +
											  ".%n")));
	}

	/**
	 * Test recursive checking. Note that the method returns true if the table in
	 * question, or one it references, does *not* exist.
	 */
	@Test
	public void testRecursiveCheck() {
		runner.loadTable("existent_table", new ConstantTable("exists"));
		assertThat("base case of non-existent table",
				Boolean.valueOf(
						runner.recursiveCheck("non-existent-table")),
				equalTo(Boolean.TRUE));
		assertThat("base case of existent table",
				Boolean.valueOf(runner.recursiveCheck("existent_table")),
				equalTo(Boolean.FALSE));
		runner.loadTable("referent_one", new ConstantTable("#existent_table#"));
		runner.loadTable("referent_two", new ConstantTable("( #existent_table# )"));
		runner.loadTable("referent_three",
				new QuadrantTable(1, Arrays.asList("#referent_one#", "#referent_two#")));
		assertThat("recursive case to exercise cache-hits",
				Boolean.valueOf(runner.recursiveCheck("referent_three")),
				equalTo(Boolean.FALSE));
		runner.loadTable("false_referent", new ConstantTable("#nonexistent#"));
		assertThat("reference to nonexistent table",
				Boolean.valueOf(runner.recursiveCheck("false_referent")),
				equalTo(Boolean.TRUE));
	}

	/**
	 * Test the recursiveCheck() method.
	 */
	@Test
	public void testGlobalRecursiveCheck() {
		assertThat("recursive check with no tables",
				Boolean.valueOf(runner.recursiveCheck()),
				equalTo(Boolean.FALSE));
		runner.loadTable("existent", new ConstantTable("true_table"));
		assertThat("recursive check with only valid tables",
				Boolean.valueOf(runner.recursiveCheck()), equalTo(Boolean.FALSE));
		runner.loadTable("false_ref", new ConstantTable("#false#"));
		assertThat("recursive check with an invalid table",
				Boolean.valueOf(runner.recursiveCheck()), equalTo(Boolean.TRUE));
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the class
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TestExplorationRunner";
	}
}
